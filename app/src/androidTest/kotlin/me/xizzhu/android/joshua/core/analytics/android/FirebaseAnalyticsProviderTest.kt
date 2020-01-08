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

package me.xizzhu.android.joshua.core.analytics.android

import androidx.test.platform.app.InstrumentationRegistry
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FirebaseAnalyticsProviderTest : BaseUnitTest() {
    private lateinit var firebaseAnalyticsProvider: FirebaseAnalyticsProvider

    @BeforeTest
    override fun setup() {
        super.setup()
        firebaseAnalyticsProvider = FirebaseAnalyticsProvider(InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    fun testToBundle() {
        with(firebaseAnalyticsProvider) {
            val bundle = mapOf(Pair("key1", "value1"), Pair("key2", 2L)).toBundle()
            assertEquals(2, bundle.size())
            assertEquals("value1", bundle.getString("key1"))
            assertEquals(2L, bundle.getLong("key2"))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testToBundleWithException() {
        with(firebaseAnalyticsProvider) {
            mapOf(Pair("key", 1.23456)).toBundle()
        }
    }
}
