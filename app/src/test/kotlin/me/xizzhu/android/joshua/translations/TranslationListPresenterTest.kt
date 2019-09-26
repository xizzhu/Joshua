/*
 * Copyright (C) 2019 Xizhi Zhu
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

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class TranslationListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var translationManagementActivity: TranslationManagementActivity
    @Mock
    private lateinit var translationListInteractor: TranslationListInteractor
    @Mock
    private lateinit var translationListView: TranslationListView

    private lateinit var translationListViewHolder: TranslationListViewHolder
    private lateinit var translationListPresenter: TranslationListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(translationListInteractor.settings()).thenReturn(emptyFlow())
        `when`(translationListInteractor.translationList()).thenReturn(emptyFlow())
        `when`(translationListInteractor.translationDownload()).thenReturn(emptyFlow())
        `when`(translationListInteractor.translationRemoval()).thenReturn(emptyFlow())

        translationListViewHolder = TranslationListViewHolder(translationListView)
        translationListPresenter = TranslationListPresenter(translationManagementActivity, translationListInteractor, testDispatcher)
    }

    @Test
    fun testLoadTranslationRequestedOnBind() {
        translationListPresenter.bind(translationListViewHolder)
        verify(translationListInteractor, times(1)).loadTranslationList(false)
        verify(translationListInteractor, never()).loadTranslationList(true)

        translationListPresenter.unbind()
    }

    @Test
    fun testOnAvailableTranslationClicked() {
        translationListPresenter.bind(translationListViewHolder)

        val translation = MockContents.kjvTranslationInfo
        translationListPresenter.onTranslationClicked(translation)
        verify(translationListInteractor, times(1)).downloadTranslation(translation)

        translationListPresenter.unbind()
    }

    @Test
    fun testOnDownloadedTranslationClicked() = testDispatcher.runBlockingTest {
        translationListPresenter.bind(translationListViewHolder)

        val translation = MockContents.kjvDownloadedTranslationInfo
        translationListPresenter.onTranslationClicked(translation)
        verify(translationListInteractor, times(1)).saveCurrentTranslation(translation.shortName)
        verify(translationManagementActivity, times(1)).finish()
        verify(translationListInteractor, never()).downloadTranslation(translation)

        translationListPresenter.unbind()
    }

    @Test
    fun testOnDownloadedTranslationClickedWithException() = testDispatcher.runBlockingTest {
        val translation = MockContents.kjvDownloadedTranslationInfo
        `when`(translationListInteractor.saveCurrentTranslation(translation.shortName)).thenThrow(RuntimeException("random exception"))

        translationListPresenter.bind(translationListViewHolder)

        translationListPresenter.onTranslationClicked(translation)
        verify(translationListInteractor, never()).downloadTranslation(translation)

        translationListPresenter.unbind()
    }

    @Test
    fun testOnAvailableTranslationLongClicked() {
        translationListPresenter.bind(translationListViewHolder)

        val translation = MockContents.kjvTranslationInfo
        translationListPresenter.onTranslationLongClicked(translation, false)
        verify(translationListInteractor, times(1)).downloadTranslation(translation)

        translationListPresenter.unbind()
    }
}
