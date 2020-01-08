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
import org.mockito.Mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ViewPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var interactor: Interactor
    @Mock
    private lateinit var viewHolder: ViewHolder

    private class TestViewPresenter(interactor: Interactor, dispatcher: CoroutineDispatcher) : ViewPresenter<ViewHolder, Interactor>(interactor, dispatcher) {
        var onCreateCalled = false
        var onStartCalled = false
        var onResumeCalled = false
        var onPauseCalled = false
        var onStopCalled = false
        var onDestroyCalled = false

        lateinit var job: Job

        override fun onCreate(viewHolder: ViewHolder) {
            super.onCreate(viewHolder)
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

    private lateinit var viewPresenter: TestViewPresenter

    @BeforeTest
    override fun setup() {
        super.setup()
        viewPresenter = TestViewPresenter(interactor, testDispatcher)
    }

    @Test
    fun testState() {
        // initial state
        assertFalse(viewPresenter.onCreateCalled)
        assertFalse(viewPresenter.onStartCalled)
        assertFalse(viewPresenter.onResumeCalled)
        assertFalse(viewPresenter.onPauseCalled)
        assertFalse(viewPresenter.onStopCalled)
        assertFalse(viewPresenter.onDestroyCalled)

        // create
        viewPresenter.create(viewHolder)
        assertTrue(viewPresenter.onCreateCalled)
        assertFalse(viewPresenter.onStartCalled)
        assertFalse(viewPresenter.onResumeCalled)
        assertFalse(viewPresenter.onPauseCalled)
        assertFalse(viewPresenter.onStopCalled)
        assertFalse(viewPresenter.onDestroyCalled)

        // start
        viewPresenter.start()
        assertFalse(viewPresenter.job.isCancelled)
        assertTrue(viewPresenter.onCreateCalled)
        assertTrue(viewPresenter.onStartCalled)
        assertFalse(viewPresenter.onResumeCalled)
        assertFalse(viewPresenter.onPauseCalled)
        assertFalse(viewPresenter.onStopCalled)
        assertFalse(viewPresenter.onDestroyCalled)

        // resume
        viewPresenter.resume()
        assertFalse(viewPresenter.job.isCancelled)
        assertTrue(viewPresenter.onCreateCalled)
        assertTrue(viewPresenter.onStartCalled)
        assertTrue(viewPresenter.onResumeCalled)
        assertFalse(viewPresenter.onPauseCalled)
        assertFalse(viewPresenter.onStopCalled)
        assertFalse(viewPresenter.onDestroyCalled)

        // pause
        viewPresenter.pause()
        assertFalse(viewPresenter.job.isCancelled)
        assertTrue(viewPresenter.onCreateCalled)
        assertTrue(viewPresenter.onStartCalled)
        assertTrue(viewPresenter.onResumeCalled)
        assertTrue(viewPresenter.onPauseCalled)
        assertFalse(viewPresenter.onStopCalled)
        assertFalse(viewPresenter.onDestroyCalled)

        // stop
        viewPresenter.stop()
        assertFalse(viewPresenter.job.isCancelled)
        assertTrue(viewPresenter.onCreateCalled)
        assertTrue(viewPresenter.onStartCalled)
        assertTrue(viewPresenter.onResumeCalled)
        assertTrue(viewPresenter.onPauseCalled)
        assertTrue(viewPresenter.onStopCalled)
        assertFalse(viewPresenter.onDestroyCalled)

        // destroy
        viewPresenter.destroy()
        assertTrue(viewPresenter.onCreateCalled)
        assertTrue(viewPresenter.onStartCalled)
        assertTrue(viewPresenter.onResumeCalled)
        assertTrue(viewPresenter.onPauseCalled)
        assertTrue(viewPresenter.onStopCalled)
        assertTrue(viewPresenter.onDestroyCalled)
    }
}
