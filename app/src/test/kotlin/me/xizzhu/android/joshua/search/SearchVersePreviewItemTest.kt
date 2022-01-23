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
import kotlin.test.assertEquals
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class SearchVersePreviewItemTest : BaseUnitTest() {
    @Test
    fun `test viewType`() {
        assertEquals(R.layout.item_search_verse_preview, SearchVersePreviewItem(Verse.INVALID, "", 0).viewType)
    }

    @Test
    fun `test textForDisplay without followingEmptyVerseCount`() {
        val actual = SearchVersePreviewItem(MockContents.kjvVerses[0], "God", 0).textForDisplay
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual.toString())

        if (actual is Spanned) {
            val titleSpans = actual.getSpans<Any>(0, 2)
            assertEquals(2, titleSpans.size)
            assertEquals(0.85F, (titleSpans[0] as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (titleSpans[1] as StyleSpan).style)

            val querySpans = actual.getSpans<Any>(21, 23)
            assertEquals(2, querySpans.size)
            assertEquals(1.2F, (querySpans[0] as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (querySpans[1] as StyleSpan).style)

            assertEquals(4, actual.getSpans<Any>().size)
        }
    }

    @Test
    fun `test textForDisplay with followingEmptyVerseCount`() {
        val actual = SearchVersePreviewItem(MockContents.msgVerses[0], "God", 1).textForDisplay
        assertEquals(
                "1:1-2 First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.",
                actual.toString()
        )

        if (actual is Spanned) {
            val titleSpans = actual.getSpans<Any>(0, 4)
            assertEquals(2, titleSpans.size)
            assertEquals(0.85F, (titleSpans[0] as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (titleSpans[1] as StyleSpan).style)

            val querySpans = actual.getSpans<Any>(18, 20)
            assertEquals(2, querySpans.size)
            assertEquals(1.2F, (querySpans[0] as RelativeSizeSpan).sizeChange)
            assertEquals(Typeface.BOLD, (querySpans[1] as StyleSpan).style)

            assertEquals(6, actual.getSpans<Any>().size)
        }
    }

    @Test
    fun `test toSearchVersePreviewItems() with single verse`() {
        val actual = listOf(MockContents.kjvVerses[0]).toSearchVersePreviewItems("God")
        assertEquals(1, actual.size)
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual[0].textForDisplay.toString())
    }

    @Test
    fun `test toSearchVersePreviewItems() with multiple verses`() {
        val actual = listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1]).toSearchVersePreviewItems("God")
        assertEquals(2, actual.size)
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual[0].textForDisplay.toString())
        assertEquals(
                "1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
                actual[1].textForDisplay.toString()
        )
    }

    @Test
    fun `test toSearchVersePreviewItems() with multiple verses but not consecutive`() {
        val actual = listOf(MockContents.msgVerses[0], MockContents.msgVerses[1], MockContents.msgVerses[2]).toSearchVersePreviewItems("God")
        assertEquals(2, actual.size)
        assertEquals(
                "1:1-2 First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.",
                actual[0].textForDisplay.toString()
        )
        assertEquals(
                "1:3 God spoke: \"Light!\"\nAnd light appeared.\nGod saw that light was good\nand separated light from dark.\nGod named the light Day,\nhe named the dark Night.\nIt was evening, it was morning—\nDay One.",
                actual[1].textForDisplay.toString()
        )
    }
}
