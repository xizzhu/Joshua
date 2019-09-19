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

import androidx.annotation.UiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.logger.Log
import java.io.OutputStream

class SettingsInteractor(private val settingsManager: SettingsManager,
                         private val backupManager: BackupManager) : Interactor() {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val settings: ConflatedBroadcastChannel<ViewData<Settings>> = ConflatedBroadcastChannel()
    private val settingsSaved: BroadcastChannel<ViewData<Settings>> = ConflatedBroadcastChannel()
    private val backuped: BroadcastChannel<ViewData<String>> = ConflatedBroadcastChannel()
    private val restored: BroadcastChannel<ViewData<String>> = ConflatedBroadcastChannel()

    @UiThread
    override fun onStarted() {
        super.onStarted()
        coroutineScope.launch {
            settingsManager.observeSettings().map { ViewData.success(it) }.collect { settings.offer(it) }
        }
    }

    suspend fun prepareForBackup(): String = backupManager.prepareForBackup()

    fun getSettings(): Settings = settings.valueOrNull?.data ?: Settings.DEFAULT

    fun settings(): Flow<ViewData<Settings>> = settings.asFlow()

    fun settingsSaved(): Flow<ViewData<Settings>> = settingsSaved.asFlow()

    fun backuped(): Flow<ViewData<String>> = backuped.asFlow()

    fun restored(): Flow<ViewData<String>> = restored.asFlow()

    fun saveSettings(settings: Settings) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                settingsManager.saveSettings(settings)
                settingsSaved.offer(ViewData.success(settings))
            } catch (e: Exception) {
                Log.e(tag, "Failed to save settings", e)
                settingsSaved.offer(ViewData.error(settings, e))
            }
        }
    }

    fun setFontSizeScale(fontSizeScale: Int) {
        getSettings().let { settings ->
            if (fontSizeScale != settings.fontSizeScale) {
                saveSettings(settings.copy(fontSizeScale = fontSizeScale))
            }
        }
    }

    fun setKeepScreenOn(keepScreenOn: Boolean) {
        getSettings().let { settings ->
            if (keepScreenOn != settings.keepScreenOn) {
                saveSettings(settings.copy(keepScreenOn = keepScreenOn))
            }
        }
    }

    fun setNightModeOn(nightModeOn: Boolean) {
        getSettings().let { settings ->
            if (nightModeOn != settings.nightModeOn) {
                saveSettings(settings.copy(nightModeOn = nightModeOn))
            }
        }
    }

    fun setSimpleReadingModeOn(simpleReadingModeOn: Boolean) {
        getSettings().let { settings ->
            if (simpleReadingModeOn != settings.simpleReadingModeOn) {
                saveSettings(settings.copy(simpleReadingModeOn = simpleReadingModeOn))
            }
        }
    }

    fun backup(outputStream: OutputStream) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                outputStream.write(backupManager.prepareForBackup().toByteArray(Charsets.UTF_8))

                backuped.offer(ViewData.success(backupManager.prepareForBackup()))
            } catch (e: Exception) {
                Log.e(tag, "Failed to backup data", e)
                backuped.offer(ViewData.error("", e))
            }
        }
    }

    fun restore(data: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                backupManager.restore(data)
                restored.offer(ViewData.success(""))
            } catch (e: Exception) {
                Log.e(tag, "Failed to backup data", e)
                restored.offer(ViewData.error(data, e))
            }
        }
    }
}
