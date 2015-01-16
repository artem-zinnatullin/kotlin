/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

class SMAPParser(val input: String?, val path: String) {

    val fileMappings = linkedMapOf<Int, RawFileMapping>()

    fun parse() : SMAP {
        if (input == null || input.isEmpty()) {
            return SMAP(listOf())
        }

        val fileSectionStart = input.indexOf(SMAP.FILE_SECTION) + SMAP.FILE_SECTION.length
        val lineSectionAnchor = input.indexOf(SMAP.LINE_SECTION)
        val files = input.substring(fileSectionStart, lineSectionAnchor)


        val fileEntries = files.trim().split('+')

        for (fileDeclaration in fileEntries) {
            if (fileDeclaration == "") continue;
            val fileInternalName = fileDeclaration.trim()

            val indexEnd = fileInternalName.indexOf(' ')
            val fileIndex = Integer.valueOf(fileInternalName.substring(0, indexEnd))
            val newLine = fileInternalName.indexOf('\n')
            val fileName = fileInternalName.substring(indexEnd + 1, newLine)
            fileMappings.put(fileIndex, RawFileMapping(fileName, fileInternalName.substring(newLine + 1).trim()))
        }


        val lines = input.substring(lineSectionAnchor + SMAP.LINE_SECTION.length, input.indexOf(SMAP.END)).trim().split('\n')
        for (lineMapping in lines) {
            /*only simple mapping now*/
            val fileSeparator = lineMapping.indexOf('#')
            val splitIndex = lineMapping.indexOf(':')
            val originalIndex = Integer.valueOf(lineMapping.substring(0, fileSeparator))
            val fileIndex = Integer.valueOf(lineMapping.substring(fileSeparator + 1, splitIndex))
            val targetIndex = Integer.valueOf(lineMapping.substring(splitIndex + 1))
            fileMappings[fileIndex]!!.mapLine(originalIndex, targetIndex, true)
        }

        return SMAP(fileMappings.values().map { it.toFileMapping() })
    }
}