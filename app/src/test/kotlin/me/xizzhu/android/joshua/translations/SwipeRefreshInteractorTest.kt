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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SwipeRefreshInteractorTest : BaseUnitTest() {
    private lateinit var swipeRefreshInteractor: SwipeRefreshInteractor

    @BeforeTest
    override fun setup() {
        super.setup()
        swipeRefreshInteractor = SwipeRefreshInteractor(testDispatcher)
    }

    @Test
    fun testRefreshRequest() = testDispatcher.runBlockingTest {
        val refreshRequestAsync = async { swipeRefreshInteractor.refreshRequested().take(1).first() }
        swipeRefreshInteractor.requestRefresh()
        assertEquals(ViewData.success(null), refreshRequestAsync.await())
    }
}
