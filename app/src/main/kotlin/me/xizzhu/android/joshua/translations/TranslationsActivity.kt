/*
 * Copyright (C) 2023 Xizhi Zhu
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

import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.asExecutor
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.databinding.ActivityTranslationsBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.indeterminateProgressDialog
import me.xizzhu.android.joshua.ui.progressDialog
import me.xizzhu.android.joshua.ui.toast

@AndroidEntryPoint
class TranslationsActivity : BaseActivityV2<ActivityTranslationsBinding, TranslationsViewModel.ViewAction, TranslationsViewModel.ViewState, TranslationsViewModel>() {
    @Inject
    lateinit var coroutineDispatcherProvider: CoroutineDispatcherProvider

    private lateinit var adapter: TranslationsAdapter

    private var downloadTranslationDialog: ProgressDialog? = null
    private var removeTranslationDialog: AlertDialog? = null

    override val viewModel: TranslationsViewModel by viewModels()

    override val viewBinding: ActivityTranslationsBinding by lazy(LazyThreadSafetyMode.NONE) { ActivityTranslationsBinding.inflate(layoutInflater) }

    override fun initializeView() {
        adapter = TranslationsAdapter(
            inflater = layoutInflater,
            executor = coroutineDispatcherProvider.default.asExecutor()
        ) { viewEvent ->
            when (viewEvent) {
                is TranslationsAdapter.ViewEvent.DownloadTranslation -> {
                    dialog(
                        cancelable = true,
                        title = viewEvent.translationToDownload.name,
                        message = R.string.dialog_message_download_translation_confirmation,
                        onPositive = { _, _ -> viewModel.downloadTranslation(viewEvent.translationToDownload) },
                    )
                }
                is TranslationsAdapter.ViewEvent.RemoveTranslation -> {
                    dialog(
                        cancelable = true,
                        title = viewEvent.translationToRemove.name,
                        message = R.string.dialog_message_delete_translation_confirmation,
                        onPositive = { _, _ -> viewModel.removeTranslation(viewEvent.translationToRemove) },
                    )
                }
                is TranslationsAdapter.ViewEvent.SelectTranslation -> viewModel.selectTranslation(viewEvent.translationToSelect)
            }
        }
        viewBinding.translationList.adapter = adapter

        with(viewBinding.swipeRefresher) {
            setColorSchemeResources(R.color.primary, R.color.secondary, R.color.dark_cyan, R.color.dark_lime)
            setOnRefreshListener { viewModel.loadTranslations(forceRefresh = true) }
        }
    }

    override fun onViewActionEmitted(viewAction: TranslationsViewModel.ViewAction) = when (viewAction) {
        TranslationsViewModel.ViewAction.GoBack -> navigator.goBack(this)
    }

    override fun onViewStateUpdated(viewState: TranslationsViewModel.ViewState): Unit = with(viewBinding) {
        if (viewState.loading) {
            swipeRefresher.isRefreshing = true
            translationList.isVisible = false
        } else {
            swipeRefresher.isRefreshing = false
            translationList.fadeIn()
        }

        adapter.submitList(viewState.items)

        viewState.translationDownloadingState.handle()
        viewState.translationRemovalState.handle()
        viewState.error?.handle()
    }

    private fun TranslationsViewModel.ViewState.TranslationDownloadingState.handle() {
        when (this) {
            is TranslationsViewModel.ViewState.TranslationDownloadingState.Idle -> {
                downloadTranslationDialog?.dismiss()
                downloadTranslationDialog = null
            }
            is TranslationsViewModel.ViewState.TranslationDownloadingState.Downloading -> {
                if (downloadTranslationDialog == null) {
                    downloadTranslationDialog = progressDialog(R.string.dialog_title_downloading, 100) { viewModel.cancelDownloadingTranslation() }
                }
                downloadTranslationDialog?.setProgress(progress)
            }
            is TranslationsViewModel.ViewState.TranslationDownloadingState.Installing -> {
                downloadTranslationDialog?.let { dialog ->
                    dialog.setTitle(R.string.dialog_title_installing)
                    dialog.setIsIndeterminate(true)
                }
            }
            is TranslationsViewModel.ViewState.TranslationDownloadingState.Completed -> {
                downloadTranslationDialog?.dismiss()
                downloadTranslationDialog = null

                if (successful) {
                    toast(R.string.toast_downloaded)
                }

                viewModel.markTranslationDownloadingStateAsIdle()
            }
        }
    }

    private fun TranslationsViewModel.ViewState.TranslationRemovalState.handle() {
        when (this) {
            is TranslationsViewModel.ViewState.TranslationRemovalState.Idle -> {
                removeTranslationDialog?.dismiss()
                removeTranslationDialog = null
            }
            is TranslationsViewModel.ViewState.TranslationRemovalState.Removing -> {
                if (removeTranslationDialog == null) {
                    removeTranslationDialog = indeterminateProgressDialog(R.string.dialog_title_deleting)
                }
            }
            is TranslationsViewModel.ViewState.TranslationRemovalState.Completed -> {
                removeTranslationDialog?.dismiss()
                removeTranslationDialog = null

                if (successful) {
                    toast(R.string.toast_deleted)
                }

                viewModel.markTranslationRemovalStateAsIdle()
            }
        }
    }

    private fun TranslationsViewModel.ViewState.Error.handle() = when (this) {
        is TranslationsViewModel.ViewState.Error.NoTranslationsError -> {
            dialog(
                cancelable = false,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_load_translation_list,
                onPositive = { _, _ -> viewModel.loadTranslations(forceRefresh = true) },
                onNegative = { _, _ -> navigator.goBack(this@TranslationsActivity) },
                onDismiss = { viewModel.markErrorAsShown(this) },
            )
        }
        is TranslationsViewModel.ViewState.Error.TranslationAlreadyInstalledError,
        is TranslationsViewModel.ViewState.Error.TranslationNotInstalledError -> {
            toast(R.string.toast_unknown_error)
            viewModel.markErrorAsShown(this)
        }
        is TranslationsViewModel.ViewState.Error.TranslationDownloadingError -> {
            dialog(
                cancelable = true,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_download,
                onPositive = { _, _ -> viewModel.downloadTranslation(translationToDownload) },
                onDismiss = { viewModel.markErrorAsShown(this) },
            )
        }
        is TranslationsViewModel.ViewState.Error.TranslationRemovalError -> {
            dialog(
                cancelable = true,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_delete,
                onPositive = { _, _ -> viewModel.removeTranslation(translationToRemove) },
                onDismiss = { viewModel.markErrorAsShown(this) },
            )
        }
        is TranslationsViewModel.ViewState.Error.TranslationSelectionError -> {
            dialog(
                cancelable = true,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_select_translation,
                onPositive = { _, _ -> viewModel.selectTranslation(translationToSelect) },
                onDismiss = { viewModel.markErrorAsShown(this) },
            )
        }
    }
}
