/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.strongnumber

import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StrongNumberListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var lifecycle: Lifecycle

    @Mock
    private lateinit var navigator: Navigator

    @Mock
    private lateinit var strongNumberListViewModel: StrongNumberListViewModel

    @Mock
    private lateinit var strongNumberListActivity: StrongNumberListActivity

    @Mock
    private lateinit var loadingSpinner: ProgressBar

    @Mock
    private lateinit var strongNumberListView: CommonRecyclerView

    private lateinit var strongNumberListViewHolder: StrongNumberListViewHolder
    private lateinit var strongNumberListPresenter: StrongNumberListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(strongNumberListActivity.lifecycle).thenReturn(lifecycle)

        strongNumberListViewHolder = StrongNumberListViewHolder(loadingSpinner, strongNumberListView)
        strongNumberListPresenter = StrongNumberListPresenter(navigator, strongNumberListViewModel, strongNumberListActivity, testCoroutineScope)
        strongNumberListPresenter.bind(strongNumberListViewHolder)
    }

    @Test
    fun testObserveSettings() = runBlocking {
        val settings = Settings.DEFAULT.copy(keepScreenOn = false)
        `when`(strongNumberListViewModel.settings()).thenReturn(flowOf(settings))

        strongNumberListPresenter.observeSettings()
        verify(strongNumberListView, times(1)).setSettings(settings)
    }

    @Test
    fun testLoadStrongNumber() = runBlocking {
        strongNumberListPresenter = spy(strongNumberListPresenter)
        doReturn("").`when`(strongNumberListPresenter).formatStrongNumber(any())
        `when`(strongNumberListActivity.strongNumber()).thenReturn("")
        `when`(strongNumberListViewModel.strongNumber(""))
                .thenReturn(flowOf(StrongNumberViewData(StrongNumber("", ""), emptyList(), emptyList(), emptyList())))

        strongNumberListPresenter.loadStrongNumber()

        with(inOrder(loadingSpinner, strongNumberListView)) {
            // loading
            verify(loadingSpinner, times(1)).fadeIn()
            verify(strongNumberListView, times(1)).visibility = View.GONE

            // success
            verify(strongNumberListView, times(1)).setItems(any())
            verify(strongNumberListView, times(1)).fadeIn()
            verify(loadingSpinner, times(1)).visibility = View.GONE
        }
    }

    @Test
    fun testLoadStrongNumberWithException() = runBlocking {
        `when`(strongNumberListActivity.strongNumber()).thenReturn("")
        `when`(strongNumberListViewModel.strongNumber("")).thenReturn(flow { throw RuntimeException() })

        strongNumberListPresenter.loadStrongNumber()
        verify(loadingSpinner, times(1)).visibility = View.GONE
    }

    @Test
    fun testToItems() {
        strongNumberListPresenter = spy(strongNumberListPresenter)
        doReturn("formatted strong number").`when`(strongNumberListPresenter).formatStrongNumber(any())

        val expected = listOf(
                TextItem("formatted strong number"),
                TitleItem(MockContents.kjvBookNames[0], false),
                VerseStrongNumberItem(VerseIndex(0, 0, 0), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[0].text.text, strongNumberListPresenter::openVerse)
        )

        val strongNumber = StrongNumber("H7225", MockContents.strongNumberWords.getValue("H7225"))
        val verses = listOf(MockContents.kjvVerses[0])
        val bookNames = MockContents.kjvBookNames
        val bookShortNames = MockContents.kjvBookShortNames
        val actual = with(strongNumberListPresenter) { StrongNumberViewData(strongNumber, verses, bookNames, bookShortNames).toItems() }

        assertEquals(expected, actual)
    }

    @Test
    fun testOpenChapter() = runBlocking {
        val verseIndex = VerseIndex(1, 2, 3)
        strongNumberListPresenter.openVerse(verseIndex)
        verify(strongNumberListViewModel, times(1)).saveCurrentVerseIndex(verseIndex)
        verify(navigator, times(1)).navigate(strongNumberListActivity, Navigator.SCREEN_READING)
    }
}
