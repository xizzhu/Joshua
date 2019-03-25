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
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class LoadingSpinnerPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var loadingSpinnerView: LoadingSpinnerView

    private lateinit var loadingSpinnerState: BroadcastChannel<LoadingSpinnerState>
    private lateinit var loadingSpinnerPresenter: LoadingSpinnerPresenter

    @Before
    override fun setup() {
        super.setup()

        loadingSpinnerState = ConflatedBroadcastChannel()
        loadingSpinnerPresenter = LoadingSpinnerPresenter(loadingSpinnerState.openSubscription())

        loadingSpinnerPresenter.attachView(loadingSpinnerView)
    }

    @After
    override fun tearDown() {
        loadingSpinnerPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testLoadingSpinnerState() {
        runBlocking {
            verify(loadingSpinnerView, never()).show()
            verify(loadingSpinnerView, never()).hide()

            loadingSpinnerState.send(LoadingSpinnerState.IS_LOADING)
            verify(loadingSpinnerView, times(1)).show()
            verify(loadingSpinnerView, never()).hide()

            loadingSpinnerState.send(LoadingSpinnerState.NOT_LOADING)
            verify(loadingSpinnerView, times(1)).show()
            verify(loadingSpinnerView, times(1)).hide()
        }
    }
}
