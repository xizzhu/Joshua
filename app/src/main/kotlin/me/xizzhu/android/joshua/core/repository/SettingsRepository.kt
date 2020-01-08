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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.repository.local.LocalSettingsStorage
import me.xizzhu.android.logger.Log

class SettingsRepository(private val localSettingsStorage: LocalSettingsStorage,
                         initDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    companion object {
        private val TAG = SettingsRepository::class.java.simpleName
    }

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val currentSettings: BroadcastChannel<Settings> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(initDispatcher) {
            try {
                currentSettings.offer(localSettingsStorage.readSettings())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize settings", e)
                currentSettings.offer(Settings.DEFAULT)
            }
        }
    }

    fun settings(): Flow<Settings> = currentSettings.asFlow()

    suspend fun saveSettings(settings: Settings) {
        localSettingsStorage.saveSettings(settings)
        currentSettings.offer(settings)
    }
}
