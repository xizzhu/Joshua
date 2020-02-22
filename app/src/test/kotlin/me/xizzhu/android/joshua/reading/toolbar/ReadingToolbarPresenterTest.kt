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

package me.xizzhu.android.joshua.reading.toolbar

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class ReadingToolbarPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingActivity: ReadingActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var readingViewModel: ReadingViewModel
    @Mock
    private lateinit var readingToolbarInteractor: ReadingToolbarInteractor
    @Mock
    private lateinit var readingToolbar: ReadingToolbar

    private lateinit var readingToolbarPresenter: ReadingToolbarPresenter
    private lateinit var readingToolbarViewHolder: ReadingToolbarViewHolder

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(readingViewModel.downloadedTranslations()).thenReturn(emptyFlow())
        `when`(readingViewModel.currentTranslation()).thenReturn(emptyFlow())
        `when`(readingViewModel.parallelTranslations()).thenReturn(emptyFlow())
        `when`(readingViewModel.currentVerseIndex()).thenReturn(emptyFlow())
        `when`(readingViewModel.bookShortNames()).thenReturn(emptyFlow())

        readingToolbarViewHolder = ReadingToolbarViewHolder(readingToolbar)
        readingToolbarPresenter = ReadingToolbarPresenter(readingActivity, navigator, readingViewModel, readingToolbarInteractor, testDispatcher)
    }

    @Test
    fun testObserveDownloadedTranslations() = testDispatcher.runBlockingTest {
        `when`(readingViewModel.downloadedTranslations()).thenReturn(flowOf(ViewData.success(listOf(MockContents.kjvTranslationInfo))))
        `when`(readingActivity.getString(R.string.menu_more_translation)).thenReturn("More")

        readingToolbarPresenter.create(readingToolbarViewHolder)
        verify(readingToolbar, times(1)).setTranslationShortNames(listOf(MockContents.kjvTranslationInfo.shortName, "More"))
        verify(readingToolbar, times(1)).setSpinnerSelection(0)

        readingToolbarPresenter.destroy()
    }

    @Test
    fun testObserveCurrentTranslation() = testDispatcher.runBlockingTest {
        `when`(readingViewModel.currentTranslation()).thenReturn(flowOf(ViewData.success(MockContents.kjvShortName)))

        readingToolbarPresenter.create(readingToolbarViewHolder)
        verify(readingToolbar, times(1)).setCurrentTranslation(MockContents.kjvShortName)
        verify(readingToolbar, times(1)).setSpinnerSelection(0)

        readingToolbarPresenter.destroy()
    }

    @Test
    fun testObserveBookNames() = testDispatcher.runBlockingTest {
        `when`(readingViewModel.currentVerseIndex()).thenReturn(flowOf(ViewData.success(VerseIndex(1, 2, 3))))
        `when`(readingViewModel.bookShortNames()).thenReturn(flowOf(ViewData.success(MockContents.kjvBookShortNames)))

        readingToolbarPresenter.create(readingToolbarViewHolder)
        verify(readingToolbar, times(1)).title = "${MockContents.kjvBookShortNames[1]}, 3"

        readingToolbarPresenter.destroy()
    }
}
