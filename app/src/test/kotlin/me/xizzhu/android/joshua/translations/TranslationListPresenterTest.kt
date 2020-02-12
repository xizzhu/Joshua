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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TranslationListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var translationManagementActivity: TranslationManagementActivity
    @Mock
    private lateinit var translationListInteractor: TranslationListInteractor
    @Mock
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @Mock
    private lateinit var translationListView: CommonRecyclerView

    private lateinit var translationListViewHolder: TranslationListViewHolder
    private lateinit var translationListPresenter: TranslationListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(translationListInteractor.settings()).thenReturn(emptyFlow())
        `when`(translationListInteractor.translationList()).thenReturn(emptyFlow())

        translationListViewHolder = TranslationListViewHolder(swipeRefreshLayout, translationListView)
        translationListPresenter = TranslationListPresenter(translationManagementActivity, translationListInteractor, testDispatcher)
        translationListPresenter.create(translationListViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        translationListPresenter.destroy()
        super.tearDown()
    }

    @Test
    fun testLoadTranslationRequestedOnCreate() {
        verify(translationListInteractor, times(1)).loadTranslationList(false)
        verify(translationListInteractor, never()).loadTranslationList(true)
    }

    @Test
    fun testOnAvailableTranslationClicked() {
        val translation = MockContents.kjvTranslationInfo
        `when`(translationListInteractor.downloadTranslation(translation)).thenReturn(emptyFlow())

        translationListPresenter.onTranslationClicked(translation)
        verify(translationListInteractor, times(1)).downloadTranslation(translation)
    }

    @Test
    fun testOnDownloadedTranslationClicked() = testDispatcher.runBlockingTest {
        val translation = MockContents.kjvDownloadedTranslationInfo
        translationListPresenter.onTranslationClicked(translation)
        verify(translationListInteractor, times(1)).saveCurrentTranslation(translation.shortName)
        verify(translationManagementActivity, times(1)).finish()
        verify(translationListInteractor, never()).downloadTranslation(translation)
    }

    @Test
    fun testOnDownloadedTranslationClickedWithException() = testDispatcher.runBlockingTest {
        val translation = MockContents.kjvDownloadedTranslationInfo
        `when`(translationListInteractor.saveCurrentTranslation(translation.shortName)).thenThrow(RuntimeException("random exception"))

        translationListPresenter.onTranslationClicked(translation)
        verify(translationListInteractor, never()).downloadTranslation(translation)
    }

    @Test
    fun testOnAvailableTranslationLongClicked() {
        val translation = MockContents.kjvTranslationInfo
        `when`(translationListInteractor.downloadTranslation(translation)).thenReturn(emptyFlow())

        translationListPresenter.onTranslationLongClicked(translation, false)
        verify(translationListInteractor, times(1)).downloadTranslation(translation)
    }
}
