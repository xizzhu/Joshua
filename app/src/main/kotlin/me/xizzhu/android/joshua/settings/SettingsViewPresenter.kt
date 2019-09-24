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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.UiThread
import androidx.appcompat.widget.SwitchCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.settings.widgets.SettingButton
import me.xizzhu.android.joshua.settings.widgets.SettingSectionHeader
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.utils.createChooserForSharing
import me.xizzhu.android.logger.Log
import kotlin.math.roundToInt

data class SettingsViewHolder(val display: SettingSectionHeader, val fontSize: SettingButton,
                              val keepScreenOn: SwitchCompat, val nightModeOn: SwitchCompat,
                              val reading: SettingSectionHeader, val simpleReadingMode: SwitchCompat,
                              val backupRestore: SettingSectionHeader, val backup: SettingButton,
                              val restore: SettingButton, val about: SettingSectionHeader,
                              val rate: SettingButton, val version: SettingButton) : ViewHolder

class SettingsViewPresenter(private val settingsActivity: SettingsActivity, interactor: SettingsInteractor)
    : ViewPresenter<SettingsViewHolder, SettingsInteractor>(interactor) {
    companion object {
        const val CODE_GET_CONTENT_FOR_RESTORE = 9999

        private val fontSizeTexts: Array<String> = arrayOf(".5x", "1x", "1.5x", "2x", "2.5x", "3x")
    }

    private var shouldAnimateFontSize = false
    private var shouldAnimateColor = false
    private var originalSettings: Settings? = null

    private var backupRestoreDialog: ProgressDialog? = null

    @UiThread
    override fun onBind(viewHolder: SettingsViewHolder) {
        super.onBind(viewHolder)

        try {
            viewHolder.version.setDescription(settingsActivity.packageManager.getPackageInfo(settingsActivity.packageName, 0).versionName)
        } catch (e: Exception) {
            Log.e(tag, "Failed to load app version", e)
        }

        setupListeners(viewHolder)
        observeSettings()
        observeBackupRestore()
    }

    private fun setupListeners(viewHolder: SettingsViewHolder) {
        viewHolder.fontSize.setOnClickListener {
            DialogHelper.showDialog(settingsActivity, R.string.settings_title_font_size,
                    fontSizeTexts, interactor.getSettings().fontSizeScale - 1,
                    DialogInterface.OnClickListener { dialog, which ->
                        originalSettings = interactor.getSettings()
                        shouldAnimateFontSize = true
                        interactor.setFontSizeScale(which + 1)

                        dialog.dismiss()
                    })
        }

        viewHolder.keepScreenOn.setOnCheckedChangeListener { _, isChecked -> interactor.setKeepScreenOn(isChecked) }

        viewHolder.nightModeOn.setOnCheckedChangeListener { _, isChecked ->
            originalSettings = interactor.getSettings()
            shouldAnimateColor = true
            interactor.setNightModeOn(isChecked)
        }

        viewHolder.simpleReadingMode.setOnCheckedChangeListener { _, isChecked -> interactor.setSimpleReadingModeOn(isChecked) }

        viewHolder.backup.setOnClickListener {
            showBackupRestoreDialog()
            interactor.prepareBackupData()
        }

        viewHolder.restore.setOnClickListener {
            try {
                val chooserIntent = Intent.createChooser(
                        Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),
                        settingsActivity.getString(R.string.text_restore_from))
                settingsActivity.startActivityForResult(chooserIntent, CODE_GET_CONTENT_FOR_RESTORE)
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to get content for restore", e)
                Toast.makeText(settingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
            }
        }

        viewHolder.rate.setOnClickListener {
            try {
                settingsActivity.startActivity(Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("market://details?id=me.xizzhu.android.joshua")))
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to rate app", e)
                Toast.makeText(settingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showBackupRestoreDialog() {
        dismissBackupRestoreDialog()
        backupRestoreDialog = ProgressDialog.showIndeterminateProgressDialog(settingsActivity, R.string.dialog_wait)
    }

    private fun dismissBackupRestoreDialog() {
        backupRestoreDialog?.dismiss()
        backupRestoreDialog = null
    }

    private fun observeSettings() {
        coroutineScope.launch(Dispatchers.Main) {
            interactor.settings().collect { settings ->
                when (settings.status) {
                    ViewData.STATUS_SUCCESS -> updateSettings(settings.data)
                    else -> Log.e(tag, "", IllegalStateException("Unexpected status (${settings.status}) when observing settings()"))
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            interactor.settingsSaved().collect { settings ->
                when (settings.status) {
                    ViewData.STATUS_SUCCESS -> {
                        // when setting is saved, the settings updated observer will be triggered,
                        // so no need to update here
                    }
                    ViewData.STATUS_ERROR -> {
                        DialogHelper.showDialog(settingsActivity, true, R.string.dialog_update_settings_error,
                                DialogInterface.OnClickListener { _, _ -> interactor.saveSettings(settings.data) })
                    }
                    else -> Log.e(tag, "", IllegalStateException("Unexpected status (${settings.status}) when observing settingsSaved()"))
                }
            }
        }
    }

    private fun updateSettings(settings: Settings) {
        settingsActivity.window.decorView.keepScreenOn = settings.keepScreenOn

        val resources = settingsActivity.resources
        if (shouldAnimateFontSize) {
            shouldAnimateFontSize = false

            animateTextSize(originalSettings!!.getBodyTextSize(resources), settings.getBodyTextSize(resources),
                    originalSettings!!.getCaptionTextSize(resources), settings.getCaptionTextSize(resources))
        } else {
            updateTextSize(settings.getBodyTextSize(resources), settings.getCaptionTextSize(resources))
        }

        if (shouldAnimateColor) {
            shouldAnimateColor = false

            animateColor(originalSettings!!.getBackgroundColor(), settings.getBackgroundColor(),
                    originalSettings!!.getPrimaryTextColor(resources), settings.getPrimaryTextColor(resources),
                    originalSettings!!.getSecondaryTextColor(resources), settings.getSecondaryTextColor(resources))
        } else {
            updateColor(settings.getBackgroundColor(), settings.getPrimaryTextColor(resources),
                    settings.getSecondaryTextColor(resources))
        }

        viewHolder?.let {
            it.fontSize.setDescription(fontSizeTexts[settings.fontSizeScale - 1])
            it.keepScreenOn.isChecked = settings.keepScreenOn
            it.nightModeOn.isChecked = settings.nightModeOn
            it.simpleReadingMode.isChecked = settings.simpleReadingModeOn
        }
    }

    private fun animateColor(@ColorInt fromBackgroundColor: Int, @ColorInt toBackgroundColor: Int,
                             @ColorInt fromPrimaryTextColor: Int, @ColorInt toPrimaryTextColor: Int,
                             @ColorInt fromSecondaryTextColor: Int, @ColorInt toSecondaryTextColor: Int) {
        val argbEvaluator = ArgbEvaluator()
        val colorAnimator = ValueAnimator.ofFloat(0.0F, 1.0F)
        colorAnimator.addUpdateListener { animator ->
            val fraction = animator.animatedValue as Float
            val backgroundColor = argbEvaluator.evaluate(fraction, fromBackgroundColor, toBackgroundColor) as Int
            val primaryTextColor = argbEvaluator.evaluate(fraction, fromPrimaryTextColor, toPrimaryTextColor) as Int
            val secondaryTextColor = argbEvaluator.evaluate(fraction, fromSecondaryTextColor, toSecondaryTextColor) as Int
            updateColor(backgroundColor, primaryTextColor, secondaryTextColor)
        }
        colorAnimator.start()
    }

    private fun updateColor(@ColorInt backgroundColor: Int, @ColorInt primaryTextColor: Int,
                            @ColorInt secondaryTextColor: Int) {
        settingsActivity.window.decorView.setBackgroundColor(backgroundColor)

        viewHolder?.let {
            it.fontSize.setTextColor(primaryTextColor, secondaryTextColor)
            it.keepScreenOn.setTextColor(primaryTextColor)
            it.nightModeOn.setTextColor(primaryTextColor)
            it.simpleReadingMode.setTextColor(primaryTextColor)
            it.backup.setTextColor(primaryTextColor, secondaryTextColor)
            it.restore.setTextColor(primaryTextColor, secondaryTextColor)
            it.rate.setTextColor(primaryTextColor, secondaryTextColor)
            it.version.setTextColor(primaryTextColor, secondaryTextColor)
        }
    }

    private fun animateTextSize(fromBodyTextSize: Float, toBodyTextSize: Float,
                                fromCaptionTextSize: Float, toCaptionTextSize: Float) {
        val textSizeAnimator = ValueAnimator.ofFloat(0.0F, 1.0F)
        textSizeAnimator.addUpdateListener { animator ->
            val fraction = animator.animatedValue as Float
            val bodyTextSize = fromBodyTextSize + fraction * (toBodyTextSize - fromBodyTextSize)
            val captionTextSize = fromCaptionTextSize + fraction * (toCaptionTextSize - fromCaptionTextSize)
            updateTextSize(bodyTextSize, captionTextSize)
        }
        textSizeAnimator.start()
    }

    private fun updateTextSize(bodyTextSize: Float, captionTextSize: Float) {
        viewHolder?.let {
            it.display.setTextSize(bodyTextSize.roundToInt())
            it.fontSize.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            it.keepScreenOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            it.nightModeOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            it.reading.setTextSize(bodyTextSize.roundToInt())
            it.simpleReadingMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
            it.backupRestore.setTextSize(bodyTextSize.roundToInt())
            it.backup.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            it.restore.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            it.about.setTextSize(bodyTextSize.roundToInt())
            it.rate.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
            it.version.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
        }
    }

    private fun observeBackupRestore() {
        coroutineScope.launch(Dispatchers.Main) {
            interactor.backupPrepared().collect { backup ->
                when (backup.status) {
                    ViewData.STATUS_SUCCESS -> {
                        dismissBackupRestoreDialog()
                        createChooserForSharing(settingsActivity, settingsActivity.getString(R.string.text_backup_with), backup.data)
                                ?.let { settingsActivity.startActivity(it) }
                                ?: Toast.makeText(settingsActivity, R.string.toast_unknown_error, Toast.LENGTH_SHORT).show()
                    }
                    ViewData.STATUS_ERROR -> {
                        dismissBackupRestoreDialog()
                        DialogHelper.showDialog(settingsActivity, true, R.string.dialog_backup_error,
                                DialogInterface.OnClickListener { _, _ -> interactor.prepareBackupData() })
                    }
                    else -> Log.e(tag, "", IllegalStateException("Unexpected status (${backup.status}) when observing backupPrepared()"))
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            interactor.restored().collect { restore ->
                when (restore.status) {
                    ViewData.STATUS_SUCCESS -> {
                        dismissBackupRestoreDialog()
                        Toast.makeText(settingsActivity, R.string.toast_restored, Toast.LENGTH_SHORT).show()
                    }
                    ViewData.STATUS_ERROR -> {
                        dismissBackupRestoreDialog()
                        DialogHelper.showDialog(settingsActivity, true, R.string.dialog_restore_error,
                                DialogInterface.OnClickListener { _, _ -> interactor.restore(restore.data) })
                    }
                    else -> Log.e(tag, "", IllegalStateException("Unexpected status (${restore.status}) when observing restored()"))
                }
            }
        }
    }

    fun onGetContentForRestore(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        data?.data
                ?.let {
                    showBackupRestoreDialog()
                    coroutineScope.launch(Dispatchers.IO) {
                        settingsActivity.contentResolver.openInputStream(it)?.use {
                            interactor.restore(String(it.readBytes(), Charsets.UTF_8))
                        }
                    }
                }
                ?: Toast.makeText(settingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
    }
}
