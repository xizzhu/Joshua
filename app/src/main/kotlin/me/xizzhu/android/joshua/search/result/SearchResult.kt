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

import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import java.lang.StringBuilder

data class SearchedVerse(val verseIndex: VerseIndex, private val bookName: String, private val text: String) {
    private var textForDisplay: String? = null

    fun getTextForDisplay(): String {
        if (textForDisplay == null) {
            val stringBuilder = StringBuilder()
            stringBuilder.append(bookName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .append('\n').append(text)
            textForDisplay = stringBuilder.toString()
        }
        return textForDisplay!!
    }
}

typealias SearchResult = List<SearchedVerse>

fun List<Verse>.toSearchResult(): SearchResult {
    val searchResult: ArrayList<SearchedVerse> = ArrayList(size)
    for (verse in this) {
        searchResult.add(SearchedVerse(verse.verseIndex, verse.text.bookName, verse.text.text))
    }
    return searchResult
}
