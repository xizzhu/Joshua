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
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.App
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.utils.MVPPresenter

class SettingsPresenter(private val app: App, private val settingsManager: SettingsManager) : MVPPresenter<SettingsView>() {
    companion object {
        private val TAG: String = SettingsPresenter::class.java.simpleName
    }

    private var settings: Settings? = null

    override fun onViewAttached() {
        super.onViewAttached()

        loadSettings()
        loadVersion()
    }

    private fun loadVersion() {
        try {
            val version = app.packageManager.getPackageInfo(app.packageName, 0).versionName
            view?.onVersionLoaded(version)
        } catch (e: Exception) {
            Log.e(TAG, e, "Failed to load app version")
        }
    }

    fun loadSettings() {
        launch(Dispatchers.Main) {
            try {
                onSettingsLoaded(settingsManager.readSettings())
            } catch (e: Exception) {
                Log.e(TAG, e, "Failed to load settings")
                view?.onSettingsLoadFailed()
            }
        }
    }

    private fun onSettingsLoaded(settings: Settings) {
        this.settings = settings
        view?.onSettingsLoaded(settings)
    }

    fun saveSettings(settings: Settings) {
        launch(Dispatchers.Main) {
            try {
                settingsManager.saveSettings(settings)
                onSettingsLoaded(settings)
            } catch (e: Exception) {
                Log.e(TAG, e, "Failed to save settings")
                view?.onSettingsUpdateFailed(settings)
            }
        }
    }

    fun setKeepScreenOn(keepScreenOn: Boolean) {
        val settings: Settings = this.settings ?: Settings.DEFAULT
        if (keepScreenOn != settings.keepScreenOn) {
            saveSettings(settings.toBuilder().keepScreenOn(keepScreenOn).build())
        }
    }
}
