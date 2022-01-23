/*
 * Copyright (C) 2022 Xizhi Zhu
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

package me.xizzhu.android.joshua.search

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class SearchVerseItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_search_verse, SearchVerseItem(VerseIndex.INVALID, "", "", "", Highlight.COLOR_NONE).viewType)
    }

    @Test
    fun testTextForDisplay() {
        val verseIndex = VerseIndex(1, 2, 3)
        val bookShortName = MockContents.kjvBookShortNames[0]
        val text = MockContents.kjvVerses[0].text.text
        assertEquals(
                "$bookShortName ${verseIndex.chapterIndex + 1}:${verseIndex.verseIndex + 1}\n$text",
                SearchVerseItem(verseIndex, bookShortName, text, "", Highlight.COLOR_NONE).textForDisplay.toString()
        )
    }
}
