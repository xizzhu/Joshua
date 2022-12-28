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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
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
class AnnotatedVerseViewHolderTest : BaseUnitTest() {
    private lateinit var context: Context

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
    }

    @Test
    fun `test Header`() {
        val viewHolder = AnnotatedVerseViewHolder.Header(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        )
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.title).text.isEmpty())

        viewHolder.bindData(
            item = AnnotatedVerseItem.Header(
                settings = Settings.DEFAULT,
                text = "random text",
                hideDivider = false,
            )
        )
        assertTrue(viewHolder.itemView.findViewById<View>(R.id.divider).isVisible)
        assertEquals("random text", viewHolder.itemView.findViewById<TextView>(R.id.title).text.toString())
    }

    @Test
    fun `test Bookmark`() {
        var openVerseCalled = 0
        var showPreviewCalled = 0
        val viewHolder = AnnotatedVerseViewHolder.Bookmark(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is AnnotatedVerseAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
                is AnnotatedVerseAdapter.ViewEvent.ShowPreview -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToPreview)
                    showPreviewCalled++
                }
            }
        }
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.text).text.isEmpty())
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        viewHolder.itemView.performLongClick()
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.bindData(
            item = AnnotatedVerseItem.Bookmark(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookNames[0],
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                Constants.SORT_BY_DATE
            )
        )
        assertEquals(
            "Genesis 1:1\nIn the beginning God created the heaven and the earth.",
            viewHolder.itemView.findViewById<TextView>(R.id.text).text.toString()
        )
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        assertEquals(1, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(1, openVerseCalled)
        assertEquals(1, showPreviewCalled)
    }

    @Test
    fun `test Highlight`() {
        var openVerseCalled = 0
        var showPreviewCalled = 0
        val viewHolder = AnnotatedVerseViewHolder.Highlight(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is AnnotatedVerseAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
                is AnnotatedVerseAdapter.ViewEvent.ShowPreview -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToPreview)
                    showPreviewCalled++
                }
            }
        }
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.text).text.isEmpty())
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        viewHolder.itemView.performLongClick()
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.bindData(
            item = AnnotatedVerseItem.Highlight(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookNames[0],
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                Highlight.COLOR_BLUE,
                Constants.SORT_BY_DATE
            )
        )
        assertEquals(
            "Genesis 1:1\nIn the beginning God created the heaven and the earth.",
            viewHolder.itemView.findViewById<TextView>(R.id.text).text.toString()
        )
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        assertEquals(1, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(1, openVerseCalled)
        assertEquals(1, showPreviewCalled)
    }

    @Test
    fun `test Note`() {
        var openVerseCalled = 0
        var showPreviewCalled = 0
        val viewHolder = AnnotatedVerseViewHolder.Note(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is AnnotatedVerseAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
                is AnnotatedVerseAdapter.ViewEvent.ShowPreview -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToPreview)
                    showPreviewCalled++
                }
            }
        }
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.text).text.isEmpty())
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        viewHolder.itemView.performLongClick()
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.bindData(
            item = AnnotatedVerseItem.Note(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                "a note"
            )
        )
        assertEquals(
            "Gen. 1:1 In the beginning God created the heaven and the earth.",
            viewHolder.itemView.findViewById<TextView>(R.id.verse).text.toString()
        )
        assertEquals("a note", viewHolder.itemView.findViewById<TextView>(R.id.text).text.toString())
        assertEquals(0, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performClick()
        assertEquals(1, openVerseCalled)
        assertEquals(0, showPreviewCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(1, openVerseCalled)
        assertEquals(1, showPreviewCalled)
    }
}
