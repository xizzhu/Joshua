/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.annotated.highlights

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class HighlightItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_highlight, HighlightItem(VerseIndex.INVALID, "", "", "", Highlight.COLOR_NONE, Constants.DEFAULT_SORT_ORDER).viewType)
    }

    @Test
    fun testTextForDisplaySortByDate() {
        assertEquals(
                "Genesis 1:1\nIn the beginning God created the heaven and the earth.",
                HighlightItem(
                        MockContents.kjvVerses[0].verseIndex, MockContents.kjvBookNames[0],
                        MockContents.kjvBookShortNames[0], MockContents.kjvVerses[0].text.text, Highlight.COLOR_NONE, Constants.SORT_BY_DATE
                ).textForDisplay.toString()
        )
    }

    @Test
    fun testTextForDisplaySortByBook() {
        assertEquals(
                "Gen. 1:1 In the beginning God created the heaven and the earth.",
                HighlightItem(
                        MockContents.kjvVerses[0].verseIndex, MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0],
                        MockContents.kjvVerses[0].text.text, Highlight.COLOR_NONE, Constants.SORT_BY_BOOK
                ).textForDisplay.toString()
        )
    }
}
