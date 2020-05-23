/*
 * Copyright (C) 2020 Xizhi Zhu
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
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log
import java.util.*

data class TranslationListViewHolder(
        val swipeRefreshLayout: SwipeRefreshLayout, val translationListView: CommonRecyclerView
) : ViewHolder

class TranslationListPresenter(
        translationsViewModel: TranslationsViewModel, translationsActivity: TranslationsActivity,
        coroutineScope: CoroutineScope = translationsActivity.lifecycleScope
) : BaseSettingsPresenter<TranslationListViewHolder, TranslationsViewModel, TranslationsActivity>(
        translationsViewModel, translationsActivity, coroutineScope
) {
    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME)

    private var downloadingJob: Job? = null
    private var downloadTranslationDialog: ProgressDialog? = null
    private var removingJob: Job? = null
    private var removeTranslationDialog: AlertDialog? = null

    @UiThread
    override fun onBind() {
        super.onBind()

        with(viewHolder.swipeRefreshLayout) {
            setColorSchemeResources(R.color.primary_dark, R.color.primary, R.color.dark_cyan, R.color.dark_lime)
            setOnRefreshListener { loadTranslationList(true) }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        observeSettings()
        loadTranslationList(false)
    }

    private fun observeSettings() {
        viewModel.settings().onEach { viewHolder.translationListView.setSettings(it) }.launchIn(coroutineScope)
    }

    private fun loadTranslationList(forceRefresh: Boolean) {
        viewModel.translationList(forceRefresh)
                .onStart {
                    with(viewHolder) {
                        swipeRefreshLayout.isRefreshing = true
                        translationListView.visibility = View.GONE
                    }
                }.onEach { translationList ->
                    with(viewHolder) {
                        swipeRefreshLayout.isRefreshing = false
                        translationListView.setItems(translationList.toItems())
                        translationListView.fadeIn()
                    }
                }.catch { e ->
                    viewHolder.swipeRefreshLayout.isRefreshing = false
                    activity.dialog(false, R.string.dialog_load_translation_list_error,
                            DialogInterface.OnClickListener { _, _ -> loadTranslationList(forceRefresh) },
                            DialogInterface.OnClickListener { _, _ -> activity.finish() })
                }.launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun TranslationList.toItems(): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        val availableTranslations = availableTranslations.sortedWith(translationComparator)
        val downloadedTranslations = downloadedTranslations.sortedWith(translationComparator)
        items.addAll(downloadedTranslations.toItems(currentTranslation))
        if (availableTranslations.isNotEmpty()) {
            items.add(TitleItem(activity.getString(R.string.header_available_translations), false))
            items.addAll(availableTranslations.toItems(currentTranslation))
        }
        return items
    }

    private fun List<TranslationInfo>.toItems(currentTranslation: String): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        var currentLanguage = ""
        for (translationInfo in this@toItems) {
            val language = translationInfo.language.split("_")[0]
            if (currentLanguage != language) {
                items.add(TitleItem(Locale(language).displayLanguage, true))
                currentLanguage = language
            }
            items.add(TranslationItem(
                    translationInfo,
                    translationInfo.downloaded && translationInfo.shortName == currentTranslation,
                    this@TranslationListPresenter::onTranslationClicked,
                    this@TranslationListPresenter::onTranslationLongClicked
            ))
        }
        return items
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onTranslationClicked(translationInfo: TranslationInfo) {
        if (translationInfo.downloaded) {
            updateCurrentTranslationAndFinishActivity(translationInfo.shortName)
        } else {
            confirmAndDownloadTranslation(translationInfo)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateCurrentTranslationAndFinishActivity(translationShortName: String) {
        coroutineScope.launch {
            try {
                viewModel.saveCurrentTranslation(translationShortName)
                activity.finish()
            } catch (e: Exception) {
                Log.e(tag, "Failed to select translation and close translation management activity", e)
                activity.dialog(true, R.string.dialog_update_translation_error,
                        DialogInterface.OnClickListener { _, _ -> updateCurrentTranslationAndFinishActivity(translationShortName) })
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun confirmAndDownloadTranslation(translationToDownload: TranslationInfo) {
        activity.dialog(true, activity.getString(R.string.dialog_download_translation_confirmation, translationToDownload.name),
                DialogInterface.OnClickListener { _, _ -> downloadTranslation(translationToDownload) })
    }

    private fun downloadTranslation(translationToDownload: TranslationInfo) {
        if (downloadingJob != null || downloadTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        downloadTranslationDialog = activity.progressDialog(
                R.string.dialog_downloading, 100) { downloadingJob?.cancel() }

        downloadingJob = viewModel.downloadTranslation(translationToDownload)
                .onEach { progress ->
                    when (progress) {
                        in 0 until 100 -> {
                            downloadTranslationDialog?.setProgress(progress)
                        }
                        else -> {
                            downloadTranslationDialog?.run {
                                setTitle(R.string.dialog_installing)
                                setIsIndeterminate(true)
                            }
                        }
                    }
                }.catch { e ->
                    Log.e(tag, "Failed to download translation", e)
                    activity.dialog(true, R.string.dialog_download_error,
                            DialogInterface.OnClickListener { _, _ -> downloadTranslation(translationToDownload) })
                }.onCompletion { e ->
                    downloadTranslationDialog?.dismiss()
                    downloadTranslationDialog = null
                    downloadingJob = null

                    if (e == null) {
                        activity.toast(R.string.toast_downloaded)
                        loadTranslationList(false)
                    }
                }.launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onTranslationLongClicked(translationInfo: TranslationInfo, isCurrentTranslation: Boolean) {
        if (translationInfo.downloaded) {
            if (!isCurrentTranslation) {
                confirmAndRemoveTranslation(translationInfo)
            }
        } else {
            confirmAndDownloadTranslation(translationInfo)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun confirmAndRemoveTranslation(translationToRemove: TranslationInfo) {
        activity.dialog(true, activity.getString(R.string.dialog_delete_translation_confirmation, translationToRemove.name),
                DialogInterface.OnClickListener { _, _ -> removeTranslation(translationToRemove) })
    }

    private fun removeTranslation(translationToRemove: TranslationInfo) {
        if (removeTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        removeTranslationDialog = activity.indeterminateProgressDialog(R.string.dialog_deleting)

        removingJob = viewModel.removeTranslation(translationToRemove)
                .onEach {
                    activity.toast(R.string.toast_deleted)
                    loadTranslationList(false)
                }.catch { e ->
                    Log.e(tag, "Failed to remove translation", e)
                    activity.dialog(true, R.string.dialog_delete_error,
                            DialogInterface.OnClickListener { _, _ -> removeTranslation(translationToRemove) })
                }.onCompletion {
                    removeTranslationDialog?.dismiss()
                    removeTranslationDialog = null
                    removingJob = null
                }.launchIn(coroutineScope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        downloadingJob?.cancel()
        removingJob?.cancel()
    }
}
