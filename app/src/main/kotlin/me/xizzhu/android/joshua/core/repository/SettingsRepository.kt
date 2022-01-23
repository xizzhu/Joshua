/*
 * Copyright (C) 2022 Xizhi Zhu
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.repository.local.LocalSettingsStorage
import me.xizzhu.android.logger.Log

class SettingsRepository(private val localSettingsStorage: LocalSettingsStorage, appScope: CoroutineScope) {
    companion object {
        private val TAG = SettingsRepository::class.java.simpleName
    }

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: Flow<Settings> = _settings.filterNotNull()

    init {
        appScope.launch {
            try {
                _settings.value = localSettingsStorage.readSettings()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize settings", e)
                _settings.value = Settings.DEFAULT
            }
        }
    }

    suspend fun saveSettings(settings: Settings) {
        if (settings == _settings.value) return

        localSettingsStorage.saveSettings(settings)
        _settings.value = settings
    }
}
