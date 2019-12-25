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
    private class TestViewModel(interactors: List<Interactor>, dispatcher: CoroutineDispatcher) : ViewModel(interactors, dispatcher) {
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

    @Mock
    private lateinit var mockInteractor: Interactor

    private lateinit var testViewModel: TestViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        testViewModel = TestViewModel(listOf(mockInteractor), testDispatcher)
    }

    @Test
    fun testState() {
        // initial state
        verify(mockInteractor, never()).create()
        verify(mockInteractor, never()).start()
        verify(mockInteractor, never()).resume()
        verify(mockInteractor, never()).pause()
        verify(mockInteractor, never()).stop()
        verify(mockInteractor, never()).destroy()
        assertFalse(testViewModel.onCreateCalled)
        assertFalse(testViewModel.onStartCalled)
        assertFalse(testViewModel.onResumeCalled)
        assertFalse(testViewModel.onPauseCalled)
        assertFalse(testViewModel.onStopCalled)
        assertFalse(testViewModel.onDestroyCalled)

        // create
        testViewModel.create()
        verify(mockInteractor, times(1)).create()
        verify(mockInteractor, never()).start()
        verify(mockInteractor, never()).resume()
        verify(mockInteractor, never()).pause()
        verify(mockInteractor, never()).stop()
        verify(mockInteractor, never()).destroy()
        assertTrue(testViewModel.onCreateCalled)
        assertFalse(testViewModel.onStartCalled)
        assertFalse(testViewModel.onResumeCalled)
        assertFalse(testViewModel.onPauseCalled)
        assertFalse(testViewModel.onStopCalled)
        assertFalse(testViewModel.onDestroyCalled)

        // start
        testViewModel.start()
        verify(mockInteractor, times(1)).create()
        verify(mockInteractor, times(1)).start()
        verify(mockInteractor, never()).resume()
        verify(mockInteractor, never()).pause()
        verify(mockInteractor, never()).stop()
        verify(mockInteractor, never()).destroy()
        assertFalse(testViewModel.job.isCancelled)
        assertTrue(testViewModel.onCreateCalled)
        assertTrue(testViewModel.onStartCalled)
        assertFalse(testViewModel.onResumeCalled)
        assertFalse(testViewModel.onPauseCalled)
        assertFalse(testViewModel.onStopCalled)
        assertFalse(testViewModel.onDestroyCalled)

        // resume
        testViewModel.resume()
        verify(mockInteractor, times(1)).create()
        verify(mockInteractor, times(1)).start()
        verify(mockInteractor, times(1)).resume()
        verify(mockInteractor, never()).pause()
        verify(mockInteractor, never()).stop()
        verify(mockInteractor, never()).destroy()
        assertFalse(testViewModel.job.isCancelled)
        assertTrue(testViewModel.onCreateCalled)
        assertTrue(testViewModel.onStartCalled)
        assertTrue(testViewModel.onResumeCalled)
        assertFalse(testViewModel.onPauseCalled)
        assertFalse(testViewModel.onStopCalled)
        assertFalse(testViewModel.onDestroyCalled)

        // pause
        testViewModel.pause()
        verify(mockInteractor, times(1)).create()
        verify(mockInteractor, times(1)).start()
        verify(mockInteractor, times(1)).resume()
        verify(mockInteractor, times(1)).pause()
        verify(mockInteractor, never()).stop()
        verify(mockInteractor, never()).destroy()
        assertFalse(testViewModel.job.isCancelled)
        assertTrue(testViewModel.onCreateCalled)
        assertTrue(testViewModel.onStartCalled)
        assertTrue(testViewModel.onResumeCalled)
        assertTrue(testViewModel.onPauseCalled)
        assertFalse(testViewModel.onStopCalled)
        assertFalse(testViewModel.onDestroyCalled)

        // stop
        testViewModel.stop()
        verify(mockInteractor, times(1)).create()
        verify(mockInteractor, times(1)).start()
        verify(mockInteractor, times(1)).resume()
        verify(mockInteractor, times(1)).pause()
        verify(mockInteractor, times(1)).stop()
        verify(mockInteractor, never()).destroy()
        assertFalse(testViewModel.job.isCancelled)
        assertTrue(testViewModel.onCreateCalled)
        assertTrue(testViewModel.onStartCalled)
        assertTrue(testViewModel.onResumeCalled)
        assertTrue(testViewModel.onPauseCalled)
        assertTrue(testViewModel.onStopCalled)
        assertFalse(testViewModel.onDestroyCalled)

        // destroy
        testViewModel.destroy()
        verify(mockInteractor, times(1)).create()
        verify(mockInteractor, times(1)).start()
        verify(mockInteractor, times(1)).resume()
        verify(mockInteractor, times(1)).pause()
        verify(mockInteractor, times(1)).stop()
        verify(mockInteractor, times(1)).destroy()
        assertTrue(testViewModel.job.isCancelled)
        assertTrue(testViewModel.onCreateCalled)
        assertTrue(testViewModel.onStartCalled)
        assertTrue(testViewModel.onResumeCalled)
        assertTrue(testViewModel.onPauseCalled)
        assertTrue(testViewModel.onStopCalled)
        assertTrue(testViewModel.onDestroyCalled)
    }
}
