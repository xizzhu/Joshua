/*
 * Copyright (C) 2023 Xizhi Zhu
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

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.core.text.getSpans
import io.mockk.every
import io.mockk.mockk
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class SimpleVerseItemTest : BaseUnitTest() {
    private lateinit var drawable: Drawable

    @BeforeTest
    override fun setup() {
        super.setup()

        drawable = mockk<Drawable>().apply {
            every { intrinsicWidth } returns 0
            every { intrinsicHeight } returns 0
            every { setBounds(0, 0, 0, 0) } returns Unit
            every { colorFilter = any() } returns Unit
        }
    }

    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_simple_verse, SimpleVerseItem(mockk(), Verse.INVALID, 0, 0, false, Highlight.COLOR_NONE, false, false).viewType)
    }

    @Test
    fun testIndexForDisplay() {
        assertEquals("1", SimpleVerseItem(mockk(), MockContents.kjvVerses[0], 9, 0, false, Highlight.COLOR_NONE, false, false).indexForDisplay)

        assertEquals(" 1", SimpleVerseItem(mockk(), MockContents.kjvVerses[0], 10, 0, false, Highlight.COLOR_NONE, false, false).indexForDisplay)
        assertEquals("10", SimpleVerseItem(mockk(), MockContents.kjvVerses[9], 10, 0, false, Highlight.COLOR_NONE, false, false).indexForDisplay)
        assertEquals("99", SimpleVerseItem(mockk(), Verse(VerseIndex(1, 2, 98), Verse.Text("", ""), emptyList()), 99, 0, false, Highlight.COLOR_NONE, false, false).indexForDisplay)

        assertEquals("  1", SimpleVerseItem(mockk(), MockContents.kjvVerses[0], 100, 0, false, Highlight.COLOR_NONE, false, false).indexForDisplay)
        assertEquals(" 10", SimpleVerseItem(mockk(), MockContents.kjvVerses[9], 100, 0, false, Highlight.COLOR_NONE, false, false).indexForDisplay)
        assertEquals("100", SimpleVerseItem(mockk(), Verse(VerseIndex(1, 2, 99), Verse.Text("", ""), emptyList()), 100, 0, false, Highlight.COLOR_NONE, false, false).indexForDisplay)
    }

    @Test
    fun testIndexForDisplayWithEmptyFollowingVerses() {
        assertEquals("1-2", SimpleVerseItem(mockk(), MockContents.msgVerses[0], MockContents.msgVerses.size, 1, false, Highlight.COLOR_NONE, false, false).indexForDisplay)
    }

    @Test
    fun testIndexForDisplayWithParallel() {
        assertTrue(SimpleVerseItem(mockk(), MockContents.msgVersesWithKjvParallel[0], MockContents.msgVerses.size, 1, false, Highlight.COLOR_NONE, false, false).indexForDisplay.isEmpty())
    }

    @Test
    fun testTextForDisplay() {
        assertEquals(
                MockContents.kjvVerses[0].text.text,
                SimpleVerseItem(mockk(), MockContents.kjvVerses[0], MockContents.kjvVerses.size, 0, false, Highlight.COLOR_NONE, false, false).textForDisplay.toString()
        )
    }

    @Test
    fun `test textForDisplay with bookmark`() {
        val context = mockk<Context>().apply { every { getDrawable(R.drawable.ic_bookmark) } returns drawable }

        val actual = SimpleVerseItem(context, MockContents.kjvVerses[0], MockContents.kjvVerses.size, 0, true, Highlight.COLOR_NONE, false, false).textForDisplay
        assertEquals("In the beginning God created the heaven and the earth. ", actual.toString())
        assertEquals(1, (actual as SpannableStringBuilder).getSpans<Any>().size)
        assertTrue(actual.getSpans<Any>()[0] is ImageSpan)
    }

    @Test
    fun `test textForDisplay with note`() {
        val context = mockk<Context>().apply { every { getDrawable(R.drawable.ic_note) } returns drawable }

        val actual = SimpleVerseItem(context, MockContents.kjvVerses[0], MockContents.kjvVerses.size, 0, false, Highlight.COLOR_NONE, true, false).textForDisplay
        assertEquals("In the beginning God created the heaven and the earth. ", actual.toString())
        assertEquals(1, (actual as SpannableStringBuilder).getSpans<Any>().size)
        assertTrue(actual.getSpans<Any>()[0] is ImageSpan)
    }

    @Test
    fun `test textForDisplay with bookmark and note`() {
        val context = mockk<Context>().apply { every { getDrawable(any()) } returns drawable }

        val actual = SimpleVerseItem(context, MockContents.kjvVerses[0], MockContents.kjvVerses.size, 0, true, Highlight.COLOR_NONE, true, false).textForDisplay
        assertEquals("In the beginning God created the heaven and the earth.  ", actual.toString())
        assertEquals(2, (actual as SpannableStringBuilder).getSpans<Any>().size)
        assertTrue(actual.getSpans<Any>()[0] is ImageSpan)
        assertTrue(actual.getSpans<Any>()[1] is ImageSpan)
    }

    @Test
    fun testTextForDisplayWithParallelTranslations() {
        assertEquals(
                "${MockContents.kjvShortName} 1:1\n${MockContents.kjvVersesWithCuvParallel[0].text.text}\n\n${MockContents.cuvShortName} 1:1\n${MockContents.kjvVersesWithCuvParallel[0].parallel[0].text}",
                SimpleVerseItem(mockk(), MockContents.kjvVersesWithCuvParallel[0], MockContents.kjvVerses.size, 0, false, Highlight.COLOR_NONE, false, false).textForDisplay.toString()
        )
    }
}
