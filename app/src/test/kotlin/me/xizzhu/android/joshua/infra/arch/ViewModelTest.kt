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
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ViewModelTest : BaseUnitTest() {
    private class TestViewModel(interactors: Set<Interactor>, dispatcher: CoroutineDispatcher) : ViewModel(interactors, dispatcher) {
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

    @Mock
    private lateinit var mockInteractor: Interactor

    private lateinit var testViewModel: TestViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        testViewModel = TestViewModel(setOf(mockInteractor), testDispatcher)
    }

    @Test
    fun testState() {
        // initial state
        verify(mockInteractor, never()).start()
        verify(mockInteractor, never()).stop()
        assertFalse(testViewModel.onStartedCalled)
        assertFalse(testViewModel.onStoppedCalled)

        // start
        testViewModel.start()
        verify(mockInteractor, times(1)).start()
        verify(mockInteractor, never()).stop()
        assertFalse(testViewModel.job.isCancelled)
        assertTrue(testViewModel.onStartedCalled)
        assertFalse(testViewModel.onStoppedCalled)

        // stop
        testViewModel.stop()
        verify(mockInteractor, times(1)).start()
        verify(mockInteractor, times(1)).stop()
        assertTrue(testViewModel.job.isCancelled)
        assertTrue(testViewModel.onStartedCalled)
        assertTrue(testViewModel.onStoppedCalled)
    }

    @Test
    fun testRestart() {
        // start
        testViewModel.start()
        assertFalse(testViewModel.job.isCancelled)

        // stop
        testViewModel.stop()
        assertTrue(testViewModel.job.isCancelled)

        // start again
        testViewModel.start()
        assertFalse(testViewModel.job.isCancelled)

        // stop again
        testViewModel.stop()
        assertTrue(testViewModel.job.isCancelled)
    }
}
