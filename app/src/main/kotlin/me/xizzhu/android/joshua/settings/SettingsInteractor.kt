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

import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.BaseInteractor

class SettingsInteractor(settingsManager: SettingsManager) : BaseInteractor(settingsManager) {
    suspend fun saveFontSizeScale(fontSizeScale: Int) {
        currentSettings().takeIf { fontSizeScale != it.fontSizeScale }
                ?.let { settingsManager.saveSettings(it.copy(fontSizeScale = fontSizeScale)) }
    }

    private suspend fun currentSettings(): Settings = settingsManager.settings().first()

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        currentSettings().takeIf { keepScreenOn != it.keepScreenOn }
                ?.let { settingsManager.saveSettings(it.copy(keepScreenOn = keepScreenOn)) }
    }

    suspend fun saveNightModeOn(nightModeOn: Boolean) {
        currentSettings().takeIf { nightModeOn != it.nightModeOn }
                ?.let { settingsManager.saveSettings(it.copy(nightModeOn = nightModeOn)) }
    }

    suspend fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean) {
        currentSettings().takeIf { simpleReadingModeOn != it.simpleReadingModeOn }
                ?.let { settingsManager.saveSettings(it.copy(simpleReadingModeOn = simpleReadingModeOn)) }
    }

    suspend fun saveHideSearchButton(hideSearchButton: Boolean) {
        currentSettings().takeIf { hideSearchButton != it.hideSearchButton }
                ?.let { settingsManager.saveSettings(it.copy(hideSearchButton = hideSearchButton)) }
    }

    suspend fun saveConsolidateVersesForSharing(consolidateVerses: Boolean) {
        currentSettings().takeIf { consolidateVerses != it.consolidateVersesForSharing }
                ?.let { settingsManager.saveSettings(it.copy(consolidateVersesForSharing = consolidateVerses)) }
    }

    suspend fun saveDefaultHighlightColor(@Highlight.Companion.AvailableColor color: Int) {
        currentSettings().takeIf { color != it.defaultHighlightColor }
                ?.let { settingsManager.saveSettings(it.copy(defaultHighlightColor = color)) }
    }
}
