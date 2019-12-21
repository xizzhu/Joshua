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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.repository.local.LocalSettingsStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localSettingsStorage: LocalSettingsStorage

    private lateinit var settingsRepository: SettingsRepository

    @Test
    fun testObserveInitialSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 3, false, false)
        `when`(localSettingsStorage.readSettings()).thenReturn(settings)
        settingsRepository = SettingsRepository(localSettingsStorage)

        assertEquals(settings, settingsRepository.settings().first())
    }

    @Test
    fun testObserveInitialSettingsWithException() = testDispatcher.runBlockingTest {
        `when`(localSettingsStorage.readSettings()).thenThrow(RuntimeException("Random exception"))
        settingsRepository = SettingsRepository(localSettingsStorage)

        assertEquals(Settings.DEFAULT, settingsRepository.settings().first())
    }

    @Test
    fun testSaveThenReadSettings() = testDispatcher.runBlockingTest {
        `when`(localSettingsStorage.readSettings()).thenReturn(Settings.DEFAULT)
        settingsRepository = SettingsRepository(localSettingsStorage)

        val settings = Settings(false, true, 1, true, true)
        settingsRepository.saveSettings(settings)
        assertEquals(settings, settingsRepository.settings().first())
    }
}
