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
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.SearchItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchResultTest : BaseUnitTest() {
    @Test
    fun testEmptyVerseList() {
        val actual = emptyList<Verse>().toSearchResult("", {})
        assertTrue(actual.items.isEmpty())
        assertEquals(0, actual.searchResultCount)
    }

    @Test
    fun testVerseList() {
        val verses: List<Verse> = listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        val query = "query"
        val onClickListener: (VerseIndex) -> Unit = {}
        assertEquals(SearchResult(
                listOf(
                        TitleItem(MockContents.kjvVerses[0].text.bookName, false),
                        SearchItem(MockContents.kjvVerses[0].verseIndex, MockContents.kjvVerses[0].text.bookName,
                                MockContents.kjvVerses[0].text.text, query, onClickListener),
                        SearchItem(MockContents.kjvVerses[1].verseIndex, MockContents.kjvVerses[1].text.bookName,
                                MockContents.kjvVerses[1].text.text, query, onClickListener),
                        SearchItem(MockContents.kjvVerses[2].verseIndex, MockContents.kjvVerses[2].text.bookName,
                                MockContents.kjvVerses[2].text.text, query, onClickListener)
                ), verses.size),
                verses.toSearchResult(query, onClickListener))
    }
}
