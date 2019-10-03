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

package me.xizzhu.android.joshua.infra.ui

import android.view.View
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.ui.fadeIn
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LoadingSpinnerPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var loadingSpinnerInteractor: LoadingSpinnerInteractor
    @Mock
    private lateinit var loadingSpinner: LoadingSpinner

    private lateinit var loadingSpinnerViewHolder: LoadingSpinnerViewHolder
    private lateinit var loadingSpinnerPresenter: LoadingSpinnerPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        loadingSpinnerViewHolder = LoadingSpinnerViewHolder(loadingSpinner)
        loadingSpinnerPresenter = LoadingSpinnerPresenter(loadingSpinnerInteractor, testDispatcher)
        loadingSpinnerPresenter.bind(loadingSpinnerViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        loadingSpinnerPresenter.unbind()
        super.tearDown()
    }

    @Test
    fun testObserveLoadingStateSuccess() = testDispatcher.runBlockingTest {
        `when`(loadingSpinnerInteractor.loadingState()).thenReturn(flowOf(ViewData.success(Unit)))

        loadingSpinnerPresenter.start()
        verify(loadingSpinnerViewHolder.loadingSpinner, times(1)).visibility = View.GONE
        verify(loadingSpinnerViewHolder.loadingSpinner, never()).fadeIn()

        loadingSpinnerPresenter.stop()
    }

    @Test
    fun testObserveLoadingStateError() = testDispatcher.runBlockingTest {
        `when`(loadingSpinnerInteractor.loadingState()).thenReturn(flowOf(ViewData.error(Unit)))

        loadingSpinnerPresenter.start()
        verify(loadingSpinnerViewHolder.loadingSpinner, times(1)).visibility = View.GONE
        verify(loadingSpinnerViewHolder.loadingSpinner, never()).fadeIn()

        loadingSpinnerPresenter.stop()
    }

    @Test
    fun testObserveLoadingStateLoading() = testDispatcher.runBlockingTest {
        `when`(loadingSpinnerInteractor.loadingState()).thenReturn(flowOf(ViewData.loading(Unit)))

        loadingSpinnerPresenter.start()
        verify(loadingSpinnerViewHolder.loadingSpinner, times(1)).fadeIn()
        verify(loadingSpinnerViewHolder.loadingSpinner, never()).visibility = anyInt()

        loadingSpinnerPresenter.stop()
    }
}
