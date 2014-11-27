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

public class SMAPBuilder(val source: String, val fileMappings: List<FileMapping>) {

    val header = """SMAP
                    $source
                    Kotlin
                    *S Kotlin"""

    fun build(): String? {
        if (fileMappings.empty) {
            return null;
        }

        var id = 1;

        val fileIds = "*F\n" +
                      fileMappings.fold("") {(a, e) ->
                          a + "${e.toSMAPFile(id++)}"
                      }

        val fileMappings = "*L\n" +
                      fileMappings.fold("") {(a, e) ->
                          a + "${e.toSMAPMapping()}"
                      }

        return header + "\n" + fileIds +"\n" + fileMappings + "\n*E"
    }

}

public class SourceMapper(val lineNumbers: Int) {

    private var currentOffset = lineNumbers;

    var fileMapping: FileMapping? = null;

    fun visitSource(name: String) {
        fileMapping = FileMapping(name)
    }

    fun visitLineNumber(iv: MethodVisitor, lineNumber: Int, start: Label) {
        iv.visitLineNumber(++currentOffset, start)
        fileMapping!!.lineMappings.add(LineMapping(lineNumber, currentOffset))
    }

}

class FileMapping(val name: String) {
    val lineMappings = arrayListOf<LineMapping>()

    var id = -1;

    fun toSMAPFile(id: Int): String {
        this.id = id;
        return "$id $name"
    }

    fun toSMAPMapping(): String {
        return "$id $name"
    }
}

class LineMapping(val source: Int, val dest: Int) {

    fun toSMAP(): String {
        return "$source:$dest"
    }
}