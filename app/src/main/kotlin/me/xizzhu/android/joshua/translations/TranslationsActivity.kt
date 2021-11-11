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

package me.xizzhu.android.joshua.translations

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.databinding.ActivityTranslationManagementBinding
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.indeterminateProgressDialog
import me.xizzhu.android.joshua.ui.progressDialog
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.logger.Log

@AndroidEntryPoint
class TranslationsActivity : BaseActivity<ActivityTranslationManagementBinding, TranslationsViewModel>(), TranslationItem.Callback {
    private val translationsViewModel: TranslationsViewModel by viewModels()

    private var downloadTranslationJob: Job? = null
    private var downloadTranslationDialog: ProgressDialog? = null

    private var removeTranslationJob: Job? = null
    private var removeTranslationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSettings()
        observeTranslationList()
        initializeListeners()
    }

    private fun observeSettings() {
        translationsViewModel.settings().onEach { viewBinding.translationList.setSettings(it) }.launchIn(lifecycleScope)
    }

    private fun observeTranslationList() {
        translationsViewModel.translations()
                .onEach(
                        onLoading = {
                            with(viewBinding) {
                                swipeRefresher.isRefreshing = true
                                translationList.visibility = View.GONE
                            }
                        },
                        onSuccess = {
                            with(viewBinding) {
                                swipeRefresher.isRefreshing = false
                                translationList.setItems(it.items)
                                translationList.fadeIn()
                            }
                        },
                        onFailure = {
                            Log.e(tag, "Error while loading translation list", it)
                            viewBinding.swipeRefresher.isRefreshing = false
                            dialog(false, R.string.dialog_load_translation_list_error,
                                    { _, _ -> loadTranslationList() }, { _, _ -> finish() })
                        }
                )
                .launchIn(lifecycleScope)
    }

    private fun initializeListeners() {
        with(viewBinding.swipeRefresher) {
            setColorSchemeResources(R.color.primary_dark, R.color.primary, R.color.dark_cyan, R.color.dark_lime)
            setOnRefreshListener { loadTranslationList() }
        }
    }

    private fun loadTranslationList() {
        translationsViewModel.refreshTranslations(true)
    }

    override fun inflateViewBinding(): ActivityTranslationManagementBinding = ActivityTranslationManagementBinding.inflate(layoutInflater)

    override fun viewModel(): TranslationsViewModel = translationsViewModel

    override fun selectTranslation(translationToSelect: TranslationInfo) {
        translationsViewModel.selectTranslation(translationToSelect)
                .onSuccess { navigator.goBack(this) }
                .onFailure { dialog(true, R.string.dialog_update_translation_error, { _, _ -> selectTranslation(translationToSelect) }) }
                .launchIn(lifecycleScope)
    }

    override fun downloadTranslation(translationToDownload: TranslationInfo) {
        dialog(
                true, getString(R.string.dialog_download_translation_confirmation, translationToDownload.name),
                { _, _ -> doDownloadTranslation(translationToDownload) }
        )
    }

    private fun doDownloadTranslation(translationToDownload: TranslationInfo) {
        if (downloadTranslationJob != null || downloadTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        downloadTranslationDialog = progressDialog(R.string.dialog_downloading, 100) { downloadTranslationJob?.cancel() }

        downloadTranslationJob = lifecycleScope.launchWhenStarted {
            translationsViewModel.downloadTranslation(translationToDownload)
                    .onEach(
                            onLoading = { progress ->
                                when (progress) {
                                    in 0 until 99 -> {
                                        downloadTranslationDialog?.setProgress(progress!!)
                                    }
                                    else -> {
                                        downloadTranslationDialog?.run {
                                            setTitle(R.string.dialog_installing)
                                            setIsIndeterminate(true)
                                        }
                                    }
                                }
                            },
                            onSuccess = {
                                toast(R.string.toast_downloaded)
                            },
                            onFailure = {
                                dialog(true, R.string.dialog_download_error, { _, _ -> doDownloadTranslation(translationToDownload) })
                            }
                    )
                    .onCompletion {
                        downloadTranslationDialog?.dismiss()
                        downloadTranslationDialog = null
                        downloadTranslationJob = null
                    }
                    .collect()
        }
    }

    override fun removeTranslation(translationToRemove: TranslationInfo) {
        dialog(
                true, getString(R.string.dialog_delete_translation_confirmation, translationToRemove.name),
                { _, _ -> doRemoveTranslation(translationToRemove) }
        )
    }

    private fun doRemoveTranslation(translationToRemove: TranslationInfo) {
        if (removeTranslationJob != null || removeTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        removeTranslationDialog = indeterminateProgressDialog(R.string.dialog_deleting)

        removeTranslationJob = translationsViewModel.removeTranslation(translationToRemove)
                .onSuccess {
                    toast(R.string.toast_deleted)
                }
                .onFailure {
                    dialog(true, R.string.dialog_delete_error, { _, _ -> doRemoveTranslation(translationToRemove) })
                }
                .onCompletion {
                    removeTranslationDialog?.dismiss()
                    removeTranslationDialog = null
                    removeTranslationJob = null
                }
                .launchIn(lifecycleScope)
    }
}
