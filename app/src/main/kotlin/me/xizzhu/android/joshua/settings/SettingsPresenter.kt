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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.settings.widgets.SettingButton
import me.xizzhu.android.joshua.settings.widgets.SettingSectionHeader
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.logger.Log
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

data class SettingsViewHolder(val display: SettingSectionHeader, val fontSize: SettingButton,
                              val keepScreenOn: SwitchCompat, val nightModeOn: SwitchCompat,
                              val reading: SettingSectionHeader, val simpleReadingMode: SwitchCompat,
                              val hideSearchButton: SwitchCompat, val consolidatedSharing: SwitchCompat,
                              val defaultHighlightColor: SettingButton, val backupRestore: SettingSectionHeader,
                              val backup: SettingButton, val restore: SettingButton, val about: SettingSectionHeader,
                              val rate: SettingButton, val website: SettingButton, val version: SettingButton) : ViewHolder

class SettingsPresenter(
        settingsViewModel: SettingsViewModel, settingsActivity: SettingsActivity,
        coroutineScope: CoroutineScope = settingsActivity.lifecycleScope
) : ViewPresenter<SettingsViewHolder, SettingsViewModel, SettingsActivity>(settingsViewModel, settingsActivity, coroutineScope) {
    companion object {
        const val CODE_CREATE_DOCUMENT_FOR_BACKUP = 9999
        const val CODE_GET_CONTENT_FOR_RESTORE = 9998

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val fontSizeTexts: Array<String> = arrayOf(".5x", "1x", "1.5x", "2x", "2.5x", "3x")
    }

    private val highlightColorTexts = activity.resources.getStringArray(R.array.text_colors)

    private var currentSettings: Settings? = null

    private var shouldAnimateFontSize = false
    private var shouldAnimateColor = false

    private var backupRestoreDialog: AlertDialog? = null

    @UiThread
    override fun onBind() {
        super.onBind()

        try {
            viewHolder.version.setDescription(activity.packageManager.getPackageInfo(activity.packageName, 0).versionName)
        } catch (e: Exception) {
            Log.e(tag, "Failed to load app version", e)
        }

        viewHolder.fontSize.setOnClickListener {
            activity.dialog(R.string.settings_title_font_size, fontSizeTexts, currentSettings!!.fontSizeScale - 1) { dialog, which ->
                saveFontSizeScale(which + 1)
                dialog.dismiss()
            }
        }

        viewHolder.keepScreenOn.setOnCheckedChangeListener { _, isChecked -> saveKeepScreenOn(isChecked) }

        viewHolder.nightModeOn.setOnCheckedChangeListener { _, isChecked -> saveNightModeOn(isChecked) }

        viewHolder.simpleReadingMode.setOnCheckedChangeListener { _, isChecked -> saveSimpleReadingModeOn(isChecked) }

        viewHolder.hideSearchButton.setOnCheckedChangeListener { _, isChecked -> saveHideSearchButton(isChecked) }

        viewHolder.consolidatedSharing.setOnCheckedChangeListener { _, isChecked -> saveConsolidateVersesForSharing(isChecked) }

        viewHolder.defaultHighlightColor.setOnClickListener {
            activity.dialog(R.string.text_pick_highlight_color, highlightColorTexts,
                    max(0, Highlight.AVAILABLE_COLORS.indexOf(currentSettings?.defaultHighlightColor ?: Highlight.COLOR_NONE))) { dialog, which ->
                saveDefaultHighlightColor(Highlight.AVAILABLE_COLORS[which])
                dialog.dismiss()
            }
        }

        viewHolder.backup.setOnClickListener {
            try {
                activity.startActivityForResult(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),
                        CODE_CREATE_DOCUMENT_FOR_BACKUP
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to create document for backup", e)
                activity.toast(R.string.toast_unknown_error)
            }
        }

        viewHolder.restore.setOnClickListener {
            try {
                activity.startActivityForResult(
                        Intent.createChooser(
                                Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),
                                activity.getString(R.string.text_restore_from)
                        ),
                        CODE_GET_CONTENT_FOR_RESTORE
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to get content for restore", e)
                activity.toast(R.string.toast_unknown_error)
            }
        }

        viewHolder.rate.setOnClickListener {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("market://details?id=me.xizzhu.android.joshua")))
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to rate app", e)
                activity.toast(R.string.toast_unknown_error)
            }
        }

        viewHolder.website.setOnClickListener {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://xizzhu.me/pages/about-joshua/")))
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to visit website", e)
                activity.toast(R.string.toast_unknown_error)
            }
        }

        viewModel.settings().onEach { updateSettings(it) }.launchIn(coroutineScope)
    }

    private fun saveFontSizeScale(fontSizeScale: Int) {
        coroutineScope.launch {
            try {
                shouldAnimateFontSize = true
                viewModel.saveFontSizeScale(fontSizeScale)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save font size scale", e)
                activity.dialog(true, R.string.dialog_update_settings_error,
                        { _, _ -> saveFontSizeScale(fontSizeScale) })
            }
        }
    }

    private fun saveKeepScreenOn(keepScreenOn: Boolean) {
        coroutineScope.launch {
            try {
                viewModel.saveKeepScreenOn(keepScreenOn)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save keep screen on", e)
                activity.dialog(true, R.string.dialog_update_settings_error,
                        { _, _ -> saveKeepScreenOn(keepScreenOn) })
            }
        }
    }

    private fun saveNightModeOn(nightModeOn: Boolean) {
        coroutineScope.launch {
            try {
                shouldAnimateColor = true
                viewModel.saveNightModeOn(nightModeOn)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save night mode on", e)
                activity.dialog(true, R.string.dialog_update_settings_error,
                        { _, _ -> saveNightModeOn(nightModeOn) })
            }
        }
    }

    private fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean) {
        coroutineScope.launch {
            try {
                viewModel.saveSimpleReadingModeOn(simpleReadingModeOn)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save simple reading mode on", e)
                activity.dialog(true, R.string.dialog_update_settings_error,
                        { _, _ -> saveSimpleReadingModeOn(simpleReadingModeOn) })
            }
        }
    }

    private fun saveHideSearchButton(hideSearchButton: Boolean) {
        coroutineScope.launch {
            try {
                viewModel.saveHideSearchButton(hideSearchButton)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save hiding search button", e)
                activity.dialog(true, R.string.dialog_update_settings_error,
                        { _, _ -> saveHideSearchButton(hideSearchButton) })
            }
        }
    }

    private fun saveConsolidateVersesForSharing(consolidateVerses: Boolean) {
        coroutineScope.launch {
            try {
                viewModel.saveConsolidateVersesForSharing(consolidateVerses)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save consolidating verses for sharing", e)
                activity.dialog(true, R.string.dialog_update_settings_error,
                        { _, _ -> saveConsolidateVersesForSharing(consolidateVerses) })
            }
        }
    }

    private fun saveDefaultHighlightColor(@Highlight.Companion.AvailableColor color: Int) {
        coroutineScope.launch {
            try {
                viewModel.saveDefaultHighlightColor(color)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save default highlight color", e)
                activity.dialog(true, R.string.dialog_update_settings_error,
                        { _, _ -> saveDefaultHighlightColor(color) })
            }
        }
    }

    fun onCreateDocumentForBackup(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        data?.data?.let { backup(it) } ?: activity.toast(R.string.toast_unknown_error)
    }

    private fun backup(uri: Uri) {
        coroutineScope.launch {
            try {
                showBackupRestoreDialog()
                activity.contentResolver.openOutputStream(uri)?.use { viewModel.backup(it) }
                        ?: throw IOException("Failed to open Uri for backup - $uri")
                dismissBackupRestoreDialog()
                activity.toast(R.string.toast_backed_up)
            } catch (e: Exception) {
                Log.e(tag, "Failed to backup data", e)
                dismissBackupRestoreDialog()
                activity.dialog(true, R.string.dialog_backup_error, { _, _ -> backup(uri) })
            }
        }
    }

    private fun showBackupRestoreDialog() {
        dismissBackupRestoreDialog()
        backupRestoreDialog = activity.indeterminateProgressDialog(R.string.dialog_wait)
    }

    private fun dismissBackupRestoreDialog() {
        backupRestoreDialog?.dismiss()
        backupRestoreDialog = null
    }

    fun onGetContentForRestore(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        data?.data?.let { restore(it) } ?: activity.toast(R.string.toast_unknown_error)
    }

    private fun restore(uri: Uri) {
        coroutineScope.launch {
            try {
                showBackupRestoreDialog()
                activity.contentResolver.openInputStream(uri)?.use { viewModel.restore(it) }
                        ?: throw IOException("Failed to open Uri for restore - $uri")
                dismissBackupRestoreDialog()
                activity.toast(R.string.toast_restored)
            } catch (e: Throwable) {
                when (e) {
                    is Exception, is OutOfMemoryError -> {
                        // Catching OutOfMemoryError here, because there're cases when users try to
                        // open a huge file.
                        // See https://console.firebase.google.com/u/0/project/joshua-production/crashlytics/app/android:me.xizzhu.android.joshua/issues/e9339c69d6e1856856db88413614d3d3
                        Log.e(tag, "Failed to backup data", e)
                        dismissBackupRestoreDialog()
                        activity.dialog(true, R.string.dialog_restore_error, { _, _ -> restore(uri) })
                    }
                    else -> throw e
                }
            }
        }
    }

    private fun updateSettings(settings: Settings) {
        activity.window.decorView.keepScreenOn = settings.keepScreenOn

        val resources = activity.resources
        if (shouldAnimateFontSize) {
            shouldAnimateFontSize = false
            animateTextSize(currentSettings!!.getBodyTextSize(resources), settings.getBodyTextSize(resources),
                    currentSettings!!.getCaptionTextSize(resources), settings.getCaptionTextSize(resources))
        } else {
            updateTextSize(settings.getBodyTextSize(resources), settings.getCaptionTextSize(resources))
        }

        if (shouldAnimateColor) {
            shouldAnimateColor = false
            animateColor(currentSettings!!.getBackgroundColor(), settings.getBackgroundColor(),
                    currentSettings!!.getPrimaryTextColor(resources), settings.getPrimaryTextColor(resources),
                    currentSettings!!.getSecondaryTextColor(resources), settings.getSecondaryTextColor(resources))
        } else {
            updateColor(settings.getBackgroundColor(), settings.getPrimaryTextColor(resources),
                    settings.getSecondaryTextColor(resources))
        }

        viewHolder.let {
            it.fontSize.setDescription(fontSizeTexts[settings.fontSizeScale - 1])
            it.keepScreenOn.isChecked = settings.keepScreenOn
            it.nightModeOn.isChecked = settings.nightModeOn
            it.simpleReadingMode.isChecked = settings.simpleReadingModeOn
            it.hideSearchButton.isChecked = settings.hideSearchButton
            it.consolidatedSharing.isChecked = settings.consolidateVersesForSharing
            it.defaultHighlightColor.setDescription(highlightColorTexts[Highlight.AVAILABLE_COLORS.indexOf(settings.defaultHighlightColor)])
        }

        currentSettings = settings
    }

    private fun animateColor(@ColorInt fromBackgroundColor: Int, @ColorInt toBackgroundColor: Int,
                             @ColorInt fromPrimaryTextColor: Int, @ColorInt toPrimaryTextColor: Int,
                             @ColorInt fromSecondaryTextColor: Int, @ColorInt toSecondaryTextColor: Int) {
        val argbEvaluator = ArgbEvaluator()
        ValueAnimator.ofFloat(0.0F, 1.0F).run {
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                val backgroundColor = argbEvaluator.evaluate(fraction, fromBackgroundColor, toBackgroundColor) as Int
                val primaryTextColor = argbEvaluator.evaluate(fraction, fromPrimaryTextColor, toPrimaryTextColor) as Int
                val secondaryTextColor = argbEvaluator.evaluate(fraction, fromSecondaryTextColor, toSecondaryTextColor) as Int
                updateColor(backgroundColor, primaryTextColor, secondaryTextColor)
            }
            start()
        }
    }

    private fun updateColor(@ColorInt backgroundColor: Int, @ColorInt primaryTextColor: Int,
                            @ColorInt secondaryTextColor: Int) {
        activity.window.decorView.setBackgroundColor(backgroundColor)

        viewHolder.run {
            fontSize.setTextColor(primaryTextColor, secondaryTextColor)
            keepScreenOn.setTextColor(primaryTextColor)
            nightModeOn.setTextColor(primaryTextColor)
            simpleReadingMode.setTextColor(primaryTextColor)
            hideSearchButton.setTextColor(primaryTextColor)
            consolidatedSharing.setTextColor(primaryTextColor)
            defaultHighlightColor.setTextColor(primaryTextColor, secondaryTextColor)
            backup.setTextColor(primaryTextColor, secondaryTextColor)
            restore.setTextColor(primaryTextColor, secondaryTextColor)
            rate.setTextColor(primaryTextColor, secondaryTextColor)
            website.setTextColor(primaryTextColor, secondaryTextColor)
            version.setTextColor(primaryTextColor, secondaryTextColor)
        }
    }

    private fun animateTextSize(fromBodyTextSize: Float, toBodyTextSize: Float,
                                fromCaptionTextSize: Float, toCaptionTextSize: Float) {
        ValueAnimator.ofFloat(0.0F, 1.0F).run {
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                val bodyTextSize = fromBodyTextSize + fraction * (toBodyTextSize - fromBodyTextSize)
                val captionTextSize = fromCaptionTextSize + fraction * (toCaptionTextSize - fromCaptionTextSize)
                updateTextSize(bodyTextSize, captionTextSize)
            }
            start()
        }
    }

    private fun updateTextSize(bodyTextSize: Float, captionTextSize: Float) {
        viewHolder.run {
            display.setTextSize(bodyTextSize.roundToInt())
            fontSize.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            keepScreenOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            nightModeOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            reading.setTextSize(bodyTextSize.roundToInt())
            simpleReadingMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            hideSearchButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            consolidatedSharing.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            defaultHighlightColor.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            backupRestore.setTextSize(bodyTextSize.roundToInt())
            backup.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            restore.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            about.setTextSize(bodyTextSize.roundToInt())
            rate.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            website.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            version.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
        }
    }
}
