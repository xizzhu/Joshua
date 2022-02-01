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

package me.xizzhu.android.joshua

import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApp() {
    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        appScope.launch(Dispatchers.Main) {
            val systemNightMode = when (settingsManager.settings().first().nightMode) {
                Settings.NIGHT_MODE_ON -> AppCompatDelegate.MODE_NIGHT_YES
                Settings.NIGHT_MODE_OFF -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(systemNightMode)
        }
    }
}
