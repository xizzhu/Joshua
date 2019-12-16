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

package me.xizzhu.android.joshua.infra.arch

import kotlinx.coroutines.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InteractorTest : BaseUnitTest() {
    private class TestInteractor(dispatcher: CoroutineDispatcher) : Interactor(dispatcher) {
        var onStartedCalled = false
        var onResumedCalled = false
        var onPausedCalled = false
        var onStoppedCalled = false

        lateinit var job: Job

        override fun onStart() {
            super.onStart()
            onStartedCalled = true
            job = coroutineScope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1L)
                }
            }
        }

        override fun onResume() {
            super.onResume()
            onResumedCalled = true
        }

        override fun onPause() {
            onPausedCalled = true
            super.onPause()
        }

        override fun onStop() {
            onStoppedCalled = true
            super.onStop()
        }
    }

    private lateinit var interactor: TestInteractor

    @BeforeTest
    override fun setup() {
        super.setup()
        interactor = TestInteractor(testDispatcher)
    }

    @Test
    fun testState() {
        // initial state
        assertFalse(interactor.onStartedCalled)
        assertFalse(interactor.onResumedCalled)
        assertFalse(interactor.onPausedCalled)
        assertFalse(interactor.onStoppedCalled)

        // start
        interactor.start()
        assertTrue(interactor.onStartedCalled)
        assertFalse(interactor.onResumedCalled)
        assertFalse(interactor.onPausedCalled)
        assertFalse(interactor.onStoppedCalled)
        assertFalse(interactor.job.isCancelled)

        // resume
        interactor.resume()
        assertTrue(interactor.onStartedCalled)
        assertTrue(interactor.onResumedCalled)
        assertFalse(interactor.onPausedCalled)
        assertFalse(interactor.onStoppedCalled)
        assertFalse(interactor.job.isCancelled)

        // pause
        interactor.pause()
        assertTrue(interactor.onStartedCalled)
        assertTrue(interactor.onResumedCalled)
        assertTrue(interactor.onPausedCalled)
        assertFalse(interactor.onStoppedCalled)
        assertFalse(interactor.job.isCancelled)

        // stop
        interactor.stop()
        assertTrue(interactor.onStartedCalled)
        assertTrue(interactor.onResumedCalled)
        assertTrue(interactor.onPausedCalled)
        assertTrue(interactor.onStoppedCalled)
        assertTrue(interactor.job.isCancelled)
    }

    @Test
    fun testRestart() {
        // start
        interactor.start()
        assertFalse(interactor.job.isCancelled)

        // stop
        interactor.stop()
        assertTrue(interactor.job.isCancelled)

        // start again
        interactor.start()
        assertFalse(interactor.job.isCancelled)

        // stop again
        interactor.stop()
        assertTrue(interactor.job.isCancelled)
    }
}
