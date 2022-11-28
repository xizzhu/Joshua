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

package me.xizzhu.android.joshua.strongnumber

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class StrongNumberItemTest : BaseUnitTest() {
    fun `test DiffCallback`() {
        val diffCallback = StrongNumberItem.DiffCallback()

        assertTrue(diffCallback.areItemsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.StrongNumber(Settings.DEFAULT, "")
        ))
        assertTrue(diffCallback.areContentsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.StrongNumber(Settings.DEFAULT, "")
        ))
        assertTrue(diffCallback.areItemsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.StrongNumber(Settings.DEFAULT, "other")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.StrongNumber(Settings.DEFAULT, "other")
        ))

        assertTrue(diffCallback.areItemsTheSame(
            StrongNumberItem.BookName(Settings.DEFAULT, ""),
            StrongNumberItem.BookName(Settings.DEFAULT, "")
        ))
        assertTrue(diffCallback.areContentsTheSame(
            StrongNumberItem.BookName(Settings.DEFAULT, ""),
            StrongNumberItem.BookName(Settings.DEFAULT, "")
        ))
        assertTrue(diffCallback.areItemsTheSame(
            StrongNumberItem.BookName(Settings.DEFAULT, ""),
            StrongNumberItem.BookName(Settings.DEFAULT, "other")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            StrongNumberItem.BookName(Settings.DEFAULT, ""),
            StrongNumberItem.BookName(Settings.DEFAULT, "other")
        ))

        assertTrue(diffCallback.areItemsTheSame(
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "")
        ))
        assertTrue(diffCallback.areContentsTheSame(
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "")
        ))
        assertTrue(diffCallback.areItemsTheSame(
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "")
        ))

        assertFalse(diffCallback.areItemsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.BookName(Settings.DEFAULT, "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            StrongNumberItem.BookName(Settings.DEFAULT, ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", ""),
            StrongNumberItem.BookName(Settings.DEFAULT, "")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.BookName(Settings.DEFAULT, "")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            StrongNumberItem.BookName(Settings.DEFAULT, ""),
            StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "")
        ))
    }

    @Test
    fun `test viewType`() {
        assertEquals(R.layout.item_text, StrongNumberItem.StrongNumber(Settings.DEFAULT, "").viewType)
        assertEquals(R.layout.item_title, StrongNumberItem.BookName(Settings.DEFAULT, "").viewType)
        assertEquals(R.layout.item_strong_number_verse, StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex.INVALID, "", "").viewType)
    }

    @Test
    fun `test Verse_textForDisplay`() {
        val actual = StrongNumberItem.Verse(
            settings = Settings.DEFAULT,
            verseIndex = VerseIndex(0, 0, 0),
            bookShortName = MockContents.kjvBookShortNames[0],
            verseText = MockContents.kjvVerses[0].text.text
        ).textForDisplay
        assertEquals("Gen. 1:1 In the beginning God created the heaven and the earth.", actual.toString())
        assertEquals(2, (actual as SpannableStringBuilder).getSpans<Any>().size)
        assertEquals(0.85F, actual.getSpans<RelativeSizeSpan>().first().sizeChange)
        assertEquals(Typeface.BOLD, actual.getSpans<StyleSpan>().first().style)
    }
}
