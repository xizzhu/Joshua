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

package me.xizzhu.android.joshua.reading.verse

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import java.lang.StringBuilder

data class VerseForReading(val verse: Verse, private val totalVerseCount: Int) {
    companion object {
        private val STRING_BUILDER = StringBuilder()
        private val PARALLEL_VERSE_SIZE_SPAN = RelativeSizeSpan(0.95F)
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()

        private fun buildVerseForDisplay(out: StringBuilder, verseIndex: VerseIndex, text: Verse.Text) {
            if (out.isNotEmpty()) {
                out.append('\n').append('\n')
            }
            out.append(text.translationShortName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .append('\n').append(text.text)
        }
    }

    val indexForDisplay: CharSequence by lazy {
        if (verse.parallel.isEmpty()) {
            STRING_BUILDER.setLength(0)
            val verseIndex = verse.verseIndex.verseIndex
            if (totalVerseCount >= 10) {
                if (totalVerseCount < 100) {
                    if (verseIndex + 1 < 10) {
                        STRING_BUILDER.append(' ')
                    }
                } else {
                    if (verseIndex + 1 < 10) {
                        STRING_BUILDER.append("  ")
                    } else if (verseIndex + 1 < 100) {
                        STRING_BUILDER.append(" ")
                    }
                }
            }
            STRING_BUILDER.append(verseIndex + 1)
            return@lazy STRING_BUILDER.toString()
        } else {
            return@lazy ""
        }
    }

    val textForDisplay: CharSequence by lazy {
        if (verse.parallel.isEmpty()) {
            return@lazy verse.text.text
        } else {
            // format:
            // <primary translation> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            // <empty line>
            // <parallel translation 1> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            // <parallel translation 2> <chapter verseIndex>:<verse verseIndex>
            // <verse text>

            STRING_BUILDER.setLength(0)
            buildVerseForDisplay(STRING_BUILDER, verse.verseIndex, verse.text)
            val primaryTextLength = STRING_BUILDER.length

            for (text in verse.parallel) {
                buildVerseForDisplay(STRING_BUILDER, verse.verseIndex, text)
            }

            SPANNABLE_STRING_BUILDER.clear()
            SPANNABLE_STRING_BUILDER.clearSpans()
            SPANNABLE_STRING_BUILDER.append(STRING_BUILDER)
            val length = SPANNABLE_STRING_BUILDER.length
            SPANNABLE_STRING_BUILDER.setSpan(PARALLEL_VERSE_SIZE_SPAN, primaryTextLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return@lazy SPANNABLE_STRING_BUILDER.subSequence(0, length)
        }
    }
}
