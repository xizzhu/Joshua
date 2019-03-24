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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.SettingsRepository

data class Settings(val keepScreenOn: Boolean, val nightModeOn: Boolean) {
    companion object {
        val DEFAULT = Settings(true, false)
    }

    data class Builder(var keepScreenOn: Boolean, var nightModeOn: Boolean) {
        fun keepScreenOn(keepScreenOn: Boolean) = apply { this.keepScreenOn = keepScreenOn }
        fun nightModeOn(nightModeOn: Boolean) = apply { this.nightModeOn = nightModeOn }
        fun build() = Settings(keepScreenOn, nightModeOn)
    }

    fun toBuilder(): Builder = Builder(keepScreenOn, nightModeOn)
}

class SettingsManager(private val settingsRepository: SettingsRepository) {
    private val currentSettings: BroadcastChannel<Settings> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) { currentSettings.send(settingsRepository.readSettings()) }
    }

    fun observeSettings(): ReceiveChannel<Settings> = currentSettings.openSubscription()

    suspend fun saveSettings(settings: Settings) {
        settingsRepository.saveSettings(settings)
        currentSettings.send(settings)
    }
}
