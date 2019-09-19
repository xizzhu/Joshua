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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InteractorTest {
    private class TestInteractor : Interactor() {
        var onStartedCalled = false
        var onStoppedCalled = false

        lateinit var job: Job

        override fun onStarted() {
            super.onStarted()
            onStartedCalled = true
            job = coroutineScope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1L)
                }
            }
        }

        override fun onStopped() {
            onStoppedCalled = true
            super.onStopped()
        }
    }

    private lateinit var interactor: TestInteractor

    @BeforeTest
    fun setup() {
        interactor = TestInteractor()
    }

    @Test
    fun testState() {
        // initial state
        assertFalse(interactor.onStartedCalled)
        assertFalse(interactor.onStoppedCalled)

        // start
        interactor.start()
        assertTrue(interactor.onStartedCalled)
        assertFalse(interactor.onStoppedCalled)

        // stop
        interactor.stop()
        assertTrue(interactor.onStartedCalled)
        assertTrue(interactor.onStoppedCalled)
    }

    @Test
    fun testJobCanceledOnStopped() {
        // start
        interactor.start()
        assertFalse(interactor.job.isCancelled)

        // stop
        interactor.stop()
        assertTrue(interactor.job.isCancelled)
    }
}
