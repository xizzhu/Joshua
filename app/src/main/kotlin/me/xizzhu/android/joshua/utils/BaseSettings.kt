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

package me.xizzhu.android.joshua.utils

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.ui.getBackgroundColor

abstract class BaseSettingsActivity : BaseActivity() {
    @CallSuper
    open fun onSettingsLoaded(settings: Settings) {
        val rootView = window.decorView
        rootView.keepScreenOn = settings.keepScreenOn
        rootView.setBackgroundColor(settings.getBackgroundColor())
    }
}

abstract class BaseSettingsInteractor(private val settingsManager: SettingsManager) {
    suspend fun loadSettings(): Settings = settingsManager.readSettings()
}

interface BaseSettingsView : MVPView {
    fun onSettingsLoaded(settings: Settings)
}

abstract class BaseSettingsPresenter<V : BaseSettingsView>(private val baseSettingsInteractor: BaseSettingsInteractor)
    : MVPPresenter<V>() {
    fun loadSettings() {
        launch(Dispatchers.Main) {
            view?.onSettingsLoaded(baseSettingsInteractor.loadSettings())
            try {
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load settings")
                view?.onSettingsLoaded(Settings.DEFAULT)
            }
        }
    }
}
