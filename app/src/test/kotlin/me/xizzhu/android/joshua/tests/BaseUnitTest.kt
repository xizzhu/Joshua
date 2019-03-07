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

package me.xizzhu.android.joshua.tests

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.mockito.Mockito

abstract class BaseUnitTest {
    @CallSuper
    @Before
    open fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @CallSuper
    @After
    open fun tearDown() {
        Dispatchers.resetMain()
    }

    protected fun <T> any(): T {
        // Mockito.any() returns null, but Kotlin hates it, so we have this hack here.
        Mockito.any<T>()
        return null as T
    }
}
