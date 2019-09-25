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
        var onBindCalled = false
        var onUnbindCalled = false

        lateinit var job: Job

        override fun onBind(viewHolder: ViewHolder) {
            super.onBind(viewHolder)
            onBindCalled = true
            job = coroutineScope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1L)
                }
            }
        }

        override fun onUnbind() {
            onUnbindCalled = true
            super.onUnbind()
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
        assertFalse(viewPresenter.onBindCalled)
        assertFalse(viewPresenter.onUnbindCalled)

        // bind
        viewPresenter.bind(viewHolder)
        assertFalse(viewPresenter.job.isCancelled)
        assertTrue(viewPresenter.onBindCalled)
        assertFalse(viewPresenter.onUnbindCalled)

        // unbind
        viewPresenter.unbind()
        assertTrue(viewPresenter.job.isCancelled)
        assertTrue(viewPresenter.onBindCalled)
        assertTrue(viewPresenter.onUnbindCalled)
    }
}
