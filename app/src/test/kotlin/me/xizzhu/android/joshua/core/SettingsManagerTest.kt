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
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.repository.SettingsRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var settingsManager: SettingsManager

    @BeforeTest
    override fun setup() {
        super.setup()

        runBlocking {
            `when`(settingsRepository.readSettings()).thenReturn(Settings.DEFAULT)
            settingsManager = SettingsManager(settingsRepository)
        }
    }

    @Test
    fun testObserveInitialSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 3, false, false)
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
        val updatedSettings = Settings(false, true, 3, false, false)
        settingsManager.saveSettings(updatedSettings)
        assertEquals(updatedSettings, settingsManager.observeSettings().first())
    }

    @Test
    fun testSaveFontSizeScale() = testDispatcher.runBlockingTest {
        settingsManager.saveFontSizeScale(Settings.DEFAULT.fontSizeScale)
        assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(fontSizeScale = 1)
        settingsManager.saveFontSizeScale(updatedSettings.fontSizeScale)
        assertEquals(updatedSettings, settingsManager.observeSettings().first())
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveKeepScreenOn() = testDispatcher.runBlockingTest {
        settingsManager.saveKeepScreenOn(Settings.DEFAULT.keepScreenOn)
        assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(keepScreenOn = false)
        settingsManager.saveKeepScreenOn(updatedSettings.keepScreenOn)
        assertEquals(updatedSettings, settingsManager.observeSettings().first())
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveNightModeOn() = testDispatcher.runBlockingTest {
        settingsManager.saveNightModeOn(Settings.DEFAULT.nightModeOn)
        assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(nightModeOn = true)
        settingsManager.saveNightModeOn(updatedSettings.nightModeOn)
        assertEquals(updatedSettings, settingsManager.observeSettings().first())
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveSimpleReadingModeOn() = testDispatcher.runBlockingTest {
        settingsManager.saveSimpleReadingModeOn(Settings.DEFAULT.simpleReadingModeOn)
        assertEquals(Settings.DEFAULT, settingsManager.observeSettings().first())
        verify(settingsRepository, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(simpleReadingModeOn = true)
        settingsManager.saveSimpleReadingModeOn(updatedSettings.simpleReadingModeOn)
        assertEquals(updatedSettings, settingsManager.observeSettings().first())
        verify(settingsRepository, times(1)).saveSettings(updatedSettings)
    }
}
