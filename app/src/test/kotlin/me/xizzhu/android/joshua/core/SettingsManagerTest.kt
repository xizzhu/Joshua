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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.SettingsRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`

class SettingsManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var settingsManager: SettingsManager

    @Test
    fun testObserveInitialSettings() {
        runBlocking {
            val settings = Settings(false, true, 3, false)
            `when`(settingsRepository.readSettings()).thenReturn(settings)
            settingsManager = SettingsManager(settingsRepository)

            assertEquals(settings, settingsManager.observeSettings().first())
        }
    }

    @Test
    fun testObserveInitialSettingsWithException() {
        runBlocking {
            `when`(settingsRepository.readSettings()).thenThrow(RuntimeException("Random exception"))
            settingsManager = SettingsManager(settingsRepository)

            assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())
        }
    }

    @Test
    fun testUpdateSettings() {
        runBlocking {
            `when`(settingsRepository.readSettings()).thenReturn(Settings.DEFAULT)
            settingsManager = SettingsManager(settingsRepository)

            assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())

            val updatedSettings = Settings(false, true, 3, false)
            settingsManager.saveSettings(updatedSettings)
            assertEquals(updatedSettings, settingsManager.observeSettings().first())
        }
    }
}
