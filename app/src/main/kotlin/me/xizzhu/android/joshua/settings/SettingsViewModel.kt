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
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import java.io.InputStream
import java.io.OutputStream

class SettingsViewModel(settingsManager: SettingsManager,
                        private val backupManager: BackupManager) : BaseSettingsViewModel(settingsManager) {
    suspend fun saveFontSizeScale(fontSizeScale: Int) {
        currentSettings().let { current ->
            if (fontSizeScale != current.fontSizeScale) {
                settingsManager.saveSettings(current.copy(fontSizeScale = fontSizeScale))
            }
        }
    }

    private suspend fun currentSettings(): Settings = settingsManager.settings().first()

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        currentSettings().let { current ->
            if (keepScreenOn != current.keepScreenOn) {
                settingsManager.saveSettings(current.copy(keepScreenOn = keepScreenOn))
            }
        }
    }

    suspend fun saveNightModeOn(nightModeOn: Boolean) {
        currentSettings().let { current ->
            if (nightModeOn != current.nightModeOn) {
                settingsManager.saveSettings(current.copy(nightModeOn = nightModeOn))
            }
        }
    }

    suspend fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean) {
        currentSettings().let { current ->
            if (simpleReadingModeOn != current.simpleReadingModeOn) {
                settingsManager.saveSettings(current.copy(simpleReadingModeOn = simpleReadingModeOn))
            }
        }
    }

    suspend fun saveHideSearchButton(hideSearchButton: Boolean) {
        currentSettings().let { current ->
            if (hideSearchButton != current.hideSearchButton) {
                settingsManager.saveSettings(current.copy(hideSearchButton = hideSearchButton))
            }
        }
    }

    suspend fun saveConsolidateVersesForSharing(consolidateVerses: Boolean) {
        currentSettings().let { current ->
            if (consolidateVerses != current.consolidateVersesForSharing) {
                settingsManager.saveSettings(current.copy(consolidateVersesForSharing = consolidateVerses))
            }
        }
    }

    suspend fun saveDefaultHighlightColor(@Highlight.Companion.AvailableColor color: Int) {
        currentSettings().let { current ->
            if (color != current.defaultHighlightColor) {
                settingsManager.saveSettings(current.copy(defaultHighlightColor = color))
            }
        }
    }

    suspend fun backup(to: OutputStream) {
        to.write(backupManager.prepareForBackup().toByteArray(Charsets.UTF_8))
    }

    suspend fun restore(from: InputStream) {
        backupManager.restore(String(from.readBytes(), Charsets.UTF_8))
    }
}
