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

package me.xizzhu.android.joshua.reading.verse

import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class VersePresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var lifecycle: Lifecycle

    @Mock
    private lateinit var readingViewModel: ReadingViewModel

    @Mock
    private lateinit var readingActivity: ReadingActivity

    @Mock
    private lateinit var versePager: ViewPager2

    private lateinit var verseViewHolder: VerseViewHolder
    private lateinit var versePresenter: VersePresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(readingActivity.lifecycle).thenReturn(lifecycle)

        verseViewHolder = VerseViewHolder(versePager)
        versePresenter = VersePresenter(readingViewModel, readingActivity, testCoroutineScope)
        versePresenter.bind(verseViewHolder)
    }

    @Test
    fun testOnHighlightClicked() = testDispatcher.runBlockingTest {
        `when`(readingViewModel.settings()).thenReturn(flowOf(Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_PURPLE)))

        versePresenter.onHighlightClicked(VerseIndex(0, 0, 0), Highlight.COLOR_NONE)
        verify(readingViewModel, times(1)).saveHighlight(VerseIndex(0, 0, 0), Highlight.COLOR_PURPLE)

        versePresenter.onHighlightClicked(VerseIndex(0, 0, 0), Highlight.COLOR_PURPLE)
        verify(readingViewModel, times(1)).saveHighlight(VerseIndex(0, 0, 0), Highlight.COLOR_NONE)
    }
}
