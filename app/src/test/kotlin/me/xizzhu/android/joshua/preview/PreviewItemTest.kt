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

package me.xizzhu.android.joshua.preview

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex

@RunWith(RobolectricTestRunner::class)
class PreviewItemTest : BaseUnitTest() {
    @Test
    fun `test DiffCallback`() {
        val diffCallback = PreviewItem.DiffCallback()

        assertTrue(diffCallback.areItemsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.msgVerses[0].text.text, 0)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 1), MockContents.kjvVerses[1].text.text, 0)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.msgVerses[0].text.text, 0)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 1), MockContents.kjvVerses[1].text.text, 0)
        ))

        assertTrue(diffCallback.areItemsTheSame(
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.msgVerses[0].text.text, "", 0)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 1), MockContents.kjvVerses[1].text.text, "", 0)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.msgVerses[0].text.text, "", 0)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "", 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 1), MockContents.kjvVerses[1].text.text, "", 0)
        ))

        assertFalse(diffCallback.areItemsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[1].text.text, "", 0)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[1].text.text, "", 0),
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0),
            PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[1].text.text, "", 0)
        ))
    }

    @Test
    fun `test viewType`() {
        assertEquals(R.layout.item_preview_verse, PreviewItem.Verse(Settings.DEFAULT, VerseIndex.INVALID, "", 0).viewType)
        assertEquals(R.layout.item_preview_verse_with_query, PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex.INVALID, "", "", 0).viewType)
    }

    @Test
    fun `test Verse_textForDisplay, followingEmptyVerseCount is 0`() {
        val actual = PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, 0).textForDisplay
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual.toString())

        with((actual as Spanned).getSpans<Any>(0, 2)) {
            assertEquals(2, size)
            assertEquals(0.85F, (get(0) as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (get(1) as StyleSpan).style)
        }
        assertTrue(actual.getSpans<Any>(3).isEmpty())
    }

    @Test
    fun `test Verse_textForDisplay, followingEmptyVerseCount is 1`() {
        val actual = PreviewItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.msgVerses[0].text.text, 1).textForDisplay
        assertEquals(
            "1:1-2 First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.",
            actual.toString()
        )

        with((actual as Spanned).getSpans<Any>(0, 4)) {
            assertEquals(2, size)
            assertEquals(0.85F, (get(0) as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (get(1) as StyleSpan).style)
        }
        assertTrue(actual.getSpans<Any>(5).isEmpty())
    }

    @Test
    fun `test VerseWithQuery_textForDisplay, followingEmptyVerseCount is 0`() {
        val actual = PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.kjvVerses[0].text.text, "God", 0).textForDisplay
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual.toString())

        with((actual as Spanned).getSpans<Any>(0, 2)) {
            assertEquals(2, size)
            assertEquals(0.85F, (get(0) as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (get(1) as StyleSpan).style)
        }

        with(actual.getSpans<Any>(21, 23)) {
            assertEquals(2, size)
            assertEquals(1.2F, (get(0) as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (get(1) as StyleSpan).style)
        }

        assertEquals(4, actual.getSpans<Any>().size)
    }

    @Test
    fun `test VerseWithQuery_textForDisplay, followingEmptyVerseCount is 1`() {
        val actual = PreviewItem.VerseWithQuery(Settings.DEFAULT, VerseIndex(0, 0, 0), MockContents.msgVerses[0].text.text, "God", 1).textForDisplay
        assertEquals(
            "1:1-2 First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.",
            actual.toString()
        )

        with((actual as Spanned).getSpans<Any>(0, 4)) {
            assertEquals(2, size)
            assertEquals(0.85F, (get(0) as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (get(1) as StyleSpan).style)
        }

        with(actual.getSpans<Any>(18, 20)) {
            assertEquals(2, size)
            assertEquals(1.2F, (get(0) as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (get(1) as StyleSpan).style)
        }

        assertEquals(6, actual.getSpans<Any>().size)
    }
}
