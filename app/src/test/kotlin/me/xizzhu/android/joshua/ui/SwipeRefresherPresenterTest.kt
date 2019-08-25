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
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class SwipeRefresherPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var swipeRefresherView: LoadingAwareView
    @Mock
    private lateinit var swipeRefresherInteractor: BaseSwipeRefresherInteractor

    private lateinit var swipeRefresherState: BroadcastChannel<Int>
    private lateinit var swipeRefresherPresenter: SwipeRefresherPresenter

    @Before
    override fun setup() {
        super.setup()

        swipeRefresherState = ConflatedBroadcastChannel()
        swipeRefresherPresenter = SwipeRefresherPresenter(swipeRefresherState.asFlow(), swipeRefresherInteractor)

        swipeRefresherPresenter.attachView(swipeRefresherView)
    }

    @After
    override fun tearDown() {
        swipeRefresherPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testRefresherStateIsLoading() {
        verify(swipeRefresherView, never()).show()
        verify(swipeRefresherView, never()).hide()

        swipeRefresherState.offer(BaseLoadingAwareInteractor.IS_LOADING)
        verify(swipeRefresherView, times(1)).show()
        verify(swipeRefresherView, never()).hide()
    }

    @Test
    fun testRefresherStateNotLoading() {
        verify(swipeRefresherView, never()).show()
        verify(swipeRefresherView, never()).hide()

        swipeRefresherState.offer(BaseLoadingAwareInteractor.NOT_LOADING)
        verify(swipeRefresherView, never()).show()
        verify(swipeRefresherView, times(1)).hide()
    }

    @Test
    fun testRefresh() {
        verify(swipeRefresherInteractor, never()).notifyRefreshRequested()

        swipeRefresherPresenter.refresh()
        verify(swipeRefresherInteractor, times(1)).notifyRefreshRequested()
    }
}
