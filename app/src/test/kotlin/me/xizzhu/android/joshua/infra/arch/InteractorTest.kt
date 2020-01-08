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

package me.xizzhu.android.joshua.infra.arch

import kotlinx.coroutines.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InteractorTest : BaseUnitTest() {
    private class TestInteractor(dispatcher: CoroutineDispatcher) : Interactor(dispatcher) {
        var onCreateCalled = false
        var onStartCalled = false
        var onResumeCalled = false
        var onPauseCalled = false
        var onStopCalled = false
        var onDestroyCalled = false

        lateinit var job: Job

        override fun onCreate() {
            super.onCreate()
            onCreateCalled = true
        }

        override fun onStart() {
            super.onStart()
            onStartCalled = true
            job = coroutineScope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1L)
                }
            }
        }

        override fun onResume() {
            super.onResume()
            onResumeCalled = true
        }

        override fun onPause() {
            onPauseCalled = true
            super.onPause()
        }

        override fun onStop() {
            onStopCalled = true
            super.onStop()
        }

        override fun onDestroy() {
            onDestroyCalled = true
            super.onDestroy()
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
        assertFalse(interactor.onCreateCalled)
        assertFalse(interactor.onStartCalled)
        assertFalse(interactor.onResumeCalled)
        assertFalse(interactor.onPauseCalled)
        assertFalse(interactor.onStopCalled)
        assertFalse(interactor.onDestroyCalled)

        // create
        interactor.create()
        assertTrue(interactor.onCreateCalled)
        assertFalse(interactor.onStartCalled)
        assertFalse(interactor.onResumeCalled)
        assertFalse(interactor.onPauseCalled)
        assertFalse(interactor.onStopCalled)
        assertFalse(interactor.onDestroyCalled)

        // start
        interactor.start()
        assertFalse(interactor.job.isCancelled)
        assertTrue(interactor.onCreateCalled)
        assertTrue(interactor.onStartCalled)
        assertFalse(interactor.onResumeCalled)
        assertFalse(interactor.onPauseCalled)
        assertFalse(interactor.onStopCalled)
        assertFalse(interactor.onDestroyCalled)

        // resume
        interactor.resume()
        assertFalse(interactor.job.isCancelled)
        assertTrue(interactor.onCreateCalled)
        assertTrue(interactor.onStartCalled)
        assertTrue(interactor.onResumeCalled)
        assertFalse(interactor.onPauseCalled)
        assertFalse(interactor.onStopCalled)
        assertFalse(interactor.onDestroyCalled)

        // pause
        interactor.pause()
        assertFalse(interactor.job.isCancelled)
        assertTrue(interactor.onCreateCalled)
        assertTrue(interactor.onStartCalled)
        assertTrue(interactor.onResumeCalled)
        assertTrue(interactor.onPauseCalled)
        assertFalse(interactor.onStopCalled)
        assertFalse(interactor.onDestroyCalled)

        // stop
        interactor.stop()
        assertFalse(interactor.job.isCancelled)
        assertTrue(interactor.onCreateCalled)
        assertTrue(interactor.onStartCalled)
        assertTrue(interactor.onResumeCalled)
        assertTrue(interactor.onPauseCalled)
        assertTrue(interactor.onStopCalled)
        assertFalse(interactor.onDestroyCalled)

        // destroy
        interactor.destroy()
        assertTrue(interactor.onCreateCalled)
        assertTrue(interactor.onStartCalled)
        assertTrue(interactor.onResumeCalled)
        assertTrue(interactor.onPauseCalled)
        assertTrue(interactor.onStopCalled)
        assertTrue(interactor.onDestroyCalled)
    }
}
