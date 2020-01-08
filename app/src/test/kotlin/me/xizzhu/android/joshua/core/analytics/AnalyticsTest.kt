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

package me.xizzhu.android.joshua.core.analytics

import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.Test

class AnalyticsTest : BaseUnitTest() {
    @Mock
    private lateinit var provider: AnalyticsProvider

    @Test
    fun testAddProvider() {
        Analytics.addProvider(provider)
        Analytics.track("event")
        verify(provider, times(1)).track("event", null)
    }

    @Test
    fun testAddSameProvider() {
        Analytics.addProvider(provider)
        Analytics.addProvider(provider)
        Analytics.addProvider(provider)
        Analytics.track("event")
        verify(provider, times(1)).track("event", null)
    }

    @Test
    fun testAddThenRemoveProvider() {
        Analytics.addProvider(provider)
        Analytics.removeProvider(provider)
        Analytics.track("event")
        verify(provider, never()).track(anyString(), any())
    }
}
