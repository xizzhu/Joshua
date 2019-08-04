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

package me.xizzhu.android.joshua.reading.verse

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.Test
import kotlin.test.assertEquals

class VerseFormatterTest : BaseUnitTest() {
    @Test
    fun testFormat() {
        assertEquals("${MockContents.kjvBookNames[0]} 1:1\n${MockContents.kjvVerses[0].text.text}",
                SpannableStringBuilder().format(MockContents.kjvVerses[0], MockContents.kjvBookNames[0], 0, false, Highlight.COLOR_NONE).toString())
    }

    @Test
    fun testFormatWithSimpleReadingMode() {
        assertEquals(MockContents.kjvVerses[0].text.text,
                SpannableStringBuilder().format(MockContents.kjvVerses[0], MockContents.kjvBookNames[0], 0, true, Highlight.COLOR_NONE).toString())
    }

    @Test
    fun testFormatWithFollowingEmptyVerse() {
        assertEquals("${MockContents.msgBookNames[0]} 1:1-2\n${MockContents.msgVerses[0].text.text}",
                SpannableStringBuilder().format(MockContents.msgVerses[0], MockContents.msgBookNames[0], 1, false, Highlight.COLOR_NONE).toString())
    }

    @Test
    fun testFormatWithHighlight() {
        val ssb = SpannableStringBuilder()
        assertEquals("${MockContents.kjvBookNames[0]} 1:1\n${MockContents.kjvVerses[0].text.text}",
                ssb.format(MockContents.kjvVerses[0], MockContents.kjvBookNames[0], 0, false, Highlight.COLOR_PINK).toString())

        val actualBackgroundColorSpans = ssb.getSpans(0, ssb.length, BackgroundColorSpan::class.java)
        assertEquals(1, actualBackgroundColorSpans.size)
        assertEquals(Highlight.COLOR_PINK, actualBackgroundColorSpans[0].backgroundColor)

        val actualForegroundColorSpans = ssb.getSpans(0, ssb.length, ForegroundColorSpan::class.java)
        assertEquals(1, actualForegroundColorSpans.size)
        assertEquals(Color.BLACK, actualForegroundColorSpans[0].foregroundColor)
    }

    @Test
    fun testFormatWithBlueHighlight() {
        val ssb = SpannableStringBuilder()
        assertEquals("${MockContents.kjvBookNames[0]} 1:1\n${MockContents.kjvVerses[0].text.text}",
                ssb.format(MockContents.kjvVerses[0], MockContents.kjvBookNames[0], 0, false, Highlight.COLOR_BLUE).toString())

        val actualBackgroundColorSpans = ssb.getSpans(0, ssb.length, BackgroundColorSpan::class.java)
        assertEquals(1, actualBackgroundColorSpans.size)
        assertEquals(Highlight.COLOR_BLUE, actualBackgroundColorSpans[0].backgroundColor)

        val actualForegroundColorSpans = ssb.getSpans(0, ssb.length, ForegroundColorSpan::class.java)
        assertEquals(1, actualForegroundColorSpans.size)
        assertEquals(Color.WHITE, actualForegroundColorSpans[0].foregroundColor)
    }

    @Test
    fun testFormatWithHighlightAndFollowingEmptyVerse() {
        val ssb = SpannableStringBuilder()
        assertEquals("${MockContents.msgBookNames[0]} 1:1-2\n${MockContents.msgVerses[0].text.text}",
                ssb.format(MockContents.msgVerses[0], MockContents.msgBookNames[0], 1, false, Highlight.COLOR_PINK).toString())

        val actualBackgroundColorSpans = ssb.getSpans(0, ssb.length, BackgroundColorSpan::class.java)
        assertEquals(1, actualBackgroundColorSpans.size)
        assertEquals(Highlight.COLOR_PINK, actualBackgroundColorSpans[0].backgroundColor)

        val actualForegroundColorSpans = ssb.getSpans(0, ssb.length, ForegroundColorSpan::class.java)
        assertEquals(1, actualForegroundColorSpans.size)
        assertEquals(Color.BLACK, actualForegroundColorSpans[0].foregroundColor)
    }

    @Test
    fun testFormatWithParallel() {
        assertEquals("${MockContents.kjvShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].text.text}\n\n${MockContents.bbeShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].parallel[0].text}\n\n${MockContents.cuvShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].parallel[1].text}",
                SpannableStringBuilder().format(MockContents.kjvVersesWithBbeCuvParallel[0], MockContents.kjvBookNames[0], 0, false, Highlight.COLOR_NONE).toString())
    }

    @Test
    fun testFormatWithParallelAndFollowingEmptyVerse() {
        assertEquals("${MockContents.msgShortName} 1:1-2\n${MockContents.msgVersesWithKjvParallel[0].text.text}\n\n${MockContents.kjvShortName} 1:1-2\n${MockContents.msgVersesWithKjvParallel[0].parallel[0].text}",
                SpannableStringBuilder().format(MockContents.msgVersesWithKjvParallel[0], MockContents.msgBookNames[0], 1, false, Highlight.COLOR_NONE).toString())
    }

    @Test
    fun testFormatWithParallelAndHighlight() {
        val ssb = SpannableStringBuilder()
        assertEquals("${MockContents.kjvShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].text.text}\n\n${MockContents.bbeShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].parallel[0].text}\n\n${MockContents.cuvShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].parallel[1].text}",
                ssb.format(MockContents.kjvVersesWithBbeCuvParallel[0], MockContents.kjvBookNames[0], 0, false, Highlight.COLOR_PINK).toString())

        val actualBackgroundColorSpans = ssb.getSpans(0, ssb.length, BackgroundColorSpan::class.java)
        assertEquals(1, actualBackgroundColorSpans.size)
        assertEquals(Highlight.COLOR_PINK, actualBackgroundColorSpans[0].backgroundColor)

        val actualForegroundColorSpans = ssb.getSpans(0, ssb.length, ForegroundColorSpan::class.java)
        assertEquals(1, actualForegroundColorSpans.size)
        assertEquals(Color.BLACK, actualForegroundColorSpans[0].foregroundColor)
    }

    @Test
    fun testFormatWithParallelAndBlueHighlight() {
        val ssb = SpannableStringBuilder()
        assertEquals("${MockContents.kjvShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].text.text}\n\n${MockContents.bbeShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].parallel[0].text}\n\n${MockContents.cuvShortName} 1:1\n${MockContents.kjvVersesWithBbeCuvParallel[0].parallel[1].text}",
                ssb.format(MockContents.kjvVersesWithBbeCuvParallel[0], MockContents.kjvBookNames[0], 0, false, Highlight.COLOR_BLUE).toString())

        val actualBackgroundColorSpans = ssb.getSpans(0, ssb.length, BackgroundColorSpan::class.java)
        assertEquals(1, actualBackgroundColorSpans.size)
        assertEquals(Highlight.COLOR_BLUE, actualBackgroundColorSpans[0].backgroundColor)

        val actualForegroundColorSpans = ssb.getSpans(0, ssb.length, ForegroundColorSpan::class.java)
        assertEquals(1, actualForegroundColorSpans.size)
        assertEquals(Color.WHITE, actualForegroundColorSpans[0].foregroundColor)
    }

    @Test
    fun testFormatWithParallelAndHighlightAndFollowingEmptyVerse() {
        val ssb = SpannableStringBuilder()
        assertEquals("${MockContents.msgShortName} 1:1-2\n${MockContents.msgVersesWithKjvParallel[0].text.text}\n\n${MockContents.kjvShortName} 1:1-2\n${MockContents.msgVersesWithKjvParallel[0].parallel[0].text}",
                ssb.format(MockContents.msgVersesWithKjvParallel[0], MockContents.msgBookNames[0], 1, false, Highlight.COLOR_PINK).toString())

        val actualBackgroundColorSpans = ssb.getSpans(0, ssb.length, BackgroundColorSpan::class.java)
        assertEquals(1, actualBackgroundColorSpans.size)
        assertEquals(Highlight.COLOR_PINK, actualBackgroundColorSpans[0].backgroundColor)

        val actualForegroundColorSpans = ssb.getSpans(0, ssb.length, ForegroundColorSpan::class.java)
        assertEquals(1, actualForegroundColorSpans.size)
        assertEquals(Color.BLACK, actualForegroundColorSpans[0].foregroundColor)
    }
}
