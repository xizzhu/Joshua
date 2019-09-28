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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.settings.widgets.SettingButton
import me.xizzhu.android.joshua.settings.widgets.SettingSectionHeader
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.logger.Log
import java.io.IOException
import kotlin.math.roundToInt

data class SettingsViewHolder(val display: SettingSectionHeader, val fontSize: SettingButton,
                              val keepScreenOn: SwitchCompat, val nightModeOn: SwitchCompat,
                              val reading: SettingSectionHeader, val simpleReadingMode: SwitchCompat,
                              val backupRestore: SettingSectionHeader, val backup: SettingButton,
                              val restore: SettingButton, val about: SettingSectionHeader,
                              val rate: SettingButton, val version: SettingButton) : ViewHolder

class SettingsViewPresenter(private val settingsActivity: SettingsActivity, interactor: SettingsInteractor,
                            dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<SettingsViewHolder, SettingsInteractor>(interactor, dispatcher) {
    companion object {
        const val CODE_CREATE_DOCUMENT_FOR_BACKUP = 9999
        const val CODE_GET_CONTENT_FOR_RESTORE = 9998

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

        viewHolder.fontSize.setOnClickListener {
            coroutineScope.launch {
                DialogHelper.showDialog(settingsActivity, R.string.settings_title_font_size,
                        fontSizeTexts, interactor.readSettings().fontSizeScale - 1,
                        DialogInterface.OnClickListener { dialog, which ->
                            saveFontSizeScale(which + 1)
                            dialog.dismiss()
                        })
            }
        }

        viewHolder.keepScreenOn.setOnCheckedChangeListener { _, isChecked -> saveKeepScreenOn(isChecked) }

        viewHolder.nightModeOn.setOnCheckedChangeListener { _, isChecked -> saveNightModeOn(isChecked) }

        viewHolder.simpleReadingMode.setOnCheckedChangeListener { _, isChecked -> saveSimpleReadingModeOn(isChecked) }

        viewHolder.backup.setOnClickListener {
            try {
                settingsActivity.startActivityForResult(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),
                        CODE_CREATE_DOCUMENT_FOR_BACKUP
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to create document for backup", e)
                Toast.makeText(settingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
            }
        }

        viewHolder.restore.setOnClickListener {
            try {
                settingsActivity.startActivityForResult(
                        Intent.createChooser(
                                Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),
                                settingsActivity.getString(R.string.text_restore_from)
                        ),
                        CODE_GET_CONTENT_FOR_RESTORE
                )
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

        coroutineScope.launch { updateSettings(interactor.readSettings()) }
    }

    private fun saveFontSizeScale(fontSizeScale: Int) {
        coroutineScope.launch {
            try {
                originalSettings = interactor.readSettings()
                shouldAnimateFontSize = true

                updateSettings(interactor.saveFontSizeScale(fontSizeScale))
            } catch (e: Exception) {
                Log.e(tag, "Failed to save font size scale", e)
                DialogHelper.showDialog(settingsActivity, true, R.string.dialog_update_settings_error,
                        DialogInterface.OnClickListener { _, _ -> saveFontSizeScale(fontSizeScale) })
            }
        }
    }

    private fun saveKeepScreenOn(keepScreenOn: Boolean) {
        coroutineScope.launch {
            try {
                updateSettings(interactor.saveKeepScreenOn(keepScreenOn))
            } catch (e: Exception) {
                Log.e(tag, "Failed to save keep screen on", e)
                DialogHelper.showDialog(settingsActivity, true, R.string.dialog_update_settings_error,
                        DialogInterface.OnClickListener { _, _ -> saveKeepScreenOn(keepScreenOn) })
            }
        }
    }

    private fun saveNightModeOn(nightModeOn: Boolean) {
        coroutineScope.launch {
            try {
                originalSettings = interactor.readSettings()
                shouldAnimateColor = true

                updateSettings(interactor.saveNightModeOn(nightModeOn))
            } catch (e: Exception) {
                Log.e(tag, "Failed to save night mode on", e)
                DialogHelper.showDialog(settingsActivity, true, R.string.dialog_update_settings_error,
                        DialogInterface.OnClickListener { _, _ -> saveNightModeOn(nightModeOn) })
            }
        }
    }

    private fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean) {
        coroutineScope.launch {
            try {
                updateSettings(interactor.saveSimpleReadingModeOn(simpleReadingModeOn))
            } catch (e: Exception) {
                Log.e(tag, "Failed to save simple reading mode on", e)
                DialogHelper.showDialog(settingsActivity, true, R.string.dialog_update_settings_error,
                        DialogInterface.OnClickListener { _, _ -> saveSimpleReadingModeOn(simpleReadingModeOn) })
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

        originalSettings = null
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

    fun onCreateDocumentForBackup(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        data?.data?.let { backup(it) }
                ?: Toast.makeText(settingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
    }

    private fun backup(uri: Uri) {
        coroutineScope.launch {
            try {
                showBackupRestoreDialog()
                settingsActivity.contentResolver.openOutputStream(uri)?.use { interactor.backup(it) }
                        ?: throw IOException("Failed to open Uri for backup - $uri")
                dismissBackupRestoreDialog()
                Toast.makeText(settingsActivity, R.string.toast_backed_up, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(tag, "Failed to backup data", e)
                dismissBackupRestoreDialog()
                DialogHelper.showDialog(settingsActivity, true, R.string.dialog_backup_error,
                        DialogInterface.OnClickListener { _, _ -> restore(uri) })
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

    fun onGetContentForRestore(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        data?.data?.let { restore(it) }
                ?: Toast.makeText(settingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
    }

    private fun restore(uri: Uri) {
        coroutineScope.launch {
            try {
                showBackupRestoreDialog()
                settingsActivity.contentResolver.openInputStream(uri)?.use { interactor.restore(it) }
                        ?: throw IOException("Failed to open Uri for restore - $uri")
                dismissBackupRestoreDialog()
                Toast.makeText(settingsActivity, R.string.toast_restored, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(tag, "Failed to backup data", e)
                dismissBackupRestoreDialog()
                DialogHelper.showDialog(settingsActivity, true, R.string.dialog_restore_error,
                        DialogInterface.OnClickListener { _, _ -> restore(uri) })
            }
        }
    }
}
