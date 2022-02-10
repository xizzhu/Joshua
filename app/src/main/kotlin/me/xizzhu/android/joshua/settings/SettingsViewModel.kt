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

package me.xizzhu.android.joshua.settings

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.logger.Log
import java.io.IOException
import javax.inject.Inject

data class SettingsViewData(
        val currentFontSizeScale: Float, val bodyTextSizeInPixel: Float, val captionTextSizeInPixel: Float,
        val keepScreenOn: Boolean, val nightMode: NightMode, val simpleReadingModeOn: Boolean,
        val hideSearchButton: Boolean, val consolidateVersesForSharing: Boolean,
        val defaultHighlightColor: HighlightColor, val version: String
) {
    enum class NightMode(
            @AppCompatDelegate.NightMode val systemValue: Int, @Settings.Companion.NightMode val nightMode: Int, @StringRes val label: Int
    ) {
        ON(AppCompatDelegate.MODE_NIGHT_YES, Settings.NIGHT_MODE_ON, R.string.settings_text_night_mode_on),
        OFF(AppCompatDelegate.MODE_NIGHT_NO, Settings.NIGHT_MODE_OFF, R.string.settings_text_night_mode_off),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, Settings.NIGHT_MODE_FOLLOW_SYSTEM, R.string.settings_text_night_mode_system);

        companion object {
            fun fromNightMode(@Settings.Companion.NightMode nightMode: Int): NightMode =
                    values().firstOrNull { it.nightMode == nightMode } ?: SYSTEM
        }
    }

    enum class HighlightColor(@Highlight.Companion.AvailableColor val color: Int, @StringRes val label: Int) {
        NONE(Highlight.COLOR_NONE, R.string.text_highlight_color_none),
        YELLOW(Highlight.COLOR_YELLOW, R.string.text_highlight_color_yellow),
        PINK(Highlight.COLOR_PINK, R.string.text_highlight_color_pink),
        ORANGE(Highlight.COLOR_ORANGE, R.string.text_highlight_color_orange),
        PURPLE(Highlight.COLOR_PURPLE, R.string.text_highlight_color_purple),
        RED(Highlight.COLOR_RED, R.string.text_highlight_color_red),
        GREEN(Highlight.COLOR_GREEN, R.string.text_highlight_color_green),
        BLUE(Highlight.COLOR_BLUE, R.string.text_highlight_color_blue);

        companion object {
            fun fromHighlightColor(@Highlight.Companion.AvailableColor color: Int): HighlightColor =
                    values().first { it.color == color }
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val backupManager: BackupManager, settingsManager: SettingsManager, application: Application
) : BaseViewModel(settingsManager, application) {
    private val settingsViewData: MutableStateFlow<ViewData<SettingsViewData>> = MutableStateFlow(ViewData.Loading())

    init {
        val version = try {
            application.packageManager.getPackageInfo(application.packageName, 0).versionName
        } catch (e: Exception) {
            Log.e(tag, "Failed to load app version", e)
            ""
        }

        settings().onEach { settings ->
            val resources = application.resources
            settingsViewData.value = ViewData.Success(SettingsViewData(
                    currentFontSizeScale = settings.fontSizeScale,
                    bodyTextSizeInPixel = settings.getPrimaryTextSize(resources),
                    captionTextSizeInPixel = settings.getSecondaryTextSize(resources),
                    keepScreenOn = settings.keepScreenOn,
                    nightMode = SettingsViewData.NightMode.fromNightMode(settings.nightMode),
                    simpleReadingModeOn = settings.simpleReadingModeOn,
                    hideSearchButton = settings.hideSearchButton,
                    consolidateVersesForSharing = settings.consolidateVersesForSharing,
                    defaultHighlightColor = SettingsViewData.HighlightColor.fromHighlightColor(settings.defaultHighlightColor),
                    version = version
            ))
        }.launchIn(viewModelScope)
    }

    fun settingsViewData(): Flow<ViewData<SettingsViewData>> = settingsViewData

    fun currentSettingsViewData(): SettingsViewData? = settingsViewData.value.let { if (it is ViewData.Success) it.data else null }

    fun saveFontSizeScale(fontSizeScale: Float): Flow<ViewData<Unit>> = updateSettings { it.copy(fontSizeScale = fontSizeScale) }

    private inline fun updateSettings(crossinline op: (current: Settings) -> Settings): Flow<ViewData<Unit>> = viewData<Unit> {
        val current = settings().first()
        op(current).takeIf { it != current }?.let { settingsManager.saveSettings(it) }
    }.onFailure { Log.e(tag, "Failed to save settings", it) }

    fun saveKeepScreenOn(keepScreenOn: Boolean): Flow<ViewData<Unit>> = updateSettings { it.copy(keepScreenOn = keepScreenOn) }

    fun saveNightMode(nightMode: SettingsViewData.NightMode): Flow<ViewData<Unit>> = updateSettings {
        it.copy(nightMode = nightMode.nightMode)
    }.onSuccess {
        // This will restart the activity, so do it AFTER the settings are successfully saved.
        AppCompatDelegate.setDefaultNightMode(nightMode.systemValue)
    }

    fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean): Flow<ViewData<Unit>> =
            updateSettings { it.copy(simpleReadingModeOn = simpleReadingModeOn) }

    fun saveHideSearchButton(hideSearchButton: Boolean): Flow<ViewData<Unit>> = updateSettings { it.copy(hideSearchButton = hideSearchButton) }

    fun saveConsolidateVersesForSharing(consolidateVerses: Boolean): Flow<ViewData<Unit>> =
            updateSettings { it.copy(consolidateVersesForSharing = consolidateVerses) }

    fun saveDefaultHighlightColor(highlightColor: SettingsViewData.HighlightColor): Flow<ViewData<Unit>> =
            updateSettings { it.copy(defaultHighlightColor = highlightColor.color) }

    fun backup(uri: Uri?): Flow<ViewData<Int>> = flow {
        if (uri == null) {
            emit(ViewData.Failure(IllegalArgumentException("Null URI")))
            return@flow
        }

        emit(ViewData.Loading())
        try {
            application.contentResolver.openOutputStream(uri)
                    ?.use { backupManager.backup(it) }
                    ?: throw IOException("Failed to open Uri for backup - $uri")
            emit(ViewData.Success(R.string.toast_backed_up))
        } catch (e: Exception) {
            Log.e(tag, "Failed to backup data", e)
            emit(ViewData.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun restore(uri: Uri?): Flow<ViewData<Int>> = flow {
        if (uri == null) {
            emit(ViewData.Failure(IllegalArgumentException("Null URI")))
            return@flow
        }

        emit(ViewData.Loading())
        try {
            application.contentResolver.openInputStream(uri)
                    ?.use { backupManager.restore(it) }
                    ?: throw IOException("Failed to open Uri for restore - $uri")
            emit(ViewData.Success(R.string.toast_restored))
        } catch (t: Throwable) {
            when (t) {
                is Exception, is OutOfMemoryError -> {
                    // Catching OutOfMemoryError here, because there're cases when users try to
                    // open a huge file.
                    // See https://console.firebase.google.com/u/0/project/joshua-production/crashlytics/app/android:me.xizzhu.android.joshua/issues/e9339c69d6e1856856db88413614d3d3
                    Log.e(tag, "Failed to restore data", t)
                    emit(ViewData.Failure(t))
                }
                else -> throw t
            }
        }
    }.flowOn(Dispatchers.IO)
}
