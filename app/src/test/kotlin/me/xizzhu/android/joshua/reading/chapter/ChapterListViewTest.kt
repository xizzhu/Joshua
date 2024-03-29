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
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChapterListViewTest : BaseUnitTest() {
    @BeforeTest
    override fun setup() {
        super.setup()
        ApplicationProvider.getApplicationContext<Context>().setTheme(R.style.AppTheme)
    }

    @Test
    fun `test expand and collapse group`() {
        val chapterListView = ChapterListView(ApplicationProvider.getApplicationContext())
        chapterListView.initialize { fail() }
        chapterListView.setCurrentChapter(currentBookIndex = 0, currentChapterIndex = 0)
        chapterListView.setChapterSelectionItems(
            chapterSelectionItems = MockContents.kjvBookNames.mapIndexed { index, bookName ->
                ChapterSelectionItem(bookIndex = index, bookName = bookName, chapterCount = Bible.getChapterCount(index))
            }
        )
        chapterListView.expandCurrentBook()
        repeat(Bible.BOOK_COUNT) { index ->
            assertEquals(index == 0, chapterListView.isGroupExpanded(index))
        }

        chapterListView.performItemClick(mockk(), 0, 0L)
        repeat(Bible.BOOK_COUNT) { index ->
            assertFalse(chapterListView.isGroupExpanded(index))
        }

        chapterListView.performItemClick(mockk(), 1, 0L)
        repeat(Bible.BOOK_COUNT) { index ->
            assertEquals(index == 1, chapterListView.isGroupExpanded(index))
        }

        chapterListView.expandCurrentBook()
        repeat(Bible.BOOK_COUNT) { index ->
            assertEquals(index == 0, chapterListView.isGroupExpanded(index))
        }

        chapterListView.expandCurrentBook()
        repeat(Bible.BOOK_COUNT) { index ->
            assertEquals(index == 0, chapterListView.isGroupExpanded(index))
        }
    }
}
