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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.indeterminateProgressDialog
import me.xizzhu.android.joshua.ui.lifecycleScope
import me.xizzhu.android.joshua.ui.progressDialog
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText

class TranslationItem(val translationInfo: TranslationInfo, val isCurrentTranslation: Boolean,
                      val selectTranslation: (TranslationInfo) -> Flow<BaseViewModel.ViewData<Unit>>,
                      val downloadTranslation: (TranslationInfo) -> Flow<BaseViewModel.ViewData<Int>>,
                      val removeTranslation: (TranslationInfo) -> Flow<BaseViewModel.ViewData<Unit>>,
                      val rightDrawable: Int = if (isCurrentTranslation) R.drawable.ic_check else 0)
    : BaseItem(R.layout.item_translation, { inflater, parent -> TranslationItemViewHolder(inflater, parent) })

private class TranslationItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TranslationItem>(inflater.inflate(R.layout.item_translation, parent, false)) {
    private val textView = itemView as TextView

    private var downloadTranslationJob: Job? = null
    private var downloadTranslationDialog: ProgressDialog? = null

    private var removeTranslationJob: Job? = null
    private var removeTranslationDialog: AlertDialog? = null

    init {
        itemView.setOnClickListener {
            item?.let { item ->
                if (item.translationInfo.downloaded) {
                    selectTranslation()
                } else {
                    confirmAndDownloadTranslation()
                }
            }
        }
        itemView.setOnLongClickListener {
            item?.let { item ->
                if (item.translationInfo.downloaded) {
                    if (!item.isCurrentTranslation) {
                        confirmAndRemoveTranslation()
                    }
                } else {
                    confirmAndDownloadTranslation()
                }
            }
            return@setOnLongClickListener true
        }
    }

    private fun selectTranslation() {
        item?.let { item ->
            item.selectTranslation(item.translationInfo)
                    .onFailure { itemView.activity().dialog(true, R.string.dialog_update_translation_error, { _, _ -> selectTranslation() }) }
                    .launchIn(itemView.lifecycleScope())
        }
    }

    private fun confirmAndDownloadTranslation() {
        val translationName = item?.translationInfo?.name ?: return
        itemView.activity().dialog(
                true, itemView.activity().getString(R.string.dialog_download_translation_confirmation, translationName),
                { _, _ -> downloadTranslation() }
        )
    }

    private fun downloadTranslation() {
        val item = item ?: return
        if (downloadTranslationJob != null || downloadTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        downloadTranslationDialog = itemView.activity().progressDialog(R.string.dialog_downloading, 100) { downloadTranslationJob?.cancel() }

        downloadTranslationJob = itemView.lifecycleScope().launchWhenStarted {
            item.downloadTranslation(item.translationInfo)
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
                                itemView.activity().toast(R.string.toast_downloaded)
                            },
                            onFailure = {
                                itemView.activity().dialog(true, R.string.dialog_download_error, { _, _ -> downloadTranslation() })
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

    private fun confirmAndRemoveTranslation() {
        val translationName = item?.translationInfo?.name ?: return
        itemView.activity().dialog(
                true, itemView.activity().getString(R.string.dialog_delete_translation_confirmation, translationName),
                { _, _ -> removeTranslation() }
        )
    }

    private fun removeTranslation() {
        val item = item ?: return
        if (removeTranslationJob != null || removeTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        removeTranslationDialog = itemView.activity().indeterminateProgressDialog(R.string.dialog_deleting)

        removeTranslationJob = item.removeTranslation(item.translationInfo)
                .onSuccess {
                    itemView.activity().toast(R.string.toast_deleted)
                }
                .onFailure {
                    itemView.activity().dialog(true, R.string.dialog_delete_error, { _, _ -> removeTranslation() })
                }
                .onCompletion {
                    removeTranslationDialog?.dismiss()
                    removeTranslationDialog = null
                    removeTranslationJob = null
                }
                .launchIn(itemView.lifecycleScope())
    }

    override fun bind(settings: Settings, item: TranslationItem, payloads: List<Any>) {
        with(textView) {
            updateSettingsWithPrimaryText(settings)
            text = item.translationInfo.name
            setCompoundDrawablesWithIntrinsicBounds(0, 0, item.rightDrawable, 0)
        }
    }
}
