/*
 * Copyright (C) 2021 Xizhi Zhu
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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SettingsInteractorTest : BaseUnitTest() {
    private lateinit var settingsManager: SettingsManager

    private lateinit var settingsInteractor: SettingsInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        settingsManager = mockk()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(any()) } returns Unit

        settingsInteractor = SettingsInteractor(settingsManager)
    }

    @Test
    fun testSaveFontSizeScale() = runBlocking {
        settingsInteractor.saveFontSizeScale(Settings.DEFAULT.fontSizeScale)
        coVerify(exactly = 0) { settingsManager.saveSettings((any())) }

        val updatedSettings = Settings.DEFAULT.copy(fontSizeScale = 1)
        settingsInteractor.saveFontSizeScale(updatedSettings.fontSizeScale)
        coVerify(exactly = 1) { settingsManager.saveSettings(updatedSettings) }
    }

    @Test
    fun testSaveKeepScreenOn() = runBlocking {
        settingsInteractor.saveKeepScreenOn(Settings.DEFAULT.keepScreenOn)
        coVerify(exactly = 0) { settingsManager.saveSettings((any())) }

        val updatedSettings = Settings.DEFAULT.copy(keepScreenOn = false)
        settingsInteractor.saveKeepScreenOn(updatedSettings.keepScreenOn)
        coVerify(exactly = 1) { settingsManager.saveSettings(updatedSettings) }
    }

    @Test
    fun testSaveNightModeOn() = runBlocking {
        settingsInteractor.saveNightModeOn(Settings.DEFAULT.nightModeOn)
        coVerify(exactly = 0) { settingsManager.saveSettings((any())) }

        val updatedSettings = Settings.DEFAULT.copy(nightModeOn = true)
        settingsInteractor.saveNightModeOn(updatedSettings.nightModeOn)
        coVerify(exactly = 1) { settingsManager.saveSettings(updatedSettings) }
    }

    @Test
    fun testSaveSimpleReadingModeOn() = runBlocking {
        settingsInteractor.saveSimpleReadingModeOn(Settings.DEFAULT.simpleReadingModeOn)
        coVerify(exactly = 0) { settingsManager.saveSettings((any())) }

        val updatedSettings = Settings.DEFAULT.copy(simpleReadingModeOn = true)
        settingsInteractor.saveSimpleReadingModeOn(updatedSettings.simpleReadingModeOn)
        coVerify(exactly = 1) { settingsManager.saveSettings(updatedSettings) }
    }

    @Test
    fun testSaveHideSearchButton() = runBlocking {
        settingsInteractor.saveHideSearchButton(Settings.DEFAULT.hideSearchButton)
        coVerify(exactly = 0) { settingsManager.saveSettings((any())) }

        val updatedSettings = Settings.DEFAULT.copy(hideSearchButton = true)
        settingsInteractor.saveHideSearchButton(updatedSettings.hideSearchButton)
        coVerify(exactly = 1) { settingsManager.saveSettings(updatedSettings) }
    }

    @Test
    fun testSaveConsolidateVersesForSharing() = runBlocking {
        settingsInteractor.saveConsolidateVersesForSharing(Settings.DEFAULT.consolidateVersesForSharing)
        coVerify(exactly = 0) { settingsManager.saveSettings((any())) }

        val updatedSettings = Settings.DEFAULT.copy(consolidateVersesForSharing = true)
        settingsInteractor.saveConsolidateVersesForSharing(updatedSettings.consolidateVersesForSharing)
        coVerify(exactly = 1) { settingsManager.saveSettings(updatedSettings) }
    }

    @Test
    fun testSaveDefaultHighlightColor() = runBlocking {
        settingsInteractor.saveDefaultHighlightColor(Settings.DEFAULT.defaultHighlightColor)
        coVerify(exactly = 0) { settingsManager.saveSettings((any())) }

        val updatedSettings = Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_PURPLE)
        settingsInteractor.saveDefaultHighlightColor(updatedSettings.defaultHighlightColor)
        coVerify(exactly = 1) { settingsManager.saveSettings(updatedSettings) }
    }
}
