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

package me.xizzhu.android.joshua.settings

import android.app.Application
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
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
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.logger.Log
import java.io.IOException
import javax.inject.Inject

enum class HighlightColorViewData(@Highlight.Companion.AvailableColor val color: Int, @StringRes val label: Int) {
    NONE(Highlight.COLOR_NONE, R.string.text_highlight_color_none),
    YELLOW(Highlight.COLOR_YELLOW, R.string.text_highlight_color_yellow),
    PINK(Highlight.COLOR_PINK, R.string.text_highlight_color_pink),
    PURPLE(Highlight.COLOR_PURPLE, R.string.text_highlight_color_purple),
    GREEN(Highlight.COLOR_GREEN, R.string.text_highlight_color_green),
    BLUE(Highlight.COLOR_BLUE, R.string.text_highlight_color_blue);

    companion object {
        fun fromHighlightColor(@Highlight.Companion.AvailableColor color: Int): HighlightColorViewData =
                values()[Highlight.AVAILABLE_COLORS.indexOf(color)]
    }
}

class SettingsViewData(
        val fontSizes: Array<String>, val currentFontSize: Int, val animateFontSize: Boolean,
        val bodyTextSizeInPixel: Float, val captionTextSizeInPixel: Float,
        val keepScreenOn: Boolean, val nightModeOn: Boolean, val animateColor: Boolean,
        @ColorInt val backgroundColor: Int, @ColorInt val primaryTextColor: Int, @ColorInt val secondaryTextColor: Int,
        val simpleReadingModeOn: Boolean, val hideSearchButton: Boolean, val consolidateVersesForSharing: Boolean,
        val defaultHighlightColor: HighlightColorViewData,
        val version: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val backupManager: BackupManager, settingsManager: SettingsManager, application: Application
) : BaseViewModel(settingsManager, application) {
    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val fontSizeTexts: Array<String> = arrayOf(".5x", "1x", "1.5x", "2x", "2.5x", "3x")
    }

    private val settingsViewData: MutableStateFlow<ViewData<SettingsViewData>?> = MutableStateFlow(null)

    private var shouldAnimateFontSize = false
    private var shouldAnimateColor = false

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
                    fontSizes = fontSizeTexts,
                    currentFontSize = settings.fontSizeScale - 1,
                    animateFontSize = shouldAnimateFontSize,
                    bodyTextSizeInPixel = settings.getBodyTextSize(resources),
                    captionTextSizeInPixel = settings.getCaptionTextSize(resources),
                    keepScreenOn = settings.keepScreenOn,
                    nightModeOn = settings.nightModeOn,
                    animateColor = shouldAnimateColor,
                    backgroundColor = settings.getBackgroundColor(),
                    primaryTextColor = settings.getPrimaryTextColor(resources),
                    secondaryTextColor = settings.getSecondaryTextColor(resources),
                    simpleReadingModeOn = settings.simpleReadingModeOn,
                    hideSearchButton = settings.hideSearchButton,
                    consolidateVersesForSharing = settings.consolidateVersesForSharing,
                    defaultHighlightColor = HighlightColorViewData.fromHighlightColor(settings.defaultHighlightColor),
                    version = version
            ))

            shouldAnimateFontSize = false
            shouldAnimateColor = false
        }.launchIn(viewModelScope)
    }

    fun settingsViewData(): Flow<ViewData<SettingsViewData>> = settingsViewData.filterNotNull()

    fun saveFontSizeScale(fontSizeScale: Int): Flow<ViewData<Unit>> = updateSettings {
        shouldAnimateFontSize = true
        settingsManager.saveSettings(currentSettings().copy(fontSizeScale = fontSizeScale + 1))
    }

    private inline fun updateSettings(crossinline op: suspend () -> Unit): Flow<ViewData<Unit>> =
            viewData(op).onFailure { Log.e(tag, "Failed to save settings", it) }

    private suspend fun currentSettings(): Settings = settings().first()

    fun saveKeepScreenOn(keepScreenOn: Boolean): Flow<ViewData<Unit>> = updateSettings {
        settingsManager.saveSettings(currentSettings().copy(keepScreenOn = keepScreenOn))
    }

    fun saveNightModeOn(nightModeOn: Boolean): Flow<ViewData<Unit>> = updateSettings {
        shouldAnimateColor = true
        settingsManager.saveSettings(currentSettings().copy(nightModeOn = nightModeOn))
    }

    fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean): Flow<ViewData<Unit>> = updateSettings {
        settingsManager.saveSettings(currentSettings().copy(simpleReadingModeOn = simpleReadingModeOn))
    }

    fun saveHideSearchButton(hideSearchButton: Boolean): Flow<ViewData<Unit>> = updateSettings {
        settingsManager.saveSettings(currentSettings().copy(hideSearchButton = hideSearchButton))
    }

    fun saveConsolidateVersesForSharing(consolidateVerses: Boolean): Flow<ViewData<Unit>> = updateSettings {
        settingsManager.saveSettings(currentSettings().copy(consolidateVersesForSharing = consolidateVerses))
    }

    fun saveDefaultHighlightColor(highlightColor: HighlightColorViewData): Flow<ViewData<Unit>> = updateSettings {
        settingsManager.saveSettings(currentSettings().copy(defaultHighlightColor = highlightColor.color))
    }

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
