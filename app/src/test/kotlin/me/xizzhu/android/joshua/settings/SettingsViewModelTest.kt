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

package me.xizzhu.android.joshua.settings

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class SettingsViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsManager: SettingsManager

    @Mock
    private lateinit var backupManager: BackupManager

    private lateinit var settingsViewModel: SettingsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(settingsManager.settings()).thenReturn(flowOf(Settings.DEFAULT))
        settingsViewModel = SettingsViewModel(settingsManager, backupManager)
    }

    @Test
    fun testSaveFontSizeScale() = runBlocking {
        settingsViewModel.saveFontSizeScale(Settings.DEFAULT.fontSizeScale)
        verify(settingsManager, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(fontSizeScale = 1)
        settingsViewModel.saveFontSizeScale(updatedSettings.fontSizeScale)
        verify(settingsManager, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveKeepScreenOn() = runBlocking {
        settingsViewModel.saveKeepScreenOn(Settings.DEFAULT.keepScreenOn)
        verify(settingsManager, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(keepScreenOn = false)
        settingsViewModel.saveKeepScreenOn(updatedSettings.keepScreenOn)
        verify(settingsManager, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveNightModeOn() = runBlocking {
        settingsViewModel.saveNightModeOn(Settings.DEFAULT.nightModeOn)
        verify(settingsManager, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(nightModeOn = true)
        settingsViewModel.saveNightModeOn(updatedSettings.nightModeOn)
        verify(settingsManager, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveSimpleReadingModeOn() = runBlocking {
        settingsViewModel.saveSimpleReadingModeOn(Settings.DEFAULT.simpleReadingModeOn)
        verify(settingsManager, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(simpleReadingModeOn = true)
        settingsViewModel.saveSimpleReadingModeOn(updatedSettings.simpleReadingModeOn)
        verify(settingsManager, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveHideSearchButton() = runBlocking {
        settingsViewModel.saveHideSearchButton(Settings.DEFAULT.hideSearchButton)
        verify(settingsManager, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(hideSearchButton = true)
        settingsViewModel.saveHideSearchButton(updatedSettings.hideSearchButton)
        verify(settingsManager, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveConsolidateVersesForSharing() = runBlocking {
        settingsViewModel.saveConsolidateVersesForSharing(Settings.DEFAULT.consolidateVersesForSharing)
        verify(settingsManager, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(consolidateVersesForSharing = true)
        settingsViewModel.saveConsolidateVersesForSharing(updatedSettings.consolidateVersesForSharing)
        verify(settingsManager, times(1)).saveSettings(updatedSettings)
    }

    @Test
    fun testSaveDefaultHighlightColor() = runBlocking {
        settingsViewModel.saveDefaultHighlightColor(Settings.DEFAULT.defaultHighlightColor)
        verify(settingsManager, never()).saveSettings(any())

        val updatedSettings = Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_PURPLE)
        settingsViewModel.saveDefaultHighlightColor(updatedSettings.defaultHighlightColor)
        verify(settingsManager, times(1)).saveSettings(updatedSettings)
    }
}
