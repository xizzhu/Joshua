/*
 * Copyright (C) 2020 Xizhi Zhu
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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class SimpleVerseItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_simple_verse, SimpleVerseItem(Verse.INVALID, 0, 0, Highlight.COLOR_NONE, {}, {}).viewType)
    }

    @Test
    fun testIndexForDisplay() {
        assertEquals("1", SimpleVerseItem(MockContents.kjvVerses[0], 9, 0, Highlight.COLOR_NONE, {}, {}).indexForDisplay)

        assertEquals(" 1", SimpleVerseItem(MockContents.kjvVerses[0], 10, 0, Highlight.COLOR_NONE, {}, {}).indexForDisplay)
        assertEquals("10", SimpleVerseItem(MockContents.kjvVerses[9], 10, 0, Highlight.COLOR_NONE, {}, {}).indexForDisplay)
        assertEquals("99", SimpleVerseItem(Verse(VerseIndex(1, 2, 98), Verse.Text("", ""), emptyList()), 99, 0, Highlight.COLOR_NONE, {}, {}).indexForDisplay)

        assertEquals("  1", SimpleVerseItem(MockContents.kjvVerses[0], 100, 0, Highlight.COLOR_NONE, {}, {}).indexForDisplay)
        assertEquals(" 10", SimpleVerseItem(MockContents.kjvVerses[9], 100, 0, Highlight.COLOR_NONE, {}, {}).indexForDisplay)
        assertEquals("100", SimpleVerseItem(Verse(VerseIndex(1, 2, 99), Verse.Text("", ""), emptyList()), 100, 0, Highlight.COLOR_NONE, {}, {}).indexForDisplay)
    }

    @Test
    fun testIndexForDisplayWithEmptyFollowingVerses() {
        assertEquals("1-2", SimpleVerseItem(MockContents.msgVerses[0], MockContents.msgVerses.size, 1, Highlight.COLOR_NONE, {}, {}).indexForDisplay)
    }

    @Test
    fun testIndexForDisplayWithParallel() {
        assertTrue(SimpleVerseItem(MockContents.msgVersesWithKjvParallel[0], MockContents.msgVerses.size, 1, Highlight.COLOR_NONE, {}, {}).indexForDisplay.isEmpty())
    }

    @Test
    fun testTextForDisplay() {
        assertEquals(MockContents.kjvVerses[0].text.text,
                SimpleVerseItem(MockContents.kjvVerses[0], MockContents.kjvVerses.size, 0, Highlight.COLOR_NONE, {}, {}).textForDisplay.toString())
    }

    @Test
    fun testTextForDisplayWithParallelTranslations() {
        assertEquals("${MockContents.kjvShortName} 1:1\n${MockContents.kjvVersesWithCuvParallel[0].text.text}\n\n${MockContents.cuvShortName} 1:1\n${MockContents.kjvVersesWithCuvParallel[0].parallel[0].text}",
                SimpleVerseItem(MockContents.kjvVersesWithCuvParallel[0], MockContents.kjvVerses.size, 0, Highlight.COLOR_NONE, {}, {}).textForDisplay.toString())
    }
}
