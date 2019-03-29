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

package me.xizzhu.android.joshua.search.result

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import java.util.*

data class SearchedVerse(val verseIndex: VerseIndex, private val bookName: String,
                         private val text: String, private val query: String) {
    companion object {
        // We don't expect users to change locale that frequently.
        @SuppressLint("ConstantLocale")
        private val DEFAULT_LOCALE = Locale.getDefault()
    }

    private var textForDisplay: CharSequence? = null

    fun getTextForDisplay(): CharSequence {
        if (textForDisplay == null) {
            val builder = SpannableStringBuilder()
            builder.append(bookName)
                    .append(' ')
                    .append((verseIndex.chapterIndex + 1).toString())
                    .append(':')
                    .append((verseIndex.verseIndex + 1).toString())
                    .append('\n')
                    .append(text)

            // makes the book name & verse index smaller
            val textStartIndex = builder.length - text.length
            builder.setSpan(RelativeSizeSpan(0.95F), 0, textStartIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

            // highlights the keywords
            val lowerCase = builder.toString().toLowerCase(DEFAULT_LOCALE)
            for (keyword in query.trim().replace("\\s+", " ").split(" ")) {
                val start = lowerCase.indexOf(keyword.toLowerCase(DEFAULT_LOCALE), textStartIndex)
                if (start > 0) {
                    val end = start + keyword.length
                    builder.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    builder.setSpan(RelativeSizeSpan(1.2F), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }

            textForDisplay = builder
        }
        return textForDisplay!!
    }
}

typealias SearchResult = List<SearchedVerse>

fun List<Verse>.toSearchResult(query: String): SearchResult {
    val searchResult: ArrayList<SearchedVerse> = ArrayList(size)
    for (verse in this) {
        searchResult.add(SearchedVerse(verse.verseIndex, verse.text.bookName, verse.text.text, query))
    }
    return searchResult
}
