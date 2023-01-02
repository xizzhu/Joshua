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

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
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
class SearchViewHolderTest : BaseUnitTest() {
    private lateinit var context: Context

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
    }

    @Test
    fun `test Header`() {
        val viewHolder = SearchViewHolder.Header(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        )
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.title).text.isEmpty())

        viewHolder.bindData(
            item = SearchItem.Header(
                settings = Settings.DEFAULT,
                text = context.getString(R.string.title_bookmarks)
            )
        )
        assertEquals("Bookmarks", viewHolder.itemView.findViewById<TextView>(R.id.title).text.toString())
    }

    @Test
    fun `test Note`() {
        var openVerseCalled = 0
        var showPreviewCalled = 0
        val viewHolder = SearchViewHolder.Note(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is SearchAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
                is SearchAdapter.ViewEvent.ShowPreview -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToPreview)
                    showPreviewCalled++
                }
            }
        }
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.verse).text.isEmpty())
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.note).text.isEmpty())
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        viewHolder.itemView.performLongClick()
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.bindData(
            item = SearchItem.Note(
                settings = Settings.DEFAULT,
                verseIndex = VerseIndex(0, 0, 0),
                bookShortName = MockContents.kjvBookShortNames[0],
                verseText = MockContents.kjvVerses[0].text.text,
                query = "note",
                note = "just a note"
            )
        )
        assertEquals(
            "Gen. 1:1 In the beginning God created the heaven and the earth.",
            viewHolder.itemView.findViewById<TextView>(R.id.verse).text.toString()
        )
        assertEquals(
            "just a note",
            viewHolder.itemView.findViewById<TextView>(R.id.note).text.toString()
        )

        viewHolder.itemView.performClick()
        assertEquals(1, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(1, openVerseCalled)
        assertEquals(1, showPreviewCalled)
    }

    @Test
    fun `test Verse`() {
        var openVerseCalled = 0
        var showPreviewCalled = 0
        val viewHolder = SearchViewHolder.Verse(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is SearchAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
                is SearchAdapter.ViewEvent.ShowPreview -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToPreview)
                    showPreviewCalled++
                }
            }
        }
        assertTrue((viewHolder.itemView as TextView).text.isEmpty())
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        viewHolder.itemView.performLongClick()
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.bindData(
            item = SearchItem.Verse(
                settings = Settings.DEFAULT,
                verseIndex = VerseIndex(0, 0, 0),
                bookShortName = MockContents.kjvBookShortNames[0],
                verseText = MockContents.kjvVerses[0].text.text,
                query = "",
                highlightColor = Highlight.COLOR_NONE
            )
        )
        assertEquals(
            "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
            (viewHolder.itemView as TextView).text.toString()
        )

        viewHolder.itemView.performClick()
        assertEquals(1, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(1, openVerseCalled)
        assertEquals(1, showPreviewCalled)
    }
}
