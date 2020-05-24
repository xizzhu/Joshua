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

package me.xizzhu.android.joshua.reading.detail

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class VerseDetailPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var lifecycle: Lifecycle

    @Mock
    private lateinit var navigator: Navigator

    @Mock
    private lateinit var readingViewModel: ReadingViewModel

    @Mock
    private lateinit var readingActivity: ReadingActivity

    @Mock
    private lateinit var verseDetailViewLayout: VerseDetailViewLayout

    private lateinit var verseDetailViewHolder: VerseDetailViewHolder
    private lateinit var verseDetailPresenter: VerseDetailPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(readingActivity.lifecycle).thenReturn(lifecycle)

        verseDetailViewHolder = VerseDetailViewHolder(verseDetailViewLayout)
        verseDetailPresenter = VerseDetailPresenter(navigator, readingViewModel, readingActivity, testCoroutineScope)
        verseDetailPresenter.bind(verseDetailViewHolder)
    }

    @Test
    fun testDownloadStrongNumberWithException() = testDispatcher.runBlockingTest {
        verseDetailPresenter.verseDetail = VerseDetail.INVALID
        `when`(readingViewModel.downloadStrongNumber()).thenReturn(flow { throw RuntimeException("random exception") })

        verseDetailPresenter.downloadStrongNumber()
        verify(verseDetailViewLayout, never()).setVerseDetail(any())
    }
}
