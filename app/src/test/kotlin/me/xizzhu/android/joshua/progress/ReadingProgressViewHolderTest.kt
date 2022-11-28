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

package me.xizzhu.android.joshua.progress

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
class ReadingProgressViewHolderTest : BaseUnitTest() {
    private lateinit var context: Context

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
    }

    @Test
    fun `test Summary`() {
        val viewHolder = ReadingProgressViewHolder.Summary(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        )
        assertEquals("Consecutive Reading", viewHolder.itemView.findViewById<TextView>(R.id.continuous_reading_days_title).text.toString())
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.continuous_reading_days_value).text.isEmpty())
        assertEquals("Chapters Read", viewHolder.itemView.findViewById<TextView>(R.id.chapters_read_title).text.toString())
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.chapters_read_value).text.isEmpty())
        assertEquals("Books Finished", viewHolder.itemView.findViewById<TextView>(R.id.finished_books_title).text.toString())
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.finished_books_value).text.isEmpty())
        assertEquals("Old Testament", viewHolder.itemView.findViewById<TextView>(R.id.finished_old_testament_title).text.toString())
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.finished_old_testament_value).text.isEmpty())
        assertEquals("New Testament", viewHolder.itemView.findViewById<TextView>(R.id.finished_new_testament_title).text.toString())
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.finished_new_testament_value).text.isEmpty())

        viewHolder.bindData(
            item = ReadingProgressItem.Summary(
                settings = Settings.DEFAULT,
                continuousReadingDays = 1,
                chaptersRead = 2,
                finishedBooks = 7,
                finishedOldTestament = 3,
                finishedNewTestament = 4,
            )
        )
        assertEquals("Consecutive Reading", viewHolder.itemView.findViewById<TextView>(R.id.continuous_reading_days_title).text.toString())
        assertEquals("1 day(s)", viewHolder.itemView.findViewById<TextView>(R.id.continuous_reading_days_value).text.toString())
        assertEquals("Chapters Read", viewHolder.itemView.findViewById<TextView>(R.id.chapters_read_title).text.toString())
        assertEquals("2", viewHolder.itemView.findViewById<TextView>(R.id.chapters_read_value).text.toString())
        assertEquals("Books Finished", viewHolder.itemView.findViewById<TextView>(R.id.finished_books_title).text.toString())
        assertEquals("7", viewHolder.itemView.findViewById<TextView>(R.id.finished_books_value).text.toString())
        assertEquals("Old Testament", viewHolder.itemView.findViewById<TextView>(R.id.finished_old_testament_title).text.toString())
        assertEquals("3", viewHolder.itemView.findViewById<TextView>(R.id.finished_old_testament_value).text.toString())
        assertEquals("New Testament", viewHolder.itemView.findViewById<TextView>(R.id.finished_new_testament_title).text.toString())
        assertEquals("4", viewHolder.itemView.findViewById<TextView>(R.id.finished_new_testament_value).text.toString())
    }

    @Test
    fun `test Book, no chapter read, and collapsed`() {
        var expandedOrCollapseBookCalled = 0
        val viewHolder = ReadingProgressViewHolder.Book(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is ReadingProgressAdapter.ViewEvent.ExpandOrCollapseBook -> {
                    assertEquals(0, viewEvent.bookIndex)
                    expandedOrCollapseBookCalled++
                }
                is ReadingProgressAdapter.ViewEvent.OpenVerse -> fail()
            }
        }
        assertEquals(0, expandedOrCollapseBookCalled)
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.book_name).text.isEmpty())
        assertEquals(0, viewHolder.itemView.findViewById<ReadingProgressBar>(R.id.reading_progress_bar).progress)
        assertTrue(viewHolder.itemView.findViewById<LinearLayout>(R.id.chapters).isVisible)

        viewHolder.itemView.performClick()
        assertEquals(0, expandedOrCollapseBookCalled)

        viewHolder.bindData(
            item = ReadingProgressItem.Book(
                settings = Settings.DEFAULT,
                bookName = MockContents.kjvBookNames[0],
                bookIndex = 0,
                chaptersRead = ArrayList<Boolean>().apply { repeat(Bible.getChapterCount(0)) { add(false) } },
                chaptersReadCount = 0,
                expanded = false,
            )
        )
        assertEquals("Genesis", viewHolder.itemView.findViewById<TextView>(R.id.book_name).text.toString())
        assertEquals(0, viewHolder.itemView.findViewById<ReadingProgressBar>(R.id.reading_progress_bar).progress)
        assertFalse(viewHolder.itemView.findViewById<LinearLayout>(R.id.chapters).isVisible)

        viewHolder.itemView.performClick()
        assertEquals(1, expandedOrCollapseBookCalled)
    }

    @Test
    fun `test Book, two chapter read, and expanded`() {
        var expandedOrCollapseBookCalled = 0
        var openVerseCalled = 0
        val viewHolder = ReadingProgressViewHolder.Book(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is ReadingProgressAdapter.ViewEvent.ExpandOrCollapseBook -> {
                    assertEquals(2, viewEvent.bookIndex)
                    expandedOrCollapseBookCalled++
                }
                is ReadingProgressAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(2, 7, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
            }
        }
        assertEquals(0, expandedOrCollapseBookCalled)
        assertEquals(0, openVerseCalled)
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.book_name).text.isEmpty())
        assertEquals(0, viewHolder.itemView.findViewById<ReadingProgressBar>(R.id.reading_progress_bar).progress)
        assertTrue(viewHolder.itemView.findViewById<LinearLayout>(R.id.chapters).isVisible)

        viewHolder.itemView.performClick()
        assertEquals(0, expandedOrCollapseBookCalled)
        assertEquals(0, openVerseCalled)

        viewHolder.bindData(
            item = ReadingProgressItem.Book(
                settings = Settings.DEFAULT,
                bookName = MockContents.kjvBookNames[2],
                bookIndex = 2,
                chaptersRead = ArrayList<Boolean>().apply { repeat(Bible.getChapterCount(2)) { add(it == 0 || it == 1) } },
                chaptersReadCount = 2,
                expanded = true,
            )
        )
        assertEquals("Leviticus", viewHolder.itemView.findViewById<TextView>(R.id.book_name).text.toString())
        assertEquals(7, viewHolder.itemView.findViewById<ReadingProgressBar>(R.id.reading_progress_bar).progress)
        with(viewHolder.itemView.findViewById<LinearLayout>(R.id.chapters)) {
            assertTrue(isVisible)
            assertEquals(6, childCount)
            repeat(5) { rowIndex ->
                val row = getChildAt(rowIndex) as LinearLayout
                assertEquals(5, row.childCount)
                repeat(5) { columnIndex ->
                    val chapter = row.getChildAt(columnIndex) as TextView
                    assertTrue(chapter.isVisible)
                    assertEquals((rowIndex * 5 + columnIndex + 1).toString(), chapter.text.toString())
                    assertEquals(rowIndex * 5 + columnIndex, chapter.tag)
                }
            }

            val lastRow = getChildAt(5) as LinearLayout
            assertEquals(5, lastRow.childCount)
            repeat(5) { columnIndex ->
                val chapter = lastRow.getChildAt(columnIndex) as TextView
                if (columnIndex < 2) {
                    assertTrue(chapter.isVisible)
                    assertEquals((26 + columnIndex).toString(), chapter.text.toString())
                    assertEquals(25 + columnIndex, chapter.tag)
                } else {
                    assertFalse(chapter.isVisible)
                }
            }

            (getChildAt(1) as LinearLayout).getChildAt(2).performClick()
            assertEquals(1, openVerseCalled)
        }

        viewHolder.itemView.performClick()
        assertEquals(1, expandedOrCollapseBookCalled)
        assertEquals(1, openVerseCalled)
    }

    @Test
    fun `test Book, bind again with less chapters`() {
        val viewHolder = ReadingProgressViewHolder.Book(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) {}

        viewHolder.bindData(
            item = ReadingProgressItem.Book(
                settings = Settings.DEFAULT,
                bookName = MockContents.kjvBookNames[0],
                bookIndex = 0,
                chaptersRead = ArrayList<Boolean>().apply { repeat(Bible.getChapterCount(0)) { add(false) } },
                chaptersReadCount = 0,
                expanded = true,
            )
        )
        assertEquals(10, viewHolder.itemView.findViewById<LinearLayout>(R.id.chapters).childCount)

        viewHolder.bindData(
            item = ReadingProgressItem.Book(
                settings = Settings.DEFAULT,
                bookName = MockContents.kjvBookNames[2],
                bookIndex = 2,
                chaptersRead = ArrayList<Boolean>().apply { repeat(Bible.getChapterCount(2)) { add(false) } },
                chaptersReadCount = 0,
                expanded = true,
            )
        )

        with(viewHolder.itemView.findViewById<LinearLayout>(R.id.chapters)) {
            assertTrue(isVisible)
            assertEquals(6, childCount)
            repeat(5) { rowIndex ->
                val row = getChildAt(rowIndex) as LinearLayout
                assertEquals(5, row.childCount)
                repeat(5) { columnIndex ->
                    val chapter = row.getChildAt(columnIndex) as TextView
                    assertTrue(chapter.isVisible)
                    assertEquals((rowIndex * 5 + columnIndex + 1).toString(), chapter.text.toString())
                    assertEquals(rowIndex * 5 + columnIndex, chapter.tag)
                }
            }

            val lastRow = getChildAt(5) as LinearLayout
            assertEquals(5, lastRow.childCount)
            repeat(5) { columnIndex ->
                val chapter = lastRow.getChildAt(columnIndex) as TextView
                if (columnIndex < 2) {
                    assertTrue(chapter.isVisible)
                    assertEquals((26 + columnIndex).toString(), chapter.text.toString())
                    assertEquals(25 + columnIndex, chapter.tag)
                } else {
                    assertFalse(chapter.isVisible)
                }
            }
        }
    }
}
