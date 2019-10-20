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
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.repository.SettingsRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var settingsManager: SettingsManager

    @Test
    fun testObserveInitialSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 3, false)
        `when`(settingsRepository.readSettings()).thenReturn(settings)
        settingsManager = SettingsManager(settingsRepository)

        assertEquals(settings, settingsManager.observeSettings().first())
    }

    @Test
    fun testObserveInitialSettingsWithException() = testDispatcher.runBlockingTest {
        `when`(settingsRepository.readSettings()).thenThrow(RuntimeException("Random exception"))
        settingsManager = SettingsManager(settingsRepository)

        assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())
    }

    @Test
    fun testUpdateSettings() = testDispatcher.runBlockingTest {
        `when`(settingsRepository.readSettings()).thenReturn(Settings.DEFAULT)
        settingsManager = SettingsManager(settingsRepository)

        assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())

        val updatedSettings = Settings(false, true, 3, false)
        `when`(settingsRepository.saveSettings(updatedSettings)).thenReturn(updatedSettings)
        assertEquals(updatedSettings, settingsManager.saveSettings(updatedSettings))
        assertEquals(updatedSettings, settingsManager.observeSettings().first())
    }

    @Test
    fun testSaveFontSizeScale() = testDispatcher.runBlockingTest {
        `when`(settingsRepository.readSettings()).thenReturn(Settings.DEFAULT)
        settingsManager = SettingsManager(settingsRepository)

        assertEquals(Settings.DEFAULT, settingsManager.saveFontSizeScale(Settings.DEFAULT.fontSizeScale))
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(fontSizeScale = 1)
        `when`(settingsRepository.saveSettings(updatedSettings)).thenReturn(updatedSettings)
        assertEquals(updatedSettings, settingsManager.saveFontSizeScale(updatedSettings.fontSizeScale))
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveKeepScreenOn() = testDispatcher.runBlockingTest {
        `when`(settingsRepository.readSettings()).thenReturn(Settings.DEFAULT)
        settingsManager = SettingsManager(settingsRepository)

        assertEquals(Settings.DEFAULT, settingsManager.saveKeepScreenOn(Settings.DEFAULT.keepScreenOn))
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(keepScreenOn = false)
        `when`(settingsRepository.saveSettings(updatedSettings)).thenReturn(updatedSettings)
        assertEquals(updatedSettings, settingsManager.saveKeepScreenOn(updatedSettings.keepScreenOn))
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveNightModeOn() = testDispatcher.runBlockingTest {
        `when`(settingsRepository.readSettings()).thenReturn(Settings.DEFAULT)
        settingsManager = SettingsManager(settingsRepository)

        assertEquals(Settings.DEFAULT, settingsManager.saveNightModeOn(Settings.DEFAULT.nightModeOn))
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(nightModeOn = true)
        `when`(settingsRepository.saveSettings(updatedSettings)).thenReturn(updatedSettings)
        assertEquals(updatedSettings, settingsManager.saveNightModeOn(updatedSettings.nightModeOn))
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveSimpleReadingModeOn() = testDispatcher.runBlockingTest {
        `when`(settingsRepository.readSettings()).thenReturn(Settings.DEFAULT)
        settingsManager = SettingsManager(settingsRepository)

        assertEquals(Settings.DEFAULT, settingsManager.saveSimpleReadingModeOn(Settings.DEFAULT.simpleReadingModeOn))
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(simpleReadingModeOn = true)
        `when`(settingsRepository.saveSettings(updatedSettings)).thenReturn(updatedSettings)
        assertEquals(updatedSettings, settingsManager.saveSimpleReadingModeOn(updatedSettings.simpleReadingModeOn))
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }
}
