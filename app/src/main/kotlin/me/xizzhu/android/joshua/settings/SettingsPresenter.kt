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

package me.xizzhu.android.joshua.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.App
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.utils.MVPPresenter

class SettingsPresenter(private val app: App, private val settingsManager: SettingsManager) : MVPPresenter<SettingsView>() {
    private var settings: Settings = Settings.DEFAULT

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            val currentSettings = settingsManager.observeSettings()
            receiveChannels.add(currentSettings)
            currentSettings.consumeEach {
                settings = it
                view?.onSettingsUpdated(it)
            }
        }

        try {
            val version = app.packageManager.getPackageInfo(app.packageName, 0).versionName
            view?.onVersionLoaded(version)
        } catch (e: Exception) {
            Log.e(tag, e, "Failed to load app version")
        }
    }

    fun saveSettings(settings: Settings) {
        launch(Dispatchers.Main) {
            try {
                settingsManager.saveSettings(settings)
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to save settings")
                view?.onSettingsUpdateFailed(settings)
            }
        }
    }

    fun setFontSizeScale(fontSizeScale: Int) {
        launch(Dispatchers.Main) {
            if (fontSizeScale != settings.fontSizeScale) {
                saveSettings(settings.toBuilder().fontSizeScale(fontSizeScale).build())
            }
        }
    }

    fun setKeepScreenOn(keepScreenOn: Boolean) {
        launch(Dispatchers.Main) {
            if (keepScreenOn != settings.keepScreenOn) {
                saveSettings(settings.toBuilder().keepScreenOn(keepScreenOn).build())
            }
        }
    }

    fun setNightModeOn(nightModeOn: Boolean) {
        launch(Dispatchers.Main) {
            if (nightModeOn != settings.nightModeOn) {
                saveSettings(settings.toBuilder().nightModeOn(nightModeOn).build())
            }
        }
    }
}
