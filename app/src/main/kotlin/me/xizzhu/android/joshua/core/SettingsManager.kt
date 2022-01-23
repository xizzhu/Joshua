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

package me.xizzhu.android.joshua.core

import androidx.annotation.IntDef
import kotlinx.coroutines.flow.Flow
import me.xizzhu.android.joshua.core.repository.SettingsRepository

data class Settings(val keepScreenOn: Boolean, @NightMode val nightMode: Int, val fontSizeScale: Float,
                    val simpleReadingModeOn: Boolean, val hideSearchButton: Boolean,
                    val consolidateVersesForSharing: Boolean,
                    @Highlight.Companion.AvailableColor val defaultHighlightColor: Int) {
    companion object {
        const val NIGHT_MODE_ON = 0
        const val NIGHT_MODE_OFF = 1
        const val NIGHT_MODE_FOLLOW_SYSTEM = 2

        @IntDef(NIGHT_MODE_ON, NIGHT_MODE_OFF, NIGHT_MODE_FOLLOW_SYSTEM)
        @Retention(AnnotationRetention.SOURCE)
        annotation class NightMode

        val DEFAULT = Settings(
                keepScreenOn = true, nightMode = NIGHT_MODE_OFF, fontSizeScale = 1.0F,
                simpleReadingModeOn = false, hideSearchButton = false, consolidateVersesForSharing = false,
                defaultHighlightColor = Highlight.COLOR_NONE
        )
    }
}

class SettingsManager(private val settingsRepository: SettingsRepository) {
    fun settings(): Flow<Settings> = settingsRepository.settings

    suspend fun saveSettings(settings: Settings) {
        settingsRepository.saveSettings(settings)
    }
}
