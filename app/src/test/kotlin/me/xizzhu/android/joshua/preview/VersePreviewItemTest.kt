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

package me.xizzhu.android.joshua.preview

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class VersePreviewItemTest : BaseUnitTest() {
    @Test
    fun `test viewType`() {
        assertEquals(R.layout.item_preview_verse, VersePreviewItem(Verse.INVALID, 0).viewType)
    }

    @Test
    fun `test textForDisplay without followingEmptyVerseCount`() {
        val actual = VersePreviewItem(MockContents.kjvVerses[0], 0).textForDisplay
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual.toString())

        if (actual is Spanned) {
            val spans = actual.getSpans<Any>(0, 2)
            assertEquals(2, spans.size)
            assertEquals(0.85F, (spans[0] as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (spans[1] as StyleSpan).style)

            assertTrue(actual.getSpans<Any>(3).isEmpty())
        }
    }

    @Test
    fun `test textForDisplay with followingEmptyVerseCount`() {
        val actual = VersePreviewItem(MockContents.msgVerses[0], 1).textForDisplay
        assertEquals(
                "1:1-2 First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.",
                actual.toString()
        )

        if (actual is Spanned) {
            val spans = actual.getSpans<Any>(0, 4)
            assertEquals(2, spans.size)
            assertEquals(0.85F, (spans[0] as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (spans[1] as StyleSpan).style)

            assertTrue(actual.getSpans<Any>(5).isEmpty())
        }
    }
}
