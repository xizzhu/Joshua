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

package me.xizzhu.android.joshua.translations

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var lifecycle: Lifecycle
    @Mock
    private lateinit var translationsViewModel: TranslationsViewModel
    @Mock
    private lateinit var translationsActivity: TranslationsActivity
    @Mock
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @Mock
    private lateinit var translationListView: CommonRecyclerView

    private lateinit var translationListViewHolder: TranslationListViewHolder
    private lateinit var translationListPresenter: TranslationListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(translationsActivity.lifecycle).thenReturn(lifecycle)

        translationListViewHolder = TranslationListViewHolder(swipeRefreshLayout, translationListView)
        translationListPresenter = TranslationListPresenter(translationsViewModel, translationsActivity, testCoroutineScope)
        translationListPresenter.bind(translationListViewHolder)
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings.DEFAULT.copy(keepScreenOn = false)
        `when`(translationsViewModel.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.error(), ViewData.success(settings)))
        `when`(translationsViewModel.translationList(anyBoolean())).thenReturn(emptyFlow())

        translationListPresenter.onCreate()
        verify(translationListView, times(1)).setSettings(settings)
    }

    @Test
    fun testLoadTranslationList() = testDispatcher.runBlockingTest {
        `when`(translationsViewModel.settings()).thenReturn(emptyFlow())
        `when`(translationsViewModel.translationList(false)).thenReturn(flowOf(
                ViewData.error(exception = RuntimeException()),
                ViewData.loading(),
                ViewData.success(TranslationList("", emptyList(), emptyList()))
        ))

        translationListPresenter.onCreate()

        with(inOrder(swipeRefreshLayout, translationListView)) {
            // error
            verify(swipeRefreshLayout, times(1)).isRefreshing = false

            // loading
            verify(swipeRefreshLayout, times(1)).isRefreshing = true
            verify(translationListView, times(1)).visibility = View.GONE

            // success
            verify(swipeRefreshLayout, times(1)).isRefreshing = false
            verify(translationListView, times(1)).setItems(any())
            verify(translationListView, times(1)).fadeIn()
        }
    }

    @Test
    fun testToItems() {
        val expected = listOf(
                TitleItem(Locale("en").displayName, true),
                TranslationItem(MockContents.kjvDownloadedTranslationInfo, true, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked),
                TitleItem(Locale("zh").displayName, true),
                TranslationItem(MockContents.cuvDownloadedTranslationInfo, false, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked),
                TitleItem("AVAILABLE", false),
                TitleItem(Locale("en").displayName, true),
                TranslationItem(MockContents.bbeTranslationInfo, false, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked),
                TranslationItem(MockContents.msgTranslationInfo, false, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked)
        )

        `when`(translationsActivity.getString(R.string.header_available_translations)).thenReturn("AVAILABLE")
        val translationList = TranslationList(
                MockContents.kjvShortName,
                listOf(MockContents.bbeTranslationInfo, MockContents.msgTranslationInfo),
                listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvDownloadedTranslationInfo)
        )
        val actual = with(translationListPresenter) { translationList.toItems() }

        assertEquals(expected, actual)
    }
}
