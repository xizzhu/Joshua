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

package me.xizzhu.android.joshua.search

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
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
class SearchItemTest : BaseUnitTest() {
    @Test
    fun `test DiffCallback`() {
        val diffCallback = SearchItem.DiffCallback()

        assertTrue(diffCallback.areItemsTheSame(
            SearchItem.Header(Settings.DEFAULT, ""),
            SearchItem.Header(Settings.DEFAULT, "")
        ))
        assertTrue(diffCallback.areContentsTheSame(
            SearchItem.Header(Settings.DEFAULT, ""),
            SearchItem.Header(Settings.DEFAULT, "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            SearchItem.Header(Settings.DEFAULT, ""),
            SearchItem.Header(Settings.DEFAULT, "other")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            SearchItem.Header(Settings.DEFAULT, ""),
            SearchItem.Header(Settings.DEFAULT, "other")
        ))

        assertTrue(diffCallback.areItemsTheSame(
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", ""),
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", "")
        ))
        assertTrue(diffCallback.areContentsTheSame(
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", ""),
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", "")
        ))
        assertTrue(diffCallback.areItemsTheSame(
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", ""),
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", ""),
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "", "", "")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", ""),
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "", "", "")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", ""),
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", "")
        ))

        assertTrue(diffCallback.areItemsTheSame(
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", Highlight.COLOR_NONE)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "", "", Highlight.COLOR_NONE)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "", "", Highlight.COLOR_NONE)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", Highlight.COLOR_NONE)
        ))

        assertFalse(diffCallback.areItemsTheSame(
            SearchItem.Header(Settings.DEFAULT, ""),
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            SearchItem.Header(Settings.DEFAULT, ""),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            SearchItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", ""),
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            SearchItem.Verse(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Highlight.COLOR_NONE),
            SearchItem.Header(Settings.DEFAULT, "")
        ))
    }

    @Test
    fun `test viewType`() {
        assertEquals(R.layout.item_title, SearchItem.Header(Settings.DEFAULT, "").viewType)
        assertEquals(R.layout.item_search_note, SearchItem.Note(Settings.DEFAULT, VerseIndex.INVALID, "", "", "", "").viewType)
        assertEquals(R.layout.item_search_verse, SearchItem.Verse(Settings.DEFAULT, VerseIndex.INVALID, "", "", "", Highlight.COLOR_NONE).viewType)
    }

    @Test
    fun `test Note`() {
        val actual = SearchItem.Note(
            settings = Settings.DEFAULT,
            verseIndex = VerseIndex(0, 0, 0),
            bookShortName = MockContents.kjvBookShortNames[0],
            verseText = MockContents.kjvVerses[0].text.text,
            query = "note",
            note = "just a note"
        )
        assertEquals("Gen. 1:1 In the beginning God created the heaven and the earth.", actual.verseForDisplay.toString())
        assertEquals("just a note", actual.noteForDisplay.toString())
    }

    @Test
    fun `test Verse`() {
        val actual = SearchItem.Verse(
            settings = Settings.DEFAULT,
            verseIndex = VerseIndex(0, 0, 0),
            bookShortName = "Gen.",
            verseText = "In the beginning God created the heaven and the earth.",
            query = "query",
            highlightColor = Highlight.COLOR_NONE
        )
        assertEquals("Gen. 1:1\nIn the beginning God created the heaven and the earth.", actual.textForDisplay.toString())
    }
}
