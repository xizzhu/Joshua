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

package me.xizzhu.android.joshua.annotated

import me.xizzhu.android.joshua.core.Constants
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
class AnnotatedVerseItemTest : BaseUnitTest() {
    @Test
    fun `test DiffCallback`() {
        val diffCallback = AnnotatedVerseItem.DiffCallback()

        assertTrue(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Header(Settings.DEFAULT, ""),
            AnnotatedVerseItem.Header(Settings.DEFAULT, "")
        ))
        assertTrue(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Header(Settings.DEFAULT, ""),
            AnnotatedVerseItem.Header(Settings.DEFAULT, "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Header(Settings.DEFAULT, ""),
            AnnotatedVerseItem.Header(Settings.DEFAULT, "other")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Header(Settings.DEFAULT, ""),
            AnnotatedVerseItem.Header(Settings.DEFAULT, "other")
        ))

        assertTrue(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", Constants.DEFAULT_SORT_ORDER)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "", "", Constants.DEFAULT_SORT_ORDER)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", Constants.DEFAULT_SORT_ORDER)
        ))

        assertTrue(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", 0, Constants.DEFAULT_SORT_ORDER)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "", "", 0, Constants.DEFAULT_SORT_ORDER)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "", 0, Constants.DEFAULT_SORT_ORDER)
        ))

        assertTrue(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", ""),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "")
        ))
        assertTrue(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", ""),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "")
        ))
        assertTrue(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", ""),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", ""),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 1), "", "", "")
        ))
        assertFalse(diffCallback.areContentsTheSame(
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", ""),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "other", "", "")
        ))

        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Header(Settings.DEFAULT, ""),
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Header(Settings.DEFAULT, ""),
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Header(Settings.DEFAULT, ""),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Bookmark(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Highlight(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "", 0, Constants.DEFAULT_SORT_ORDER),
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", "")
        ))
        assertFalse(diffCallback.areItemsTheSame(
            AnnotatedVerseItem.Note(Settings.DEFAULT, VerseIndex(0, 0, 0), "", "", ""),
            AnnotatedVerseItem.Header(Settings.DEFAULT, "")
        ))
    }

    @Test
    fun `test Bookmark_textForDisplay`() {
        assertEquals(
            "Genesis 1:1\nIn the beginning God created the heaven and the earth.",
            AnnotatedVerseItem.Bookmark(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookNames[0],
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                Constants.SORT_BY_DATE
            ).textForDisplay.toString()
        )

        assertEquals(
            "Gen. 1:1 In the beginning God created the heaven and the earth.",
            AnnotatedVerseItem.Bookmark(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookNames[0],
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                Constants.SORT_BY_BOOK
            ).textForDisplay.toString()
        )
    }

    @Test
    fun `test Highlight_textForDisplay`() {
        assertEquals(
            "Genesis 1:1\nIn the beginning God created the heaven and the earth.",
            AnnotatedVerseItem.Highlight(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookNames[0],
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                Highlight.COLOR_NONE,
                Constants.SORT_BY_DATE
            ).textForDisplay.toString()
        )

        assertEquals(
            "Gen. 1:1 In the beginning God created the heaven and the earth.",
            AnnotatedVerseItem.Highlight(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookNames[0],
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                Highlight.COLOR_NONE,
                Constants.SORT_BY_BOOK
            ).textForDisplay.toString()
        )
    }

    @Test
    fun `test Note_textForDisplay`() {
        assertEquals(
            "Gen. 1:1 In the beginning God created the heaven and the earth.",
            AnnotatedVerseItem.Note(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                ""
            ).textForDisplay.toString()
        )
    }
}
