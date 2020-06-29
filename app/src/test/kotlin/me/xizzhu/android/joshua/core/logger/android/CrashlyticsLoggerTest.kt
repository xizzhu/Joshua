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

package me.xizzhu.android.joshua.core.logger.android

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class CrashlyticsLoggerTest : BaseUnitTest() {
    @Test
    fun testRandomExceptionIsNotCoroutineCancellationException() {
        CrashlyticsLogger().run {
            assertFalse(Throwable().isCoroutineCancellationException())
            assertFalse(RuntimeException().isCoroutineCancellationException())
            assertFalse(Error().isCoroutineCancellationException())
            assertFalse(CancellationException().isCoroutineCancellationException())
            assertFalse(RuntimeException("", CancellationException()).isCoroutineCancellationException())
            assertFalse(CancellationException("", CancellationException()).isCoroutineCancellationException())
        }
    }

    @Test
    fun testTimeoutIsNotCoroutineCancellationException() = runBlocking {
        CrashlyticsLogger().run {
            try {
                withTimeout(1L) { delay(1000L) }
                fail()
            } catch (e: CancellationException) {
                assertFalse(e.isCoroutineCancellationException())
            }
        }
    }

    @Test
    fun testIsCoroutineCancellationException() = runBlocking {
        CrashlyticsLogger().run {
            CoroutineScope(Job()).run {
                launch {
                    try {
                        delay(1000L)
                        fail()
                    } catch (e: CancellationException) {
                        assertTrue(e.isCoroutineCancellationException())
                    }
                }
                cancel()
            }
        }
    }

    @Test
    fun testRootCauseIsCoroutineCancellationException() = runBlocking {
        CrashlyticsLogger().run {
            CoroutineScope(Job()).run {
                launch {
                    try {
                        try {
                            delay(1000L)
                            fail()
                        } catch (e: CancellationException) {
                            throw RuntimeException("", e)
                        }
                    } catch (e: RuntimeException) {
                        assertTrue(e.isCoroutineCancellationException())
                    }
                }
                cancel()
            }
        }
    }

    @Test
    fun testIsChildCancelledException() = runBlocking {
        flowOf(1, 2, 3)
                .mapLatest {
                    try {
                        delay(1000L)
                        if (it != 3) fail()
                    } catch (e: CancellationException) {
                        with(CrashlyticsLogger()) { assertTrue(e.isCoroutineCancellationException()) }
                    }
                }.collect()
    }

    @Test
    fun testRootCauseIsChildCancelledException() = runBlocking {
        flowOf(1, 2, 3)
                .mapLatest {
                    try {
                        try {
                            delay(1000L)
                            if (it != 3) fail()
                        } catch (e: CancellationException) {
                            throw RuntimeException("", e)
                        }
                    } catch (e: RuntimeException) {
                        with(CrashlyticsLogger()) { assertTrue(e.isCoroutineCancellationException()) }
                    }
                }.collect()
    }
}
