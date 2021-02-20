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

import android.content.Intent
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.logger.Log

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

class SettingsViewModel(
        settingsInteractor: SettingsInteractor, settingsActivity: SettingsActivity, coroutineScope: CoroutineScope = settingsActivity.lifecycleScope
) : BaseViewModel<SettingsInteractor, SettingsActivity>(settingsInteractor, settingsActivity, coroutineScope) {
    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val fontSizeTexts: Array<String> = arrayOf(".5x", "1x", "1.5x", "2x", "2.5x", "3x")
    }

    private val settingsViewData: MutableStateFlow<ViewData<SettingsViewData>?> = MutableStateFlow(null)

    private var shouldAnimateFontSize = false
    private var shouldAnimateColor = false

    init {
        val version = try {
            activity.applicationContext.packageManager.getPackageInfo(activity.packageName, 0).versionName
        } catch (e: Exception) {
            Log.e(tag, "Failed to load app version", e)
            ""
        }

        interactor.settings()
                .onEach { settings ->
                    val resources = activity.resources
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
                }
                .launchIn(coroutineScope)
    }

    fun settings(): Flow<ViewData<SettingsViewData>> = settingsViewData.filterNotNull()

    fun saveFontSizeScale(fontSizeScale: Int): Flow<ViewData<Unit>> = updateSettings {
        shouldAnimateFontSize = true
        interactor.saveFontSizeScale(fontSizeScale + 1)
    }

    private inline fun updateSettings(crossinline op: suspend () -> Unit): Flow<ViewData<Unit>> = flow {
        try {
            emit(ViewData.Loading())
            op()
            emit(ViewData.Success(Unit))
        } catch (e: Exception) {
            Log.e(tag, "Failed to save settings", e)
            emit(ViewData.Failure(e))
        }
    }

    fun saveKeepScreenOn(keepScreenOn: Boolean): Flow<ViewData<Unit>> = updateSettings { interactor.saveKeepScreenOn(keepScreenOn) }

    fun saveNightModeOn(nightModeOn: Boolean): Flow<ViewData<Unit>> = updateSettings {
        shouldAnimateColor = true
        interactor.saveNightModeOn(nightModeOn)
    }

    fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean): Flow<ViewData<Unit>> =
            updateSettings { interactor.saveSimpleReadingModeOn(simpleReadingModeOn) }

    fun saveHideSearchButton(hideSearchButton: Boolean): Flow<ViewData<Unit>> = updateSettings { interactor.saveHideSearchButton(hideSearchButton) }

    fun saveConsolidateVersesForSharing(consolidateVerses: Boolean): Flow<ViewData<Unit>> =
            updateSettings { interactor.saveConsolidateVersesForSharing(consolidateVerses) }

    fun saveDefaultHighlightColor(highlightColor: HighlightColorViewData): Flow<ViewData<Unit>> =
            updateSettings { interactor.saveDefaultHighlightColor(highlightColor.color) }

    fun rateMe() {
        try {
            // TODO Move to Navigator
            activity.startActivity(Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("market://details?id=me.xizzhu.android.joshua")))
        } catch (e: Exception) {
            Log.e(tag, "Failed to start activity to rate app", e)
            activity.toast(R.string.toast_unknown_error)
        }
    }

    fun openWebsite() {
        try {
            // TODO Move to Navigator
            activity.startActivity(Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("https://xizzhu.me/pages/about-joshua/")))
        } catch (e: Exception) {
            Log.e(tag, "Failed to start activity to visit website", e)
            activity.toast(R.string.toast_unknown_error)
        }
    }
}
