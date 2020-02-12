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

package me.xizzhu.android.joshua.strongnumber

import android.content.res.Resources
import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.viewData
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

class StrongNumberListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var resources: Resources
    @Mock
    private lateinit var strongNumberListActivity: StrongNumberListActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var strongNumberListInteractor: StrongNumberListInteractor
    @Mock
    private lateinit var loadingSpinner: ProgressBar
    @Mock
    private lateinit var strongNumberListView: CommonRecyclerView

    private lateinit var strongNumberListViewHolder: StrongNumberListViewHolder
    private lateinit var strongNumberListPresenter: StrongNumberListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(resources.getString(anyInt(), anyString(), anyInt())).thenReturn("")
        `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
        `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })
        `when`(strongNumberListActivity.resources).thenReturn(resources)
        `when`(strongNumberListActivity.strongNumber()).thenReturn("")

        `when`(strongNumberListInteractor.settings()).thenReturn(emptyFlow())
        `when`(strongNumberListInteractor.currentTranslation()).thenReturn(emptyFlow())

        strongNumberListViewHolder = StrongNumberListViewHolder(loadingSpinner, strongNumberListView)
        strongNumberListPresenter = StrongNumberListPresenter(strongNumberListActivity, navigator, strongNumberListInteractor, testDispatcher)
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true, true)
        `when`(strongNumberListInteractor.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.success(settings), ViewData.error()))

        strongNumberListPresenter.create(strongNumberListViewHolder)
        verify(strongNumberListView, times(1)).setSettings(settings)
        verify(strongNumberListView, never()).setSettings(Settings.DEFAULT)

        strongNumberListPresenter.destroy()
    }

    @Test
    fun testLoadStrongNumber() = testDispatcher.runBlockingTest {
        `when`(strongNumberListActivity.strongNumber()).thenReturn("H7225")
        `when`(strongNumberListInteractor.strongNumber("H7225")).thenReturn(StrongNumber("H7225", MockContents.strongNumberWords.getValue("H7225")))
        `when`(strongNumberListInteractor.currentTranslation()).thenReturn(flowOf(viewData { MockContents.kjvShortName }))
        `when`(strongNumberListInteractor.bookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
        `when`(strongNumberListInteractor.bookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)
        `when`(strongNumberListInteractor.verseIndexes("H7225")).thenReturn(MockContents.strongNumberReverseIndex.getValue("H7225"))
        `when`(strongNumberListInteractor.verses(MockContents.kjvShortName, MockContents.strongNumberReverseIndex.getValue("H7225")))
                .thenReturn(mapOf(VerseIndex(0, 0, 0) to MockContents.kjvVerses[0]))

        strongNumberListPresenter = spy(strongNumberListPresenter)
        doReturn("formatted strong number").`when`(strongNumberListPresenter).formatStrongNumber(any())

        strongNumberListPresenter.create(strongNumberListViewHolder)

        with(inOrder(loadingSpinner, strongNumberListView)) {
            verify(loadingSpinner, times(1)).fadeIn()
            verify(strongNumberListView, times(1)).visibility = View.GONE
            verify(strongNumberListView, times(1)).setItems(listOf(
                    TextItem("formatted strong number"),
                    TitleItem(MockContents.kjvBookNames[0], false),
                    VerseStrongNumberItem(VerseIndex(0, 0, 0), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[0].text.text, strongNumberListPresenter::openVerse)
            ))
            verify(strongNumberListView, times(1)).fadeIn()
            verify(loadingSpinner, times(1)).visibility = View.GONE
        }

        strongNumberListPresenter.destroy()
    }

    @Test
    fun testLoadStrongNumberWithException() = testDispatcher.runBlockingTest {
        val exception = RuntimeException("random exception")
        `when`(strongNumberListInteractor.strongNumber(anyString())).thenThrow(exception)
        `when`(strongNumberListInteractor.currentTranslation()).thenReturn(flowOf(viewData { MockContents.kjvShortName }))
        `when`(strongNumberListActivity.strongNumber()).thenReturn("sn")

        strongNumberListPresenter.create(strongNumberListViewHolder)

        with(inOrder(loadingSpinner, strongNumberListView)) {
            verify(loadingSpinner, times(1)).fadeIn()
            verify(strongNumberListView, times(1)).visibility = View.GONE
            verify(loadingSpinner, times(1)).visibility = View.GONE
        }
        verify(strongNumberListView, never()).setItems(any())
        verify(strongNumberListView, never()).fadeIn()

        strongNumberListPresenter.destroy()
    }
}
