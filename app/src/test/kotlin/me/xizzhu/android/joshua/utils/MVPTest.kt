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

package me.xizzhu.android.joshua.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MVPTest {
    private class MVPViewStub : MVPView
    private class MVPPresenterStub : MVPPresenter<MVPViewStub>() {
        var onViewAttachedCalled = false
        var onViewDetachedCalled = false

        val receiveChannel = Channel<Int>()
        lateinit var job: Job

        override fun onViewAttached() {
            super.onViewAttached()
            onViewAttachedCalled = true
            receiveChannels.add(receiveChannel)
            job = launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1L)
                }
            }
        }

        override fun onViewDetached() {
            onViewDetachedCalled = true
            super.onViewDetached()
        }
    }

    private lateinit var mvpViewStub: MVPViewStub
    private lateinit var mvpPresenterStub: MVPPresenterStub

    @Before
    fun setup() {
        mvpViewStub = MVPViewStub()
        mvpPresenterStub = MVPPresenterStub()
    }

    @Test
    fun testInitialState() {
        assertFalse(mvpPresenterStub.onViewAttachedCalled)
        assertFalse(mvpPresenterStub.onViewDetachedCalled)
    }

    @Test
    fun testOnViewAttached() {
        mvpPresenterStub.attachView(mvpViewStub)
        assertTrue(mvpPresenterStub.onViewAttachedCalled)
        assertFalse(mvpPresenterStub.onViewDetachedCalled)
    }

    @Test
    fun testOnViewDetached() {
        mvpPresenterStub.detachView()
        assertFalse(mvpPresenterStub.onViewAttachedCalled)
        assertTrue(mvpPresenterStub.onViewDetachedCalled)
    }

    @Test
    fun testOnViewAttachedThenDetached() {
        mvpPresenterStub.attachView(mvpViewStub)
        assertTrue(mvpPresenterStub.onViewAttachedCalled)
        assertFalse(mvpPresenterStub.onViewDetachedCalled)

        mvpPresenterStub.detachView()
        assertTrue(mvpPresenterStub.onViewAttachedCalled)
        assertTrue(mvpPresenterStub.onViewDetachedCalled)
    }

    @Test
    fun testReceiveChannelCloseOnDetach() {
        mvpPresenterStub.attachView(mvpViewStub)
        assertFalse(mvpPresenterStub.receiveChannel.isClosedForReceive)

        mvpPresenterStub.detachView()
        assertTrue(mvpPresenterStub.receiveChannel.isClosedForReceive)
    }

    @Test
    fun testJobCanceledDetach() {
        mvpPresenterStub.attachView(mvpViewStub)
        assertFalse(mvpPresenterStub.job.isCancelled)

        mvpPresenterStub.detachView()
        assertTrue(mvpPresenterStub.job.isCancelled)
    }
}
