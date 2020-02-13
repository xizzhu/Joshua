/*
 * Copyright (C) 2020 Xizhi Zhu
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

import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class ReadingProgressPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingProgressActivity: ReadingProgressActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var readingProgressInteractor: ReadingProgressInteractor
    @Mock
    private lateinit var loadingSpinner: ProgressBar
    @Mock
    private lateinit var readingProgressListView: CommonRecyclerView

    private lateinit var readingProgressViewHolder: ReadingProgressViewHolder
    private lateinit var readingProgressPresenter: ReadingProgressPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        runBlocking {
            `when`(readingProgressInteractor.settings()).thenReturn(emptyFlow())
            `when`(readingProgressInteractor.bookNames()).thenReturn(List(Bible.BOOK_COUNT) { i -> i.toString() })
            `when`(readingProgressInteractor.readingProgress()).thenReturn(ReadingProgress(0, 0L, emptyList()))

            readingProgressViewHolder = ReadingProgressViewHolder(loadingSpinner, readingProgressListView)
            readingProgressPresenter = ReadingProgressPresenter(readingProgressActivity, navigator, readingProgressInteractor, testDispatcher)
        }
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true, true)
        `when`(readingProgressInteractor.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.success(settings), ViewData.error()))

        readingProgressPresenter.create(readingProgressViewHolder)
        verify(readingProgressListView, times(1)).setSettings(settings)
        verify(readingProgressListView, never()).setSettings(Settings.DEFAULT)

        readingProgressPresenter.destroy()
    }

    @Test
    fun testLoadReadingProgress() = testDispatcher.runBlockingTest {
        `when`(readingProgressInteractor.readingProgress()).thenReturn(ReadingProgress(5, 4321L, emptyList()))

        readingProgressPresenter.create(readingProgressViewHolder)
        readingProgressPresenter.start()

        with(inOrder(loadingSpinner, readingProgressListView)) {
            verify(loadingSpinner, times(1)).fadeIn()
            verify(readingProgressListView, times(1)).visibility = View.GONE
            verify(readingProgressListView, times(1)).setItems(any())
            verify(readingProgressListView, times(1)).fadeIn()
            verify(loadingSpinner, times(1)).visibility = View.GONE
        }

        readingProgressPresenter.stop()
        readingProgressPresenter.destroy()
    }

    @Test
    fun testLoadReadingProgressWithBookNamesException() = testDispatcher.runBlockingTest {
        val exception = RuntimeException("Random exception")
        `when`(readingProgressInteractor.bookNames()).thenThrow(exception)

        readingProgressPresenter.create(readingProgressViewHolder)
        readingProgressPresenter.start()

        with(inOrder(loadingSpinner, readingProgressListView)) {
            verify(loadingSpinner, times(1)).fadeIn()
            verify(readingProgressListView, times(1)).visibility = View.GONE
            verify(loadingSpinner, times(1)).visibility = View.GONE
        }
        verify(readingProgressListView, never()).setItems(any())
        verify(readingProgressListView, never()).fadeIn()

        readingProgressPresenter.stop()
        readingProgressPresenter.destroy()
    }

    @Test
    fun testOpenChapter() = testDispatcher.runBlockingTest {
        readingProgressPresenter.create(readingProgressViewHolder)

        val bookIndex = 1
        val chapterIndex = 2
        readingProgressPresenter.openChapter(bookIndex, chapterIndex)
        verify(readingProgressInteractor, times(1)).saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
        verify(navigator, times(1)).navigate(readingProgressActivity, Navigator.SCREEN_READING)

        readingProgressPresenter.destroy()
    }

    @Test
    fun testToItems() {
        val readingProgress = ReadingProgress(1, 23456L,
                listOf(ReadingProgress.ChapterReadingStatus(0, 1, 1, 500L, 23456L),
                        ReadingProgress.ChapterReadingStatus(0, 2, 2, 600L, 23457L),
                        ReadingProgress.ChapterReadingStatus(2, 0, 3, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 0, 1, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 1, 2, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 2, 3, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 3, 4, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(63, 0, 1, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(64, 0, 1, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(65, 0, 0, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(65, 1, 2, 700L, 23458L)))

        val expected = mutableListOf<BaseItem>(ReadingProgressSummaryItem(1, 10, 3, 1, 2))
                .apply {
                    addAll(Array(Bible.BOOK_COUNT) { bookIndex ->
                        ReadingProgressDetailItem("bookName$bookIndex", bookIndex,
                                Array(Bible.getChapterCount(bookIndex)) { chapterIndex ->
                                    when (bookIndex) {
                                        0 -> chapterIndex == 1 || chapterIndex == 2
                                        2 -> chapterIndex == 0
                                        7 -> chapterIndex == 0 || chapterIndex == 1 || chapterIndex == 2 || chapterIndex == 3
                                        63, 64 -> chapterIndex == 0
                                        65 -> chapterIndex == 1
                                        else -> false
                                    }
                                }.toList(),
                                when (bookIndex) {
                                    0 -> 2
                                    2, 63, 64, 65 -> 1
                                    7 -> 4
                                    else -> 0
                                },
                                readingProgressPresenter::onBookClicked, readingProgressPresenter::openChapter,
                                readingProgressPresenter.expanded[bookIndex])
                    }.toList())
                }
        val actual = with(readingProgressPresenter) { readingProgress.toItems(Array(Bible.BOOK_COUNT) { "bookName$it" }.toList()) }
        assertEquals(expected, actual)
    }

    @Test
    fun testOpenChapterWithException() = testDispatcher.runBlockingTest {
        `when`(readingProgressInteractor.saveCurrentVerseIndex(any())).thenThrow(RuntimeException("Random exception"))

        readingProgressPresenter.create(readingProgressViewHolder)
        readingProgressPresenter.openChapter(0, 0)
        readingProgressPresenter.destroy()
    }
}
