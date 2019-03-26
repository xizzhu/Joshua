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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class SearchResultTest: BaseUnitTest() {
    @Test
    fun testToSearchResult() {
        val expected = listOf(SearchedVerse(MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvVerses[0].text.bookName, MockContents.kjvVerses[0].text.text, ""))
        val actual = listOf(MockContents.kjvVerses[0]).toSearchResult("")
        assertEquals(expected, actual)
    }

    @Test
    fun testGetTextForDisplay() {
        val verseIndex = VerseIndex(1, 2, 3)
        val bookName = MockContents.kjvVerses[0].text.bookName
        val text = MockContents.kjvVerses[0].text.text
        val expected = "$bookName ${verseIndex.chapterIndex + 1}:${verseIndex.verseIndex + 1}\n$text"
        val actual = SearchedVerse(verseIndex, bookName, text, "").getTextForDisplay().toString()
        assertEquals(expected, actual)
    }
}
