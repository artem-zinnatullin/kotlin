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
                         val defaultLineNumbers: Int) {

    val header = "SMAP\n$source\nKotlin\n*S Kotlin"

    fun build(): String? {
        if (fileMappings.empty) {
            return null;
        }


        val defaultSourceMapping = FileMapping(source, path)
        for(i in 1..defaultLineNumbers) {
            defaultSourceMapping.addLineMapping(i, i)
        }
        val allMappings = arrayListOf(defaultSourceMapping)
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

    var fileMapping: MutableList<FileMapping> = arrayListOf();

    open fun visitSource(name: String, path: String) {
        fileMapping.add(FileMapping(name, path))
    }

    open fun visitLineNumber(iv: MethodVisitor, lineNumber: Int, start: Label) {
        iv.visitLineNumber(++currentOffset, start)
        fileMapping.last!!.addLineMapping(lineNumber, currentOffset)
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

class FileMapping(val name: String, val path: String) {
    private val lineMappings = arrayListOf<LineMapping>()

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

    fun addLineMapping(source: Int, dest: Int) {
        lineMappings.add(LineMapping(source, dest))
    }
}

class LineMapping(val source: Int, val dest: Int) {

    fun toSMAP(fileId: Int): String {
        return "$source#$fileId:$dest"
    }
}