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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.xizzhu.android.joshua.core.repository.SettingsRepository
import me.xizzhu.android.logger.Log

data class Settings(val keepScreenOn: Boolean, val nightModeOn: Boolean, val fontSizeScale: Int,
                    val simpleReadingModeOn: Boolean) {
    companion object {
        val DEFAULT = Settings(true, false, 2, false)
    }
}

class SettingsManager(private val settingsRepository: SettingsRepository) {
    companion object {
        private val TAG = SettingsManager::class.java.simpleName
    }

    private val mutex: Mutex = Mutex()

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val currentSettings: BroadcastChannel<Settings> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                currentSettings.offer(readSettings())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize settings", e)
                currentSettings.offer(Settings.DEFAULT)
            }
        }
    }

    fun observeSettings(): Flow<Settings> = currentSettings.asFlow()

    suspend fun readSettings(): Settings = mutex.withLock { settingsRepository.readSettings() }

    suspend fun saveFontSizeScale(fontSizeScale: Int): Settings = mutex.withLock {
        return settingsRepository.readSettings().let { settings ->
            if (fontSizeScale != settings.fontSizeScale) {
                saveSettings(settings.copy(fontSizeScale = fontSizeScale))
            } else {
                settings
            }
        }
    }

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean): Settings = mutex.withLock {
        return settingsRepository.readSettings().let { settings ->
            if (keepScreenOn != settings.keepScreenOn) {
                saveSettings(settings.copy(keepScreenOn = keepScreenOn))
            } else {
                settings
            }
        }
    }

    suspend fun saveNightModeOn(nightModeOn: Boolean): Settings = mutex.withLock {
        return settingsRepository.readSettings().let { settings ->
            if (nightModeOn != settings.nightModeOn) {
                saveSettings(settings.copy(nightModeOn = nightModeOn))
            } else {
                settings
            }
        }
    }

    suspend fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean): Settings = mutex.withLock {
        return settingsRepository.readSettings().let { settings ->
            if (simpleReadingModeOn != settings.simpleReadingModeOn) {
                saveSettings(settings.copy(simpleReadingModeOn = simpleReadingModeOn))
            } else {
                settings
            }
        }
    }

    @VisibleForTesting
    suspend fun saveSettings(settings: Settings): Settings =
            settingsRepository.saveSettings(settings).also { currentSettings.offer(it) }
}
