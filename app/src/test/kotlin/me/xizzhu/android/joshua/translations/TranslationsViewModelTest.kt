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

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationsViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsManager: SettingsManager
    @Mock
    private lateinit var swipeRefreshInteractor: SwipeRefreshInteractor
    @Mock
    private lateinit var translationListInteractor: TranslationListInteractor

    private lateinit var translationsViewModel: TranslationsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(swipeRefreshInteractor.refreshRequested()).thenReturn(emptyFlow())
        `when`(translationListInteractor.translationList()).thenReturn(emptyFlow())

        translationsViewModel = TranslationsViewModel(settingsManager, swipeRefreshInteractor, translationListInteractor, testDispatcher)
    }

    @Test
    fun testObserveRefreshRequest() = testDispatcher.runBlockingTest {
        `when`(swipeRefreshInteractor.refreshRequested()).thenReturn(flowOf(ViewData.success(Unit)))

        translationsViewModel.start()
        verify(translationListInteractor, times(1)).loadTranslationList(true)
        verify(translationListInteractor, never()).loadTranslationList(false)

        translationsViewModel.stop()
    }

    @Test
    fun testObserveLoadingState() = testDispatcher.runBlockingTest {
        `when`(translationListInteractor.translationList()).thenReturn(flowOf(
                ViewData.error(TranslationList.EMPTY),
                ViewData.success(TranslationList.EMPTY),
                ViewData.loading(TranslationList.EMPTY)
        ))

        translationsViewModel.start()
        with(inOrder(swipeRefreshInteractor)) {
            verify(swipeRefreshInteractor, times(1)).updateLoadingState(ViewData.error(Unit))
            verify(swipeRefreshInteractor, times(1)).updateLoadingState(ViewData.success(Unit))
            verify(swipeRefreshInteractor, times(1)).updateLoadingState(ViewData.loading(Unit))
        }

        translationsViewModel.stop()
    }
}
