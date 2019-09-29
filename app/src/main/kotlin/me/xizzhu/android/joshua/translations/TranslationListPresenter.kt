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

package me.xizzhu.android.joshua.translations

import android.content.DialogInterface
import android.view.View
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log
import java.util.ArrayList

data class TranslationListViewHolder(val translationListView: TranslationListView) : ViewHolder

class TranslationListPresenter(private val translationManagementActivity: TranslationManagementActivity,
                               translationListInteractor: TranslationListInteractor,
                               dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<TranslationListViewHolder, TranslationListInteractor>(translationListInteractor, dispatcher) {
    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME)

    private var downloadTranslationDialog: ProgressDialog? = null
    private var removeTranslationDialog: ProgressDialog? = null

    @UiThread
    override fun onBind(viewHolder: TranslationListViewHolder) {
        super.onBind(viewHolder)

        observeSettings()
        observeTranslationList()
        observeTranslationDownload()
        interactor.loadTranslationList(false)
    }

    private fun observeSettings() {
        coroutineScope.launch {
            interactor.settings().collect {
                if (it.status == ViewData.STATUS_SUCCESS) {
                    viewHolder?.translationListView?.onSettingsUpdated(it.data)
                }
            }
        }
    }

    private fun observeTranslationList() {
        coroutineScope.launch {
            interactor.translationList()
                    .map { translationList ->
                        translationList.copy(data = TranslationList(
                                translationList.data.currentTranslation,
                                translationList.data.availableTranslations.sortedWith(translationComparator),
                                translationList.data.downloadedTranslations.sortedWith(translationComparator)
                        ))
                    }
                    .collect { translationList ->
                        when (translationList.status) {
                            ViewData.STATUS_SUCCESS -> {
                                val items: ArrayList<BaseItem> = ArrayList()
                                items.addAll(translationList.data.downloadedTranslations.toTranslationItems(
                                        translationList.data.currentTranslation,
                                        this@TranslationListPresenter::onTranslationClicked,
                                        this@TranslationListPresenter::onTranslationLongClicked))
                                if (translationList.data.availableTranslations.isNotEmpty()) {
                                    items.add(TitleItem(translationManagementActivity.getString(R.string.header_available_translations), false))
                                    items.addAll(translationList.data.availableTranslations.toTranslationItems(
                                            translationList.data.currentTranslation,
                                            this@TranslationListPresenter::onTranslationClicked,
                                            this@TranslationListPresenter::onTranslationLongClicked))
                                }

                                viewHolder?.translationListView?.setTranslations(items)
                                viewHolder?.translationListView?.fadeIn()
                            }
                            ViewData.STATUS_ERROR -> {
                                DialogHelper.showDialog(translationManagementActivity, false, R.string.dialog_load_translation_list_error,
                                        DialogInterface.OnClickListener { _, _ ->
                                            interactor.loadTranslationList(false)
                                        },
                                        DialogInterface.OnClickListener { _, _ ->
                                            translationManagementActivity.finish()
                                        }
                                )
                            }
                            ViewData.STATUS_LOADING -> {
                                viewHolder?.translationListView?.visibility = View.GONE
                            }
                        }
                    }
        }
    }

    @VisibleForTesting
    fun onTranslationClicked(translationInfo: TranslationInfo) {
        if (translationInfo.downloaded) {
            updateCurrentTranslationAndFinishActivity(translationInfo.shortName)
        } else {
            interactor.downloadTranslation(translationInfo)
        }
    }

    private fun updateCurrentTranslationAndFinishActivity(translationShortName: String) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentTranslation(translationShortName)
                translationManagementActivity.finish()
            } catch (e: Exception) {
                Log.e(tag, "Failed to select translation and close translation management activity", e)
                DialogHelper.showDialog(translationManagementActivity, true, R.string.dialog_update_translation_error,
                        DialogInterface.OnClickListener { _, _ ->
                            updateCurrentTranslationAndFinishActivity(translationShortName)
                        }
                )
            }
        }
    }

    @VisibleForTesting
    fun onTranslationLongClicked(translationInfo: TranslationInfo, isCurrentTranslation: Boolean) {
        if (translationInfo.downloaded) {
            if (!isCurrentTranslation) {
                DialogHelper.showDialog(translationManagementActivity, true,
                        R.string.dialog_delete_translation_confirmation,
                        DialogInterface.OnClickListener { _, _ -> removeTranslation(translationInfo) })
            }
        } else {
            interactor.downloadTranslation(translationInfo)
        }
    }

    private fun removeTranslation(translationToRemove: TranslationInfo) {
        coroutineScope.launch {
            try {
                removeTranslationDialog = ProgressDialog.showIndeterminateProgressDialog(
                        translationManagementActivity, R.string.dialog_deleting_translation)

                interactor.removeTranslation(translationToRemove)

                dismissRemoveTranslationDialog()
                Toast.makeText(translationManagementActivity, R.string.toast_translation_deleted, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(tag, "Failed to remove translation", e)
                dismissRemoveTranslationDialog()
                DialogHelper.showDialog(translationManagementActivity, true, R.string.dialog_delete_error,
                        DialogInterface.OnClickListener { _, _ -> removeTranslation(translationToRemove) })
            }
        }
    }

    private fun dismissRemoveTranslationDialog() {
        removeTranslationDialog?.dismiss()
        removeTranslationDialog = null
    }

    private fun observeTranslationDownload() {
        coroutineScope.launch {
            interactor.translationDownload().collect { translationDownload ->
                when (translationDownload.status) {
                    ViewData.STATUS_SUCCESS -> {
                        dismissDownloadTranslationDialog()
                        Toast.makeText(translationManagementActivity, R.string.toast_translation_downloaded, Toast.LENGTH_SHORT).show()
                    }
                    ViewData.STATUS_ERROR -> {
                        dismissDownloadTranslationDialog()
                        DialogHelper.showDialog(translationManagementActivity, true, R.string.dialog_download_error,
                                DialogInterface.OnClickListener { _, _ ->
                                    interactor.downloadTranslation(translationDownload.data.translationInfo)
                                }
                        )
                    }
                    ViewData.STATUS_LOADING -> {
                        if (downloadTranslationDialog == null) {
                            downloadTranslationDialog = ProgressDialog.showProgressDialog(
                                    translationManagementActivity, R.string.dialog_downloading_translation, 100)
                        }
                        downloadTranslationDialog?.let {
                            if (translationDownload.data.progress < 100) {
                                it.setProgress(translationDownload.data.progress)
                            } else {
                                it.setTitle(R.string.dialog_installing_translation)
                                it.setIsIndeterminate(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun dismissDownloadTranslationDialog() {
        downloadTranslationDialog?.dismiss()
        downloadTranslationDialog = null
    }
}
