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

package me.xizzhu.android.joshua.reading.chapter

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.databinding.ItemBookNameBinding
import me.xizzhu.android.joshua.databinding.ItemChapterRowBinding
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChapterListAdapterTest : BaseUnitTest() {
    @BeforeTest
    override fun setup() {
        super.setup()
        ApplicationProvider.getApplicationContext<Context>().setTheme(R.style.AppTheme)
    }

    @Test
    fun `test without data`() {
        val adapter = ChapterListAdapter(
            context = ApplicationProvider.getApplicationContext(),
            onViewEvent = { fail() },
        )
        assertEquals(0, adapter.groupCount)
    }

    @Test
    fun `test getGroupView()`() {
        val adapter = ChapterListAdapter(
            context = ApplicationProvider.getApplicationContext(),
            onViewEvent = { fail() },
        )
        adapter.setCurrentChapter(currentBookIndex = 0, currentChapterIndex = 0)
        adapter.setItems(
            items = MockContents.kjvBookNames.mapIndexed { index, bookName ->
                ChapterSelectionItem(bookIndex = index, bookName = bookName, chapterCount = Bible.getChapterCount(index))
            }
        )
        assertEquals(Bible.BOOK_COUNT, adapter.groupCount)

        val binding = ItemBookNameBinding.bind(adapter.getGroupView(1, false, null, FrameLayout(ApplicationProvider.getApplicationContext())))
        assertEquals("Exodus", binding.root.text.toString())

        assertEquals(
            "Genesis",
            ItemBookNameBinding.bind(adapter.getGroupView(0, true, binding.root, FrameLayout(ApplicationProvider.getApplicationContext()))).root.text.toString()
        )
    }

    @Test
    fun `test getChildView()`() {
        var selectChapterCalled = 0
        var bookToSelect = -1
        var chapterToSelect = -1
        val adapter = ChapterListAdapter(context = ApplicationProvider.getApplicationContext()) { viewEvent ->
            when (viewEvent) {
                is ChapterSelectionView.ViewEvent.SelectChapter -> {
                    assertEquals(bookToSelect, viewEvent.bookIndex)
                    if (viewEvent.bookIndex == 2 && viewEvent.chapterIndex == 0) {
                        fail()
                    }
                    assertEquals(chapterToSelect, viewEvent.chapterIndex)
                    selectChapterCalled++
                }
            }
        }
        adapter.setCurrentChapter(currentBookIndex = 2, currentChapterIndex = 0)
        adapter.setItems(
            items = MockContents.kjvBookNames.mapIndexed { index, bookName ->
                ChapterSelectionItem(bookIndex = index, bookName = bookName, chapterCount = Bible.getChapterCount(index))
            }
        )
        assertEquals(Bible.BOOK_COUNT, adapter.groupCount)
        assertEquals(10, adapter.getChildrenCount(0))
        assertEquals(6, adapter.getChildrenCount(2))

        val firstRow = ItemChapterRowBinding.bind(adapter.getChildView(2, 0, false, null, FrameLayout(ApplicationProvider.getApplicationContext())))
        assertEquals(5, firstRow.root.childCount)
        firstRow.root.children.forEachIndexed { index, child ->
            assertTrue(child.isVisible)
            assertEquals(index == 0, child.isSelected)
            assertEquals((index + 1).toString(), (child as TextView).text.toString())

            bookToSelect = 2
            chapterToSelect = index
            child.performClick()
            assertEquals(index, selectChapterCalled)
        }

        val lastRow = ItemChapterRowBinding.bind(adapter.getChildView(2, 5, false, firstRow.root, FrameLayout(ApplicationProvider.getApplicationContext())))
        assertEquals(5, lastRow.root.childCount)
        lastRow.root.children.forEachIndexed { index, child ->
            assertEquals(index <= 1, child.isVisible)
            if (index <= 1) {
                assertFalse(child.isSelected)
                assertEquals((index + 26).toString(), (child as TextView).text.toString())

                bookToSelect = 2
                chapterToSelect = index + 25
                child.performClick()
                assertEquals(index + 5, selectChapterCalled)
            }
        }

        val firstRowOfAnotherBook = ItemChapterRowBinding.bind(adapter.getChildView(0, 0, false, null, FrameLayout(ApplicationProvider.getApplicationContext())))
        assertEquals(5, firstRowOfAnotherBook.root.childCount)
        firstRowOfAnotherBook.root.children.forEachIndexed { index, child ->
            assertTrue(child.isVisible)
            assertFalse(child.isSelected)
            assertEquals((index + 1).toString(), (child as TextView).text.toString())

            bookToSelect = 0
            chapterToSelect = index
            child.performClick()
            assertEquals(index + 7, selectChapterCalled)
        }
    }
}
