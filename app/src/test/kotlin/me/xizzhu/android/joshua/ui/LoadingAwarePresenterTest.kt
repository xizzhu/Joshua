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

package me.xizzhu.android.joshua.ui

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class LoadingAwarePresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var loadingAwareView: LoadingAwareView

    private lateinit var loadingSpinnerState: BroadcastChannel<Int>
    private lateinit var loadingSpinnerPresenter: LoadingAwarePresenter

    @Before
    override fun setup() {
        super.setup()

        loadingSpinnerState = ConflatedBroadcastChannel()
        loadingSpinnerPresenter = LoadingAwarePresenter(loadingSpinnerState.asFlow())

        loadingSpinnerPresenter.attachView(loadingAwareView)
    }

    @After
    override fun tearDown() {
        loadingSpinnerPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testLoadingSpinnerState() {
        runBlocking {
            verify(loadingAwareView, never()).show()
            verify(loadingAwareView, never()).hide()

            loadingSpinnerState.send(BaseLoadingAwareInteractor.IS_LOADING)
            verify(loadingAwareView, times(1)).show()
            verify(loadingAwareView, never()).hide()

            loadingSpinnerState.send(BaseLoadingAwareInteractor.NOT_LOADING)
            verify(loadingAwareView, times(1)).show()
            verify(loadingAwareView, times(1)).hide()
        }
    }
}
