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

package me.xizzhu.android.joshua.search

import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class KeywordHighlighterTest : BaseUnitTest() {
    @Test
    fun `test highlight with no matching keyword`() {
        with(SpannableStringBuilder("Gen. 1:1 In the beginning God created the heaven and the earth.").highlightKeyword("no-match", 0)) {
            assertTrue(getSpans<RelativeSizeSpan>(0, length).isEmpty())
            assertTrue(getSpans<StyleSpan>(0, length).isEmpty())
        }

        with(SpannableStringBuilder("Gen. 1:1 In the beginning God created the heaven and the earth.").highlightKeyword("gen", 9)) {
            assertTrue(getSpans<RelativeSizeSpan>(0, length).isEmpty())
            assertTrue(getSpans<StyleSpan>(0, length).isEmpty())
        }
    }

    @Test
    fun `test highlight with one keyword and one match`() {
        val ssb = SpannableStringBuilder("Gen. 1:1 In the beginning God created the heaven and the earth.")
                .highlightKeyword("god", 9)

        val relativeSizeSpans = ssb.getSpans<RelativeSizeSpan>(0, ssb.length)
        assertEquals(1, relativeSizeSpans.size)
        assertEquals(26, ssb.getSpanStart(relativeSizeSpans[0]))
        assertEquals(29, ssb.getSpanEnd(relativeSizeSpans[0]))

        val styleSpans = ssb.getSpans<StyleSpan>(0, ssb.length)
        assertEquals(1, styleSpans.size)
        assertEquals(26, ssb.getSpanStart(styleSpans[0]))
        assertEquals(29, ssb.getSpanEnd(styleSpans[0]))
    }

    @Test
    fun `test highlight with one keyword and multiple matches`() {
        val ssb = SpannableStringBuilder("Gen. 1:1 In the beginning God created the heaven and the earth.")
                .highlightKeyword("The", 9)

        val relativeSizeSpans = ssb.getSpans<RelativeSizeSpan>(0, ssb.length)
        assertEquals(3, relativeSizeSpans.size)
        assertEquals(12, ssb.getSpanStart(relativeSizeSpans[0]))
        assertEquals(15, ssb.getSpanEnd(relativeSizeSpans[0]))
        assertEquals(38, ssb.getSpanStart(relativeSizeSpans[1]))
        assertEquals(41, ssb.getSpanEnd(relativeSizeSpans[1]))
        assertEquals(53, ssb.getSpanStart(relativeSizeSpans[2]))
        assertEquals(56, ssb.getSpanEnd(relativeSizeSpans[2]))

        val styleSpans = ssb.getSpans<StyleSpan>(0, ssb.length)
        assertEquals(3, styleSpans.size)
        assertEquals(12, ssb.getSpanStart(styleSpans[0]))
        assertEquals(15, ssb.getSpanEnd(styleSpans[0]))
        assertEquals(38, ssb.getSpanStart(styleSpans[1]))
        assertEquals(41, ssb.getSpanEnd(styleSpans[1]))
        assertEquals(53, ssb.getSpanStart(styleSpans[2]))
        assertEquals(56, ssb.getSpanEnd(styleSpans[2]))
    }

    @Test
    fun `test highlight with multiple keywords and multiple matches`() {
        val ssb = SpannableStringBuilder("Gen. 1:1 In the beginning God created the heaven and the earth.")
                .highlightKeyword("The god", 9)

        val relativeSizeSpans = ssb.getSpans<RelativeSizeSpan>(0, ssb.length)
        assertEquals(4, relativeSizeSpans.size)
        assertEquals(12, ssb.getSpanStart(relativeSizeSpans[0]))
        assertEquals(15, ssb.getSpanEnd(relativeSizeSpans[0]))
        assertEquals(38, ssb.getSpanStart(relativeSizeSpans[1]))
        assertEquals(41, ssb.getSpanEnd(relativeSizeSpans[1]))
        assertEquals(53, ssb.getSpanStart(relativeSizeSpans[2]))
        assertEquals(56, ssb.getSpanEnd(relativeSizeSpans[2]))
        assertEquals(26, ssb.getSpanStart(relativeSizeSpans[3]))
        assertEquals(29, ssb.getSpanEnd(relativeSizeSpans[3]))

        val styleSpans = ssb.getSpans<StyleSpan>(0, ssb.length)
        assertEquals(4, styleSpans.size)
        assertEquals(12, ssb.getSpanStart(styleSpans[0]))
        assertEquals(15, ssb.getSpanEnd(styleSpans[0]))
        assertEquals(38, ssb.getSpanStart(styleSpans[1]))
        assertEquals(41, ssb.getSpanEnd(styleSpans[1]))
        assertEquals(53, ssb.getSpanStart(styleSpans[2]))
        assertEquals(56, ssb.getSpanEnd(styleSpans[2]))
        assertEquals(26, ssb.getSpanStart(styleSpans[3]))
        assertEquals(29, ssb.getSpanEnd(styleSpans[3]))
    }
}
