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

package me.xizzhu.android.joshua.infra.activity

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseSettingsAwareInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var baseSettingsAwareInteractor: BaseSettingsAwareInteractor

    @BeforeTest
    override fun setup() {
        super.setup()
        baseSettingsAwareInteractor = object : BaseSettingsAwareInteractor(settingsManager, testDispatcher) {}
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        `when`(settingsManager.observeSettings()).thenReturn(flowOf(Settings.DEFAULT))

        assertEquals(ViewData.success(Settings.DEFAULT), baseSettingsAwareInteractor.settings().first())
    }
}
