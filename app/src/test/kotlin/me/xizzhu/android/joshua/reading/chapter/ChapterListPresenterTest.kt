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

package me.xizzhu.android.joshua.reading.chapter

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ChapterListViewData
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class ChapterListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var lifecycle: Lifecycle
    @Mock
    private lateinit var readingViewModel: ReadingViewModel
    @Mock
    private lateinit var readingActivity: ReadingActivity
    @Mock
    private lateinit var readingDrawerLayout: ReadingDrawerLayout
    @Mock
    private lateinit var chapterListView: ChapterListView

    private lateinit var chapterListViewHolder: ChapterListViewHolder
    private lateinit var chapterListPresenter: ChapterListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(readingActivity.lifecycle).thenReturn(lifecycle)

        chapterListViewHolder = ChapterListViewHolder(readingDrawerLayout, chapterListView)
        chapterListPresenter = ChapterListPresenter(readingViewModel, readingActivity, testCoroutineScope)
        chapterListPresenter.bind(chapterListViewHolder)
    }

    @Test
    fun testLoadReadingProgress() = testDispatcher.runBlockingTest {
        `when`(readingViewModel.chapterList()).thenReturn(flowOf(
                ChapterListViewData(VerseIndex(0, 0, 0), MockContents.kjvBookNames)
        ))

        chapterListPresenter.observeChapterListViewData()

        with(inOrder(readingDrawerLayout, chapterListView)) {
            // success
            verify(chapterListView, times(1))
                    .setData(VerseIndex(0, 0, 0), MockContents.kjvBookNames)
            verify(readingDrawerLayout, times(1)).hide()
        }
    }

    @Test
    fun testLoadReadingProgressWithException() = testDispatcher.runBlockingTest {
        `when`(readingViewModel.chapterList()).thenReturn(flow { throw RuntimeException() })

        chapterListPresenter.observeChapterListViewData()
        verify(chapterListView, never()).setData(any(), any())
        verify(readingDrawerLayout, never()).hide()
    }
}
