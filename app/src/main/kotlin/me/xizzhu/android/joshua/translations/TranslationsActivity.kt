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

package me.xizzhu.android.joshua.translations

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.databinding.ActivityTranslationsBinding
import me.xizzhu.android.joshua.infra.*
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.indeterminateProgressDialog
import me.xizzhu.android.joshua.ui.progressDialog
import me.xizzhu.android.joshua.ui.toast

@AndroidEntryPoint
class TranslationsActivity : BaseActivityV2<ActivityTranslationsBinding, TranslationsViewModel>(), TranslationItem.Callback {
    private val translationsViewModel: TranslationsViewModel by viewModels()

    private var downloadTranslationDialog: ProgressDialog? = null
    private var removeTranslationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        translationsViewModel.viewAction().onEach(::onViewAction).launchIn(lifecycleScope)
        translationsViewModel.viewState().onEach(::onViewState).launchIn(lifecycleScope)
        initializeListeners()
        translationsViewModel.refreshTranslations(false)
    }

    private fun onViewAction(viewAction: TranslationsViewModel.ViewAction) = when (viewAction) {
        TranslationsViewModel.ViewAction.GoBack -> navigator.goBack(this)
        is TranslationsViewModel.ViewAction.ShowDownloadTranslationFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_download,
                    { _, _ -> translationsViewModel.downloadTranslation(viewAction.translationToDownload) })
        }
        TranslationsViewModel.ViewAction.ShowNoTranslationAvailableError -> {
            dialog(false, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_translation_list,
                    { _, _ -> translationsViewModel.refreshTranslations(true) }, { _, _ -> finish() })
        }
        is TranslationsViewModel.ViewAction.ShowRemoveTranslationFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_delete,
                    { _, _ -> translationsViewModel.removeTranslation(viewAction.translationToRemove) })
        }
        is TranslationsViewModel.ViewAction.ShowSelectTranslationFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_translation, { _, _ -> selectTranslation(viewAction.translationToSelect) })
        }
        TranslationsViewModel.ViewAction.ShowTranslationDownloaded -> toast(R.string.toast_downloaded)
        TranslationsViewModel.ViewAction.ShowTranslationRemoved -> toast(R.string.toast_deleted)
    }

    private fun onViewState(viewState: TranslationsViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { translationList.setSettings(it) }

        if (viewState.loading) {
            swipeRefresher.isRefreshing = true
            translationList.visibility = View.GONE
        } else {
            swipeRefresher.isRefreshing = false
            translationList.fadeIn()
        }

        translationList.setItems(viewState.translationItems)

        if (viewState.downloadingTranslation) {
            if (downloadTranslationDialog == null) {
                downloadTranslationDialog = progressDialog(R.string.dialog_title_downloading, 100) { translationsViewModel.cancelDownloadingTranslation() }
            }
            if (viewState.downloadingProgress in 0..99) {
                downloadTranslationDialog?.setProgress(viewState.downloadingProgress)
            } else {
                downloadTranslationDialog?.let { dialog ->
                    dialog.setTitle(R.string.dialog_title_installing)
                    dialog.setIsIndeterminate(true)
                }
            }
        } else {
            downloadTranslationDialog?.dismiss()
            downloadTranslationDialog = null
        }

        if (viewState.removingTranslation) {
            if (removeTranslationDialog == null) {
                removeTranslationDialog = indeterminateProgressDialog(R.string.dialog_title_deleting)
            }
        } else {
            removeTranslationDialog?.dismiss()
            removeTranslationDialog = null
        }
    }

    private fun initializeListeners() {
        with(viewBinding.swipeRefresher) {
            setColorSchemeResources(R.color.primary, R.color.secondary, R.color.dark_cyan, R.color.dark_lime)
            setOnRefreshListener { translationsViewModel.refreshTranslations(true) }
        }
    }

    override fun inflateViewBinding(): ActivityTranslationsBinding = ActivityTranslationsBinding.inflate(layoutInflater)

    override fun viewModel(): TranslationsViewModel = translationsViewModel

    override fun selectTranslation(translationToSelect: TranslationInfo) {
        translationsViewModel.selectTranslation(translationToSelect)
    }

    override fun downloadTranslation(translationToDownload: TranslationInfo) {
        dialog(true, translationToDownload.name, R.string.dialog_message_download_translation_confirmation,
                { _, _ -> translationsViewModel.downloadTranslation(translationToDownload) })
    }

    override fun removeTranslation(translationToRemove: TranslationInfo) {
        dialog(true, translationToRemove.name, R.string.dialog_message_delete_translation_confirmation,
                { _, _ -> translationsViewModel.removeTranslation(translationToRemove) })
    }
}
