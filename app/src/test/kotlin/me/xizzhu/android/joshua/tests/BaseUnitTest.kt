/*
 * Copyright (C) 2023 Xizhi Zhu
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
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class BaseUnitTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    protected val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
    protected val testCoroutineDispatcherProvider = object : CoroutineDispatcherProvider {
        override val main: CoroutineDispatcher
            get() = testDispatcher
        override val default: CoroutineDispatcher
            get() = testDispatcher
        override val io: CoroutineDispatcher
            get() = testDispatcher
        override val unconfined: CoroutineDispatcher
            get() = testDispatcher
    }

    @CallSuper
    @BeforeTest
    open fun setup() {
        Dispatchers.setMain(testDispatcher)
        Locale.setDefault(Locale.US)
    }

    @CallSuper
    @AfterTest
    open fun tearDown() {
        Dispatchers.resetMain()
    }
}
