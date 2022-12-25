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

import android.content.Intent
import android.util.TypedValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.Px
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.databinding.ActivitySettingsBinding
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.ui.indeterminateProgressDialog
import me.xizzhu.android.joshua.ui.listDialog
import me.xizzhu.android.joshua.ui.seekBarDialog
import me.xizzhu.android.joshua.ui.setOnCheckedChangeByUserListener
import me.xizzhu.android.joshua.ui.setOnSingleClickListener
import me.xizzhu.android.logger.Log

@AndroidEntryPoint
class SettingsActivity : BaseActivityV2<ActivitySettingsBinding, SettingsViewModel.ViewAction, SettingsViewModel.ViewState, SettingsViewModel>() {
    private val createFileForBackupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) viewModel.backup(result.data?.data)
    }
    private val selectFileForRestoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) viewModel.restore(result.data?.data)
    }

    private var indeterminateProgressDialog: AlertDialog? = null

    override val viewModel: SettingsViewModel by viewModels()

    override val viewBinding: ActivitySettingsBinding by lazy(LazyThreadSafetyMode.NONE) { ActivitySettingsBinding.inflate(layoutInflater) }

    override fun initializeView() = with(viewBinding) {
        keepScreenOn.setOnCheckedChangeByUserListener(viewModel::saveKeepScreenOn)
        simpleReadingMode.setOnCheckedChangeByUserListener(viewModel::saveSimpleReadingModeOn)
        hideSearchButton.setOnCheckedChangeByUserListener(viewModel::saveHideSearchButton)
        consolidatedSharing.setOnCheckedChangeByUserListener(viewModel::saveConsolidateVersesForSharing)
        fontSize.setOnSingleClickListener(viewModel::selectFontSizeScale)
        nightMode.setOnSingleClickListener(viewModel::selectNightMode)
        defaultHighlightColor.setOnSingleClickListener(viewModel::selectHighlightColor)
        backup.setOnSingleClickListener(viewModel::backup)
        restore.setOnSingleClickListener(viewModel::restore)
        rate.setOnSingleClickListener(viewModel::openRateMe)
        website.setOnSingleClickListener(viewModel::openWebsite)
    }

    override fun onViewActionEmitted(viewAction: SettingsViewModel.ViewAction) = when (viewAction) {
        SettingsViewModel.ViewAction.OpenRateMe -> navigator.navigate(this, Navigator.SCREEN_RATE_ME)
        SettingsViewModel.ViewAction.OpenWebsite -> navigator.navigate(this, Navigator.SCREEN_WEBSITE)
        SettingsViewModel.ViewAction.RequestUriForBackup -> {
            try {
                createFileForBackupLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE))
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to create file for backup", e)
                toast(R.string.toast_unknown_error)
            }
        }
        SettingsViewModel.ViewAction.RequestUriForRestore -> {
            try {
                selectFileForRestoreLauncher.launch(Intent.createChooser(
                    Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE), getString(R.string.text_restore_from)
                ))
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity to choose file for restore", e)
                toast(R.string.toast_unknown_error)
            }
        }
    }

    override fun onViewStateUpdated(viewState: SettingsViewModel.ViewState): Unit = with(viewBinding) {
        fontSize.setDescription("${viewState.fontSizeScale}x")
        setTextSize(bodyTextSizePx = viewState.bodyTextSizePx, captionTextSizePx = viewState.captionTextSizePx)

        keepScreenOn.isChecked = viewState.keepScreenOn
        simpleReadingMode.isChecked = viewState.simpleReadingModeOn
        hideSearchButton.isChecked = viewState.hideSearchButton
        consolidatedSharing.isChecked = viewState.consolidateVersesForSharing

        nightMode.setDescription(viewState.nightModeStringRes)
        defaultHighlightColor.setDescription(viewState.defaultHighlightColorStringRes)
        version.setDescription(viewState.version)

        viewState.backupState.handle(isBackup = true)
        viewState.restoreState.handle(isBackup = false)
        viewState.fontSizeScaleSelection?.handle()
        viewState.nightModeSelection?.handle()
        viewState.highlightColorSelection?.handle()
        viewState.error?.handle()
    }

    private fun setTextSize(@Px bodyTextSizePx: Int, @Px captionTextSizePx: Int) = with(viewBinding) {
        display.setTextSize(bodyTextSizePx)
        fontSize.setTextSize(bodyTextSizePx, captionTextSizePx)
        keepScreenOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizePx.toFloat())
        nightMode.setTextSize(bodyTextSizePx, captionTextSizePx)
        reading.setTextSize(bodyTextSizePx)
        simpleReadingMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizePx.toFloat())
        hideSearchButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizePx.toFloat())
        consolidatedSharing.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizePx.toFloat())
        defaultHighlightColor.setTextSize(bodyTextSizePx, captionTextSizePx)
        backupRestore.setTextSize(bodyTextSizePx)
        backup.setTextSize(bodyTextSizePx, captionTextSizePx)
        restore.setTextSize(bodyTextSizePx, captionTextSizePx)
        about.setTextSize(bodyTextSizePx)
        rate.setTextSize(bodyTextSizePx, captionTextSizePx)
        website.setTextSize(bodyTextSizePx, captionTextSizePx)
        version.setTextSize(bodyTextSizePx, captionTextSizePx)
    }

    private fun SettingsViewModel.ViewState.BackupRestoreState.handle(isBackup: Boolean) {
        indeterminateProgressDialog?.dismiss()
        indeterminateProgressDialog = null

        when (this) {
            is SettingsViewModel.ViewState.BackupRestoreState.Idle -> {
                // Do nothing
            }
            is SettingsViewModel.ViewState.BackupRestoreState.Ongoing -> {
                indeterminateProgressDialog = indeterminateProgressDialog(R.string.dialog_title_wait)
            }
            is SettingsViewModel.ViewState.BackupRestoreState.Completed -> {
                if (successful) {
                    toast(R.string.toast_backup_restore_succeeded)
                }
                if (isBackup) {
                    viewModel.markBackupStateAsIdle()
                } else {
                    viewModel.markRestoreStateAsIdle()
                }
            }
        }
    }

    private fun SettingsViewModel.ViewState.FontSizeScaleSelection.handle() {
        seekBarDialog(
            title = R.string.settings_title_font_size,
            initialValue = currentScale,
            minValue = minScale,
            maxValue = maxScale,
            onValueChanged = { value ->
                viewBinding.fontSize.setDescription("${value}x")
                setTextSize(
                    bodyTextSizePx = (currentBodyTextSizePx * value / currentScale).roundToInt(),
                    captionTextSizePx = (currentCaptionTextSizePx * value / currentScale).roundToInt(),
                )
            },
            onPositive = viewModel::saveFontSizeScale,
            onNegative = {
                // resets to current value
                viewBinding.fontSize.setDescription("${currentScale}x")
                setTextSize(bodyTextSizePx = currentBodyTextSizePx, captionTextSizePx = currentCaptionTextSizePx)
            },
            onDismiss = viewModel::markFontSizeSelectionAsDismissed
        )
    }

    private fun SettingsViewModel.ViewState.NightModeSelection.handle() {
        listDialog(
            title = R.string.settings_title_pick_night_mode,
            items = availableModes.map { getString(it.stringRes) }.toTypedArray(),
            selected = currentPosition,
            onClicked = { dialog, which ->
                viewModel.markNightModeSelectionAsDismissed()
                viewModel.saveNightMode(availableModes[which].nightMode)
                dialog.dismiss()
            },
            onDismiss = { viewModel.markNightModeSelectionAsDismissed() }
        )
    }

    private fun SettingsViewModel.ViewState.HighlightColorSelection.handle() {
        listDialog(
            title = R.string.text_pick_highlight_color,
            items = availableColors.map { getString(it.stringRes) }.toTypedArray(),
            selected = currentPosition,
            onClicked = { dialog, which ->
                viewModel.markHighlightColorSelectionAsDismissed()
                viewModel.saveDefaultHighlightColor(availableColors[which].color)
                dialog.dismiss()
            },
            onDismiss = { viewModel.markHighlightColorSelectionAsDismissed() }
        )
    }

    private fun SettingsViewModel.ViewState.Error.handle() = when (this) {
        SettingsViewModel.ViewState.Error.BackupError,
        SettingsViewModel.ViewState.Error.RestoreError,
        SettingsViewModel.ViewState.Error.SettingsUpdatingError -> {
            toast(R.string.toast_unknown_error)
            viewModel.markErrorAsShown(this)
        }
    }
}
