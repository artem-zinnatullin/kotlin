/*
 * Copyright 2010-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Label

public class SMAPBuilder(val source: String,
                         val path: String,
                         val fileMappings: List<FileMapping>,
                         val lineCountInOriginalFile: Int) {

    val header = "SMAP\n$source\nKotlin\n*S Kotlin"

    fun build(): String? {
        if (fileMappings.isEmpty()) {
            return null;
        }


        val defaultSourceMapping = RawFileMapping(source, path)
        for(i in 1..lineCountInOriginalFile) {
            defaultSourceMapping.mapLine(i, i - 1, true)
        }
        val allMappings = arrayListOf(defaultSourceMapping.toFileMapping())
        allMappings.addAll(fileMappings)

        var id = 1;

        val fileIds = "*F" +
                      allMappings.fold("") {(a, e) ->
                          a + "\n${e.toSMAPFile(id++)}"
                      }

        val fileMappings = "*L" +
                      allMappings.fold("") {(a, e) ->
                          a + "${e.toSMAPMapping()}"
                      }

        return header + "\n" + fileIds +"\n" + fileMappings + "\n*E\n"
    }
}

public object IdenticalSourceMapper: SourceMapper(-1) {

    override fun visitSource(name: String, path: String) {

    }

    override fun visitLineNumber(iv: MethodVisitor, lineNumber: Int, start: Label) {
        iv.visitLineNumber(lineNumber, start)
    }
}

public open class SourceMapper(val lineNumbers: Int) {

    private var currentOffset = lineNumbers;

    var fileMapping: MutableList<RawFileMapping> = arrayListOf();

    open fun visitSource(name: String, path: String) {
        fileMapping.add(RawFileMapping(name, path))
    }

    open fun visitLineNumber(iv: MethodVisitor, lineNumber: Int, start: Label) {
        val mappedLine = fileMapping.last().mapLine(lineNumber, currentOffset, true)
        iv.visitLineNumber(mappedLine, start)
        currentOffset = Math.max(currentOffset, mappedLine)
    }

}
/*Source Mapping*/
class SMAP(fileMappings: List<FileMapping>) {
    var fileMappings: List<FileMapping> = arrayListOf()

    class object {
        val FILE_SECTION = "*F"

        val LINE_SECTION = "*L"

        val END = "*E"

    }
}

class RawFileMapping(val name: String, val path: String) {
    private val lineMappings = linkedMapOf<Int, Int>()
    private val rangeMappings = arrayListOf<RangeMapping>()

    private var lastMappedWithNewIndex = -1000;

    fun toFileMapping(): FileMapping {
        val fileMapping = FileMapping(name, path)
        for (range in rangeMappings) {
            fileMapping.addLineMapping(range)
        }
        return fileMapping
    }

    fun mapLine(source: Int, currentIndex: Int, isLastMapped: Boolean): Int {
        var dest = lineMappings.get(source);
        if (dest == null) {
            val rangeMapping: RangeMapping
            if (rangeMappings.isNotEmpty() && isLastMapped && couldFoldInRange(lastMappedWithNewIndex, source)) {
                rangeMapping = rangeMappings.last()
                rangeMapping.range += source - lastMappedWithNewIndex;
                dest = lineMappings.get(lastMappedWithNewIndex) + source - lastMappedWithNewIndex;
            } else {
                dest = currentIndex + 1;
                rangeMapping = RangeMapping(source, dest)
                rangeMappings.add(rangeMapping)
            }

            lineMappings.put(source, dest)
            lastMappedWithNewIndex = source;
        }

        return dest
    }

    private fun couldFoldInRange(first: Int, second: Int): Boolean {
        val delta = second - first
        return delta > 0 && delta <= 10;
    }
}

public class FileMapping(val name: String, val path: String) {
    private val lineMappings = arrayListOf<RangeMapping>()

    var id = -1;

    fun toSMAPFile(id: Int): String {
        this.id = id;
        return "+ $id $name\n$path"
    }

    fun toSMAPMapping(): String {
        return lineMappings.fold("") {
            (a, e) ->
            "$a\n${e.toSMAP(id)}"
        }
    }

    fun addLineMapping(lineMapping: RangeMapping) {
        lineMappings.add(lineMapping)
    }
}

public class RangeMapping(val source: Int, val dest: Int, var range: Int = 1) {

    fun toSMAP(fileId: Int): String {
        return if (range == 1) "$source#$fileId:$dest" else "$source#$fileId,$range:$dest"
    }

    fun appendRangeTo() {

    }
}