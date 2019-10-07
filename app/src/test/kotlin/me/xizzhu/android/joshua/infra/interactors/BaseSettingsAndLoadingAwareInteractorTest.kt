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

package me.xizzhu.android.joshua.infra.interactors

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseSettingsAndLoadingAwareInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var loadingAwareInteractor: BaseSettingsAndLoadingAwareInteractor

    @BeforeTest
    override fun setup() {
        super.setup()
        loadingAwareInteractor = object : BaseSettingsAndLoadingAwareInteractor(settingsManager, testDispatcher) {}
    }

    @Test
    fun testLoadingState() = testDispatcher.runBlockingTest {
        val loadingStateAsync = async { loadingAwareInteractor.loadingState().take(3).toList() }

        loadingAwareInteractor.updateLoadingState(ViewData.error(Unit))
        loadingAwareInteractor.updateLoadingState(ViewData.success(Unit))
        loadingAwareInteractor.updateLoadingState(ViewData.loading(Unit))

        assertEquals(
                listOf(ViewData.error(Unit), ViewData.success(Unit), ViewData.loading(Unit)),
                loadingStateAsync.await()
        )
    }
}
