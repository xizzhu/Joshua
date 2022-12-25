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
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.ui.getPrimaryTextSize
import me.xizzhu.android.joshua.ui.getSecondaryTextSize
import me.xizzhu.android.joshua.utils.appVersionName
import me.xizzhu.android.logger.Log

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: BackupManager,
    private val settingsManager: SettingsManager,
    private val application: Application,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : BaseViewModelV2<SettingsViewModel.ViewAction, SettingsViewModel.ViewState>(
    initialViewState = ViewState(
        fontSizeScale = Settings.DEFAULT.fontSizeScale,
        bodyTextSizePx = Settings.DEFAULT.getPrimaryTextSize(application.resources).roundToInt(),
        captionTextSizePx = Settings.DEFAULT.getSecondaryTextSize(application.resources).roundToInt(),
        keepScreenOn = Settings.DEFAULT.keepScreenOn,
        nightModeStringRes = nightModeStringResource(Settings.DEFAULT.nightMode),
        simpleReadingModeOn = Settings.DEFAULT.simpleReadingModeOn,
        hideSearchButton = Settings.DEFAULT.hideSearchButton,
        consolidateVersesForSharing = Settings.DEFAULT.consolidateVersesForSharing,
        defaultHighlightColorStringRes = highlightColorStringResource(Settings.DEFAULT.defaultHighlightColor),
        backupState = ViewState.BackupRestoreState.Idle,
        restoreState = ViewState.BackupRestoreState.Idle,
        version = "",
        fontSizeScaleSelection = null,
        nightModeSelection = null,
        highlightColorSelection = null,
        error = null,
    )
) {
    sealed class ViewAction {
        object OpenRateMe : ViewAction()
        object OpenWebsite : ViewAction()
        object RequestUriForBackup : ViewAction()
        object RequestUriForRestore : ViewAction()
    }

    data class ViewState(
        val fontSizeScale: Float,
        @Px val bodyTextSizePx: Int,
        @Px val captionTextSizePx: Int,
        val keepScreenOn: Boolean,
        @StringRes val nightModeStringRes: Int,
        val simpleReadingModeOn: Boolean,
        val hideSearchButton: Boolean,
        val consolidateVersesForSharing: Boolean,
        @StringRes val defaultHighlightColorStringRes: Int,
        val backupState: BackupRestoreState,
        val restoreState: BackupRestoreState,
        val version: String,
        val fontSizeScaleSelection: FontSizeScaleSelection?,
        val nightModeSelection: NightModeSelection?,
        val highlightColorSelection: HighlightColorSelection?,
        val error: Error?,
    ) {
        sealed class BackupRestoreState {
            object Idle : BackupRestoreState()
            object Ongoing : BackupRestoreState()
            data class Completed(val successful: Boolean) : BackupRestoreState()
        }

        data class FontSizeScaleSelection(
            @Px val currentBodyTextSizePx: Int,
            @Px val currentCaptionTextSizePx: Int,
            val currentScale: Float,
            val minScale: Float,
            val maxScale: Float,
        )

        data class NightModeSelection(val currentPosition: Int, val availableModes: List<NightMode>) {
            data class NightMode(@Settings.Companion.NightMode val nightMode: Int, @StringRes val stringRes: Int)
        }

        data class HighlightColorSelection(val currentPosition: Int, val availableColors: List<HighlightColor>) {
            data class HighlightColor(@Highlight.Companion.AvailableColor val color: Int, @StringRes val stringRes: Int)
        }

        sealed class Error {
            object BackupError : Error()
            object RestoreError : Error()
            object SettingsUpdatingError : Error()
        }
    }

    init {
        updateViewState { it.copy(version = runCatching { application.appVersionName }.getOrDefault("")) }

        settingsManager.settings().onEach { settings ->
            updateViewState { current ->
                current.copy(
                    fontSizeScale = settings.fontSizeScale,
                    bodyTextSizePx = settings.getPrimaryTextSize(application.resources).roundToInt(),
                    captionTextSizePx = settings.getSecondaryTextSize(application.resources).roundToInt(),
                    keepScreenOn = settings.keepScreenOn,
                    nightModeStringRes = nightModeStringResource(settings.nightMode),
                    simpleReadingModeOn = settings.simpleReadingModeOn,
                    hideSearchButton = settings.hideSearchButton,
                    consolidateVersesForSharing = settings.consolidateVersesForSharing,
                    defaultHighlightColorStringRes = highlightColorStringResource(settings.defaultHighlightColor),
                )
            }
        }.launchIn(viewModelScope)
    }

    fun saveFontSizeScale(fontSizeScale: Float) {
        viewModelScope.launch {
            updateViewState { it.copy(fontSizeScale = fontSizeScale, fontSizeScaleSelection = null) }

            val current = settingsManager.settings().first()
            runCatching {
                if (current.fontSizeScale != fontSizeScale) {
                    settingsManager.saveSettings(current.copy(fontSizeScale = fontSizeScale))
                }
            }.onFailure { e ->
                Log.e(tag, "Failed to save settings", e)
                updateViewState { it.copy(fontSizeScale = current.fontSizeScale, error = ViewState.Error.SettingsUpdatingError) }
            }
        }
    }

    fun saveKeepScreenOn(keepScreenOn: Boolean) {
        updateSettings { it.copy(keepScreenOn = keepScreenOn) }
    }

    private inline fun updateSettings(crossinline op: (current: Settings) -> Settings) {
        viewModelScope.launch {
            runCatching {
                val current = settingsManager.settings().first()
                op(current).takeIf { it != current }?.let { settingsManager.saveSettings(it) }
            }.onFailure { e ->
                Log.e(tag, "Failed to save settings", e)
                updateViewState { it.copy(error = ViewState.Error.SettingsUpdatingError) }
            }
        }
    }

    fun saveNightMode(@Settings.Companion.NightMode nightMode: Int) {
        viewModelScope.launch {
            runCatching {
                val current = settingsManager.settings().first()
                if (current.nightMode != nightMode) {
                    settingsManager.saveSettings(current.copy(nightMode = nightMode))
                }

                // This will restart the activity, so do it AFTER the settings are successfully saved.
                AppCompatDelegate.setDefaultNightMode(
                    when (nightMode) {
                        Settings.NIGHT_MODE_ON -> AppCompatDelegate.MODE_NIGHT_YES
                        Settings.NIGHT_MODE_OFF -> AppCompatDelegate.MODE_NIGHT_NO
                        Settings.NIGHT_MODE_FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        else -> throw IllegalArgumentException("Unsupported night mode - $nightMode")
                    }
                )
            }.onFailure { e ->
                Log.e(tag, "Failed to save settings", e)
                updateViewState { it.copy(error = ViewState.Error.SettingsUpdatingError) }
            }
        }
    }

    fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean) {
        updateSettings { it.copy(simpleReadingModeOn = simpleReadingModeOn) }
    }

    fun saveHideSearchButton(hideSearchButton: Boolean) {
        updateSettings { it.copy(hideSearchButton = hideSearchButton) }
    }

    fun saveConsolidateVersesForSharing(consolidateVersesForSharing: Boolean) {
        updateSettings { it.copy(consolidateVersesForSharing = consolidateVersesForSharing) }
    }

    fun saveDefaultHighlightColor(@Highlight.Companion.AvailableColor defaultHighlightColor: Int) {
        updateSettings { it.copy(defaultHighlightColor = defaultHighlightColor) }
    }

    fun selectFontSizeScale() {
        viewModelScope.launch {
            val currentSettings = settingsManager.settings().first()
            @Px val currentBodyTextSizePx = currentSettings.getPrimaryTextSize(application.resources).roundToInt()
            @Px val currentCaptionTextSizePx = currentSettings.getSecondaryTextSize(application.resources).roundToInt()
            val currentFontSizeScale = currentSettings.fontSizeScale
            updateViewState { currentViewState ->
                currentViewState.copy(
                    fontSizeScaleSelection = ViewState.FontSizeScaleSelection(
                        currentBodyTextSizePx = currentBodyTextSizePx,
                        currentCaptionTextSizePx = currentCaptionTextSizePx,
                        currentScale = currentFontSizeScale,
                        minScale = Settings.MIN_FONT_SIZE_SCALE,
                        maxScale = Settings.MAX_FONT_SIZE_SCALE,
                    )
                )
            }
        }
    }

    fun selectNightMode() {
        viewModelScope.launch {
            val currentNightMode = settingsManager.settings().first().nightMode
            val availableModes = listOf(
                ViewState.NightModeSelection.NightMode(Settings.NIGHT_MODE_ON, R.string.settings_text_night_mode_on),
                ViewState.NightModeSelection.NightMode(Settings.NIGHT_MODE_OFF, R.string.settings_text_night_mode_off),
                ViewState.NightModeSelection.NightMode(Settings.NIGHT_MODE_FOLLOW_SYSTEM, R.string.settings_text_night_mode_system),
            )
            val currentPosition = availableModes.indexOfFirst { it.nightMode == currentNightMode }
            updateViewState { it.copy(nightModeSelection = ViewState.NightModeSelection(currentPosition, availableModes)) }
        }
    }

    fun selectHighlightColor() {
        viewModelScope.launch {
            val currentDefaultHighlightColor = settingsManager.settings().first().defaultHighlightColor
            val availableColors = listOf(
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_NONE, R.string.text_highlight_color_none),
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_YELLOW, R.string.text_highlight_color_yellow),
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_PINK, R.string.text_highlight_color_pink),
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_ORANGE, R.string.text_highlight_color_orange),
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_PURPLE, R.string.text_highlight_color_purple),
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_RED, R.string.text_highlight_color_red),
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_GREEN, R.string.text_highlight_color_green),
                ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_BLUE, R.string.text_highlight_color_blue),
            )
            val currentPosition = availableColors.indexOfFirst { it.color == currentDefaultHighlightColor }
            updateViewState { it.copy(highlightColorSelection = ViewState.HighlightColorSelection(currentPosition, availableColors)) }
        }
    }

    fun backup() {
        emitViewAction(ViewAction.RequestUriForBackup)
    }

    fun restore() {
        emitViewAction(ViewAction.RequestUriForRestore)
    }

    fun backup(uri: Uri?) {
        if (uri == null) {
            updateViewState { it.copy(error = ViewState.Error.BackupError) }
            return
        }
        updateViewState { it.copy(backupState = ViewState.BackupRestoreState.Ongoing) }

        viewModelScope.launch(coroutineDispatcherProvider.io) {
            runCatching {
                application.contentResolver.openOutputStream(uri)
                    ?.use { backupManager.backup(it) }
                    ?: throw IOException("Failed to open Uri for backup - $uri")
                updateViewState { it.copy(backupState = ViewState.BackupRestoreState.Completed(successful = true)) }
            }.onFailure {
                Log.e(tag, "Failed to backup data", it)
                updateViewState { current ->
                    current.copy(
                        backupState = ViewState.BackupRestoreState.Completed(successful = false),
                        error = ViewState.Error.BackupError,
                    )
                }
            }
        }
    }

    fun restore(uri: Uri?) {
        if (uri == null) {
            updateViewState { it.copy(error = ViewState.Error.RestoreError) }
            return
        }
        updateViewState { it.copy(restoreState = ViewState.BackupRestoreState.Ongoing) }

        viewModelScope.launch(coroutineDispatcherProvider.io) {
            runCatching {
                application.contentResolver.openInputStream(uri)
                    ?.use { backupManager.restore(it) }
                    ?: throw IOException("Failed to open Uri for restore - $uri")
                updateViewState { it.copy(restoreState = ViewState.BackupRestoreState.Completed(successful = true)) }
            }.onFailure {
                Log.e(tag, "Failed to restore data", it)
                updateViewState { current ->
                    current.copy(
                        restoreState = ViewState.BackupRestoreState.Completed(successful = false),
                        error = ViewState.Error.RestoreError,
                    )
                }
            }
        }
    }

    fun openRateMe() {
        emitViewAction(ViewAction.OpenRateMe)
    }

    fun openWebsite() {
        emitViewAction(ViewAction.OpenWebsite)
    }

    fun markBackupStateAsIdle() {
        updateViewState { it.copy(backupState = ViewState.BackupRestoreState.Idle) }
    }

    fun markRestoreStateAsIdle() {
        updateViewState { it.copy(restoreState = ViewState.BackupRestoreState.Idle) }
    }

    fun markFontSizeSelectionAsDismissed() {
        updateViewState { it.copy(fontSizeScaleSelection = null) }
    }

    fun markNightModeSelectionAsDismissed() {
        updateViewState { it.copy(nightModeSelection = null) }
    }

    fun markHighlightColorSelectionAsDismissed() {
        updateViewState { it.copy(highlightColorSelection = null) }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}

@StringRes
private fun nightModeStringResource(@Settings.Companion.NightMode nightMode: Int): Int = when (nightMode) {
    Settings.NIGHT_MODE_ON -> R.string.settings_text_night_mode_on
    Settings.NIGHT_MODE_OFF -> R.string.settings_text_night_mode_off
    Settings.NIGHT_MODE_FOLLOW_SYSTEM -> R.string.settings_text_night_mode_system
    else -> throw IllegalArgumentException("Unknown night mode - $nightMode")
}

@StringRes
private fun highlightColorStringResource(@Highlight.Companion.AvailableColor highlightColor: Int): Int = when (highlightColor) {
    Highlight.COLOR_NONE -> R.string.text_highlight_color_none
    Highlight.COLOR_YELLOW -> R.string.text_highlight_color_yellow
    Highlight.COLOR_PINK -> R.string.text_highlight_color_pink
    Highlight.COLOR_ORANGE -> R.string.text_highlight_color_orange
    Highlight.COLOR_PURPLE -> R.string.text_highlight_color_purple
    Highlight.COLOR_RED -> R.string.text_highlight_color_red
    Highlight.COLOR_GREEN -> R.string.text_highlight_color_green
    Highlight.COLOR_BLUE -> R.string.text_highlight_color_blue
    else -> throw IllegalArgumentException("Unknown highlight color - $highlightColor")
}
