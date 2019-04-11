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

package me.xizzhu.android.joshua.notes

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex

data class NoteForDisplay(val verseIndex: VerseIndex, val text: Verse.Text, val note: String, val timestamp: Long) {
    companion object {
        private val BOOK_NAME_STYLE_SPAN = StyleSpan(Typeface.BOLD)
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    private var textForDisplay: CharSequence? = null

    fun getVerseForDisplay(): CharSequence {
        if (textForDisplay == null) {
            SPANNABLE_STRING_BUILDER.clear()
            SPANNABLE_STRING_BUILDER.clearSpans()

            // format:
            // <book name> <chapter index>:<verse index> <verse text>
            SPANNABLE_STRING_BUILDER.append(text.bookName).append(' ')
                    .append((verseIndex.chapterIndex + 1).toString()).append(':').append((verseIndex.verseIndex + 1).toString())
            SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_STYLE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

            SPANNABLE_STRING_BUILDER.append(' ').append(text.text)

            textForDisplay = SPANNABLE_STRING_BUILDER.subSequence(0, SPANNABLE_STRING_BUILDER.length)
        }

        return textForDisplay!!
    }
}
