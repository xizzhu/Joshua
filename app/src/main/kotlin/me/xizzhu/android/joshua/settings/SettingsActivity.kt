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

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.databinding.ActivitySettingsBinding
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.indeterminateProgressDialog
import me.xizzhu.android.joshua.ui.seekBarDialog
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.logger.Log
import kotlin.math.roundToInt

@AndroidEntryPoint
class SettingsActivity : BaseActivity<ActivitySettingsBinding, SettingsViewModel>() {
    companion object {
        private const val CODE_CREATE_FILE_FOR_BACKUP = 9999
        private const val CODE_SELECT_FILE_FOR_RESTORE = 9998
    }

    private val settingsViewModel: SettingsViewModel by viewModels()

    private var currentSettingsViewData: SettingsViewData? = null

    private var indeterminateProgressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSettings()
        initializeListeners()
    }

    private fun observeSettings() {
        settingsViewModel.settingsViewData()
                .onEach(
                        onLoading = { /* Do nothing. */ },
                        onSuccess = {
                            updateView(it)
                            currentSettingsViewData = it
                        },
                        onFailure = { /* Do nothing. */ }
                )
                .launchIn(lifecycleScope)
    }

    private fun updateView(settingsViewData: SettingsViewData) {
        window.decorView.keepScreenOn = settingsViewData.keepScreenOn

        with(viewBinding) {
            fontSize.setDescription("${settingsViewData.currentFontSizeScale}x")
            keepScreenOn.isChecked = settingsViewData.keepScreenOn
            nightMode.setDescription(settingsViewData.nightMode.label)
            simpleReadingMode.isChecked = settingsViewData.simpleReadingModeOn
            hideSearchButton.isChecked = settingsViewData.hideSearchButton
            consolidatedSharing.isChecked = settingsViewData.consolidateVersesForSharing
            defaultHighlightColor.setDescription(settingsViewData.defaultHighlightColor.label)
            version.setDescription(settingsViewData.version)
        }

        if (settingsViewData.animateFontSize) {
            animateTextSize(settingsViewData.bodyTextSizeInPixel, settingsViewData.captionTextSizeInPixel)
        } else {
            setTextSize(settingsViewData.bodyTextSizeInPixel, settingsViewData.captionTextSizeInPixel)
        }
    }

    private fun animateTextSize(bodyTextSizeInPixel: Float, captionTextSizeInPixel: Float) {
        val currentSettings = currentSettingsViewData
        if (currentSettings == null) {
            setTextSize(bodyTextSizeInPixel, captionTextSizeInPixel)
            return
        }

        ValueAnimator.ofFloat(0.0F, 1.0F).apply {
            val fromBodyTextSizeInPixel = currentSettings.bodyTextSizeInPixel
            val fromCaptionTextSizeInPixel = currentSettings.captionTextSizeInPixel
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                setTextSize(
                        bodyTextSizeInPixel = fromBodyTextSizeInPixel + fraction * (bodyTextSizeInPixel - fromBodyTextSizeInPixel),
                        captionTextSizeInPixel = fromCaptionTextSizeInPixel + fraction * (captionTextSizeInPixel - fromCaptionTextSizeInPixel)
                )
            }
        }.start()
    }

    private fun setTextSize(bodyTextSizeInPixel: Float, captionTextSizeInPixel: Float) {
        with(viewBinding) {
            display.setTextSize(bodyTextSizeInPixel.roundToInt())
            fontSize.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
            keepScreenOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizeInPixel)
            nightMode.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
            reading.setTextSize(bodyTextSizeInPixel.roundToInt())
            simpleReadingMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizeInPixel)
            hideSearchButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizeInPixel)
            consolidatedSharing.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizeInPixel)
            defaultHighlightColor.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
            backupRestore.setTextSize(bodyTextSizeInPixel.roundToInt())
            backup.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
            restore.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
            about.setTextSize(bodyTextSizeInPixel.roundToInt())
            rate.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
            website.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
            version.setTextSize(bodyTextSizeInPixel.roundToInt(), captionTextSizeInPixel.roundToInt())
        }
    }

    private fun initializeListeners(): Unit = with(viewBinding) {
        fontSize.setOnClickListener {
            currentSettingsViewData?.let { settings ->
                seekBarDialog(
                        title = R.string.settings_title_font_size,
                        initialValue = settings.currentFontSizeScale,
                        minValue = 0.5F,
                        maxValue = 3.0F,
                        onValueChanged = { value ->
                            fontSize.setDescription("${value}x")
                            setTextSize(
                                    bodyTextSizeInPixel = settings.bodyTextSizeInPixel * value / settings.currentFontSizeScale,
                                    captionTextSizeInPixel = settings.captionTextSizeInPixel * value / settings.currentFontSizeScale
                            )
                        },
                        onPositive = { value ->
                            // update current view data to avoid animating font changes after setting is saved
                            currentSettingsViewData = settings.copy(
                                    currentFontSizeScale = value,
                                    bodyTextSizeInPixel = settings.bodyTextSizeInPixel * value / settings.currentFontSizeScale,
                                    captionTextSizeInPixel = settings.captionTextSizeInPixel * value / settings.currentFontSizeScale
                            )
                            settingsViewModel.saveFontSizeScale(value)
                                    .onFailure {
                                        // reset the current view data
                                        currentSettingsViewData = settings
                                        animateTextSize(settings.bodyTextSizeInPixel, settings.captionTextSizeInPixel)

                                        toast(R.string.toast_unknown_error)
                                    }.launchIn(lifecycleScope)
                        },
                        onNegative = {
                            setTextSize(settings.bodyTextSizeInPixel, settings.captionTextSizeInPixel)
                        }
                )
            }
        }
        keepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveKeepScreenOn(isChecked).onFailure { toast(R.string.toast_unknown_error) }.launchIn(lifecycleScope)
        }
        nightMode.setOnClickListener {
            currentSettingsViewData?.let { settings ->
                dialog(R.string.settings_title_pick_night_mode, resources.getStringArray(R.array.text_night_modes), settings.nightMode.ordinal) { dialog, which ->
                    settingsViewModel.saveNightModeOn(SettingsViewData.NightMode.values()[which])
                            .onFailure { toast(R.string.toast_unknown_error) }
                            .launchIn(lifecycleScope)
                    dialog.dismiss()
                }
            }
        }
        simpleReadingMode.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveSimpleReadingModeOn(isChecked).onFailure { toast(R.string.toast_unknown_error) }.launchIn(lifecycleScope)
        }
        hideSearchButton.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveHideSearchButton(isChecked).onFailure { toast(R.string.toast_unknown_error) }.launchIn(lifecycleScope)
        }
        consolidatedSharing.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveConsolidateVersesForSharing(isChecked).onFailure { toast(R.string.toast_unknown_error) }.launchIn(lifecycleScope)
        }
        defaultHighlightColor.setOnClickListener {
            currentSettingsViewData?.let { settings ->
                dialog(R.string.text_pick_highlight_color, resources.getStringArray(R.array.text_colors), settings.defaultHighlightColor.ordinal) { dialog, which ->
                    settingsViewModel.saveDefaultHighlightColor(HighlightColorViewData.values()[which])
                            .onFailure { toast(R.string.toast_unknown_error) }
                            .launchIn(lifecycleScope)
                    dialog.dismiss()
                }
            }
        }
        backup.setOnClickListener {
            try {
                startActivityForResult(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),
                        CODE_CREATE_FILE_FOR_BACKUP
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to create file for backup", e)
                toast(R.string.toast_unknown_error)
            }
        }
        restore.setOnClickListener {
            startActivityForResult(
                    Intent.createChooser(
                            Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE), getString(R.string.text_restore_from)
                    ),
                    CODE_SELECT_FILE_FOR_RESTORE
            )
        }
        rate.setOnClickListener {
            try {
                navigator.navigate(this@SettingsActivity, Navigator.SCREEN_RATE_ME)
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to rate app", e)
                toast(R.string.toast_unknown_error)
            }
        }
        website.setOnClickListener {
            try {
                navigator.navigate(this@SettingsActivity, Navigator.SCREEN_WEBSITE)
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to visit website", e)
                toast(R.string.toast_unknown_error)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CODE_CREATE_FILE_FOR_BACKUP -> if (resultCode == Activity.RESULT_OK) backupRestore(settingsViewModel.backup(data?.data))
            CODE_SELECT_FILE_FOR_RESTORE -> if (resultCode == Activity.RESULT_OK) backupRestore(settingsViewModel.restore(data?.data))
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun backupRestore(op: Flow<BaseViewModel.ViewData<Int>>) {
        op.onEach(
                onLoading = {
                    dismissIndeterminateProgressDialog()
                    indeterminateProgressDialog = indeterminateProgressDialog(R.string.dialog_wait)
                },
                onSuccess = {
                    dismissIndeterminateProgressDialog()
                    toast(it)
                },
                onFailure = {
                    dismissIndeterminateProgressDialog()
                    toast(R.string.toast_unknown_error)
                }
        ).launchIn(lifecycleScope)
    }

    private fun dismissIndeterminateProgressDialog() {
        indeterminateProgressDialog?.dismiss()
        indeterminateProgressDialog = null
    }

    override fun inflateViewBinding(): ActivitySettingsBinding = ActivitySettingsBinding.inflate(layoutInflater)

    override fun viewModel(): SettingsViewModel = settingsViewModel
}
