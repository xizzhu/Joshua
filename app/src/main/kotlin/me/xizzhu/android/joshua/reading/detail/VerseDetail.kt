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

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex

data class VerseDetail(val verse: Verse, val bookmarked: Boolean, val note: String) {
    companion object {
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()

        private fun buildVerseForDisplay(out: SpannableStringBuilder, verseIndex: VerseIndex, text: Verse.Text) {
            if (out.isNotEmpty()) {
                out.append('\n').append('\n')
            }

            val startIndex = out.length
            out.append(text.translationShortName).append(", ").append(text.bookName).append(' ')
                    .append((verseIndex.chapterIndex + 1).toString()).append(':').append((verseIndex.verseIndex + 1).toString())
            out.setSpan(RelativeSizeSpan(0.95F), startIndex, out.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

            out.append('\n').append(text.text)
        }
    }

    private var stringForDisplay: CharSequence? = null

    fun getTextForDisplay(): CharSequence {
        if (stringForDisplay == null) {
            SPANNABLE_STRING_BUILDER.clear()
            SPANNABLE_STRING_BUILDER.clearSpans()

            buildVerseForDisplay(SPANNABLE_STRING_BUILDER, verse.verseIndex, verse.text)
            for (text in verse.parallel) {
                buildVerseForDisplay(SPANNABLE_STRING_BUILDER, verse.verseIndex, text)
            }

            stringForDisplay = SPANNABLE_STRING_BUILDER
        }
        return stringForDisplay!!
    }

    fun toBuilder(): Builder = Builder(verse, bookmarked, note)

    data class Builder(var verse: Verse = Verse.INVALID, var bookmarked: Boolean = false, var note: String = "") {
        fun verse(verse: Verse) = apply { this.verse = verse }
        fun bookmarked(bookmarked: Boolean) = apply { this.bookmarked = bookmarked }
        fun note(note: String) = apply { this.note = note }
        fun build() = VerseDetail(verse, bookmarked, note)
    }
}
