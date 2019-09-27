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
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SwitchCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.settings.widgets.SettingButton
import me.xizzhu.android.joshua.settings.widgets.SettingSectionHeader
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.utils.activities.BaseActivity
import me.xizzhu.android.joshua.utils.MVPView
import me.xizzhu.android.joshua.utils.createChooserForSharing
import me.xizzhu.android.logger.Log
import javax.inject.Inject
import kotlin.math.roundToInt

interface SettingsView : MVPView {
    fun onRestoreStarted()

    fun onRestored()

    fun onRestoreFailed()

    fun onSettingsUpdated(settings: Settings)

    fun onSettingsUpdateFailed(settingsToUpdate: Settings)
}

class SettingsActivity : BaseActivity(), SettingsView {
    companion object {
        private const val CODE_PICK_CONTENT_FOR_RESTORE = 9999
        private const val CODE_CREATE_DOCUMENT_FOR_BACKUP = 9998
    }

    private val fontSizeTexts: Array<String> = arrayOf(".5x", "1x", "1.5x", "2x", "2.5x", "3x")

    @Inject
    lateinit var presenter: SettingsPresenter

    private val display: SettingSectionHeader by bindView(R.id.display)
    private val fontSize: SettingButton by bindView(R.id.font_size)
    private val keepScreenOn: SwitchCompat by bindView(R.id.keep_screen_on)
    private val nightModeOn: SwitchCompat by bindView(R.id.night_mode_on)
    private val reading: SettingSectionHeader by bindView(R.id.reading)
    private val simpleReadingMode: SwitchCompat by bindView(R.id.simple_reading_mode)
    private val backupRestore: SettingSectionHeader by bindView(R.id.backup_restore)
    private val backup: SettingButton by bindView(R.id.backup)
    private val restore: SettingButton by bindView(R.id.restore)
    private val about: SettingSectionHeader by bindView(R.id.about)
    private val rate: SettingButton by bindView(R.id.rate)
    private val version: SettingButton by bindView(R.id.version)

    private var shouldAnimateFontSize = false
    private var shouldAnimateColor = false
    private var originalSettings: Settings? = null

    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        fontSize.setOnClickListener {
            DialogHelper.showDialog(this@SettingsActivity, R.string.settings_title_font_size,
                    fontSizeTexts, presenter.settings.fontSizeScale - 1,
                    DialogInterface.OnClickListener { dialog, which ->
                        originalSettings = presenter.settings
                        shouldAnimateFontSize = true
                        presenter.setFontSizeScale(which + 1)

                        dialog.dismiss()
                    })
        }

        keepScreenOn.setOnCheckedChangeListener { _, isChecked -> presenter.setKeepScreenOn(isChecked) }

        nightModeOn.setOnCheckedChangeListener { _, isChecked ->
            originalSettings = presenter.settings
            shouldAnimateColor = true
            presenter.setNightModeOn(isChecked)
        }

        simpleReadingMode.setOnCheckedChangeListener { _, isChecked -> presenter.setSimpleReadingModeOn(isChecked) }

        backup.setOnClickListener {
            try {
                startActivityForResult(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),
                        CODE_CREATE_DOCUMENT_FOR_BACKUP
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to create document for backup", e)
                Toast.makeText(this@SettingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
            }
        }

        restore.setOnClickListener {
            try {
                startActivityForResult(
                        Intent.createChooser(
                                Intent(Intent.ACTION_GET_CONTENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),
                                getString(R.string.text_restore_from)
                        ),
                        CODE_PICK_CONTENT_FOR_RESTORE
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to get content for restore", e)
                Toast.makeText(this@SettingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
            }
        }

        rate.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("market://details?id=me.xizzhu.android.joshua")))
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to rate app", e)
                Toast.makeText(this@SettingsActivity, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
            }
        }

        try {
            version.setDescription(packageManager.getPackageInfo(packageName, 0).versionName)
        } catch (e: Exception) {
            Log.e(tag, "Failed to load app version", e)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CODE_PICK_CONTENT_FOR_RESTORE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data
                            ?.let { uri ->
                                showDialog()
                                coroutineScope.launch(Dispatchers.IO) {
                                    contentResolver.openInputStream(uri)?.use { inputStream ->
                                        presenter.restore(String(inputStream.readBytes(), Charsets.UTF_8))
                                    }
                                }
                            }
                            ?: Toast.makeText(this, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
                }
            }
            CODE_CREATE_DOCUMENT_FOR_BACKUP -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data
                            ?.let { uri ->
                                showDialog()
                                coroutineScope.launch(Dispatchers.IO) {
                                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                                        outputStream.write(presenter.prepareForBackup().toByteArray(Charsets.UTF_8))
                                    }
                                    coroutineScope.launch(Dispatchers.Main) {
                                        dismissDialog()
                                        Toast.makeText(this@SettingsActivity, R.string.text_backup_done, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            ?: Toast.makeText(this, R.string.toast_unknown_error, Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showDialog() {
        dismissDialog()
        dialog = ProgressDialog.showIndeterminateProgressDialog(this, R.string.dialog_wait)
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    override fun onRestoreStarted() {
        // do nothing
    }

    override fun onRestored() {
        dismissDialog()
        Toast.makeText(this, R.string.toast_restored, Toast.LENGTH_SHORT).show()
    }

    override fun onRestoreFailed() {
        dismissDialog()
        Toast.makeText(this, R.string.toast_unknown_error, Toast.LENGTH_SHORT).show()
    }

    override fun onSettingsUpdated(settings: Settings) {
        window.decorView.keepScreenOn = settings.keepScreenOn

        val resources = resources
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

        fontSize.setDescription(fontSizeTexts[settings.fontSizeScale - 1])
        keepScreenOn.isChecked = settings.keepScreenOn
        nightModeOn.isChecked = settings.nightModeOn
        simpleReadingMode.isChecked = settings.simpleReadingModeOn
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
        window.decorView.setBackgroundColor(backgroundColor)

        fontSize.setTextColor(primaryTextColor, secondaryTextColor)
        keepScreenOn.setTextColor(primaryTextColor)
        nightModeOn.setTextColor(primaryTextColor)
        simpleReadingMode.setTextColor(primaryTextColor)
        backup.setTextColor(primaryTextColor, secondaryTextColor)
        restore.setTextColor(primaryTextColor, secondaryTextColor)
        rate.setTextColor(primaryTextColor, secondaryTextColor)
        version.setTextColor(primaryTextColor, secondaryTextColor)
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
        display.setTextSize(bodyTextSize.roundToInt())
        fontSize.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
        keepScreenOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        nightModeOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        reading.setTextSize(bodyTextSize.roundToInt())
        simpleReadingMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        backupRestore.setTextSize(bodyTextSize.roundToInt())
        backup.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
        restore.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
        about.setTextSize(bodyTextSize.roundToInt())
        rate.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
        version.setTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
    }

    override fun onSettingsUpdateFailed(settingsToUpdate: Settings) {
        DialogHelper.showDialog(this, true, R.string.dialog_update_settings_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.saveSettings(settingsToUpdate)
                })
    }
}
