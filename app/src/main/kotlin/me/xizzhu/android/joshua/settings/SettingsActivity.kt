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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.result.contract.ActivityResultContracts
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
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val createFileForBackupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) backupRestore(settingsViewModel.backup(result.data?.data))
    }
    private val selectFileForRestoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) backupRestore(settingsViewModel.restore(result.data?.data))
    }

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
                        },
                        onFailure = { /* Do nothing. */ }
                )
                .launchIn(lifecycleScope)
    }

    private fun updateView(settingsViewData: SettingsViewData) {
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

        setTextSize(settingsViewData.bodyTextSizeInPixel, settingsViewData.captionTextSizeInPixel)
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
            val settings = settingsViewModel.currentSettingsViewData() ?: return@setOnClickListener
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
                        settingsViewModel.saveFontSizeScale(value)
                                .onFailure {
                                    // reset the current view data
                                    fontSize.setDescription("${settings.currentFontSizeScale}x")
                                    setTextSize(settings.bodyTextSizeInPixel, settings.captionTextSizeInPixel)

                                    toast(R.string.toast_unknown_error)
                                }.launchIn(lifecycleScope)
                    },
                    onNegative = {
                        fontSize.setDescription("${settings.currentFontSizeScale}x")
                        setTextSize(settings.bodyTextSizeInPixel, settings.captionTextSizeInPixel)
                    }
            )
        }
        keepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveKeepScreenOn(isChecked).onFailure { toast(R.string.toast_unknown_error) }.launchIn(lifecycleScope)
        }
        nightMode.setOnClickListener {
            val nightMode = settingsViewModel.currentSettingsViewData()?.nightMode ?: return@setOnClickListener
            dialog(R.string.settings_title_pick_night_mode, resources.getStringArray(R.array.text_night_modes), nightMode.ordinal) { dialog, which ->
                settingsViewModel.saveNightMode(SettingsViewData.NightMode.values()[which])
                        .onFailure { toast(R.string.toast_unknown_error) }
                        .launchIn(lifecycleScope)
                dialog.dismiss()
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
            val defaultHighlightColor = settingsViewModel.currentSettingsViewData()?.defaultHighlightColor ?: return@setOnClickListener
            dialog(R.string.text_pick_highlight_color, resources.getStringArray(R.array.text_colors), defaultHighlightColor.ordinal) { dialog, which ->
                settingsViewModel.saveDefaultHighlightColor(SettingsViewData.HighlightColor.values()[which])
                        .onFailure { toast(R.string.toast_unknown_error) }
                        .launchIn(lifecycleScope)
                dialog.dismiss()
            }
        }
        backup.setOnClickListener {
            try {
                createFileForBackupLauncher.launch(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE)
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to create file for backup", e)
                toast(R.string.toast_unknown_error)
            }
        }
        restore.setOnClickListener {
            selectFileForRestoreLauncher.launch(Intent.createChooser(
                    Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE), getString(R.string.text_restore_from)
            ))
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
