/*
 * Copyright (C) 2019 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xizzhu.android.joshua.reading.detail

import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import java.lang.StringBuilder

data class VerseDetail(val verse: Verse) {
    companion object {
        private val STRING_BUILDER = StringBuilder()

        private fun buildVerseForDisplay(out: StringBuilder, verseIndex: VerseIndex, text: Verse.Text) {
            if (out.isNotEmpty()) {
                out.append('\n').append('\n')
            }
            out.append(text.translationShortName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .append(' ').append(text.text)
        }
    }

    private var stringForDisplay: String? = null

    fun getStringForDisplay(): String {
        if (stringForDisplay == null) {
            STRING_BUILDER.setLength(0)

            buildVerseForDisplay(STRING_BUILDER, verse.verseIndex, verse.text)
            for (text in verse.parallel) {
                buildVerseForDisplay(STRING_BUILDER, verse.verseIndex, text)
            }

            stringForDisplay = STRING_BUILDER.toString()
        }
        return stringForDisplay!!
    }
}
