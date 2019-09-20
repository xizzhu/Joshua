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

import android.content.Context
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.activities.BaseSettingsPresenter
import me.xizzhu.android.logger.Log
import java.util.*

class TranslationPresenter(private val translationInteractor: TranslationInteractor,
                           private val context: Context) : BaseSettingsPresenter<TranslationView>(translationInteractor) {
    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME)

    private var currentTranslation: String? = null
    private var availableTranslations: List<TranslationInfo>? = null
    private var downloadedTranslations: List<TranslationInfo>? = null

    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            translationInteractor.observeCurrentTranslation().collect {
                currentTranslation = it
                updateTranslations()
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            translationInteractor.observeAvailableTranslations().collect {
                availableTranslations = it.sortedWith(translationComparator)
                updateTranslations()
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            translationInteractor.observeDownloadedTranslations().collect {
                downloadedTranslations = it.sortedWith(translationComparator)
                updateTranslations()
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            translationInteractor.observeRefreshRequest().collect { loadTranslationList(true) }
        }

        loadTranslationList(false)
    }

    private fun updateTranslations() {
        if (currentTranslation == null || availableTranslations == null || downloadedTranslations == null) {
            return
        }

        val items: ArrayList<BaseItem> = ArrayList()
        items.addAll(downloadedTranslations!!.toTranslationItems(currentTranslation!!,
                this::onTranslationClicked, this::onTranslationLongClicked))
        if (availableTranslations!!.isNotEmpty()) {
            items.add(TitleItem(context.getString(R.string.header_available_translations), false))
            items.addAll(availableTranslations!!.toTranslationItems(currentTranslation!!,
                    this::onTranslationClicked, this::onTranslationLongClicked))
        }

        view?.onTranslationsUpdated(items)
    }

    fun loadTranslationList(forceRefresh: Boolean) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                translationInteractor.notifyLoadingStarted()
                view?.onTranslationsLoadingStarted()

                translationInteractor.reload(forceRefresh)

                view?.onTranslationsLoadingCompleted()
            } catch (e: Exception) {
                Log.e(tag, "Failed to load translation list", e)
                view?.onTranslationsLoadingFailed(forceRefresh)
            } finally {
                translationInteractor.notifyLoadingFinished()
            }
        }
    }

    @VisibleForTesting
    fun onTranslationClicked(translationInfo: TranslationInfo) {
        if (translationInfo.downloaded) {
            updateCurrentTranslation(translationInfo.shortName)
        } else {
            downloadTranslation(translationInfo)
        }
    }

    @VisibleForTesting
    fun onTranslationLongClicked(translationInfo: TranslationInfo, isCurrentTranslation: Boolean) {
        if (translationInfo.downloaded) {
            if (!isCurrentTranslation) {
                view?.onTranslationDeleteRequested(translationInfo)
            }
        } else {
            downloadTranslation(translationInfo)
        }
    }

    fun downloadTranslation(translationToDownload: TranslationInfo) {
        downloadTranslation(translationToDownload, Channel())
    }

    @VisibleForTesting
    fun downloadTranslation(translationToDownload: TranslationInfo, downloadProgressChannel: Channel<Int>) {
        view?.onTranslationDownloadStarted()

        coroutineScope.launch(Dispatchers.Main) {
            downloadProgressChannel.consumeAsFlow().collect { progress ->
                try {
                    view?.onTranslationDownloadProgressed(progress)
                } catch (e: Exception) {
                    Log.e(tag, "Error when download progress is updated", e)
                }
            }
        }

        coroutineScope.launch(Dispatchers.Main) {
            try {
                translationInteractor.downloadTranslation(downloadProgressChannel, translationToDownload)
                if (translationInteractor.observeCurrentTranslation().first().isEmpty()) {
                    translationInteractor.saveCurrentTranslation(translationToDownload.shortName)
                }
                view?.onTranslationDownloaded()
            } catch (e: Exception) {
                Log.e(tag, "Failed to download translation", e)
                view?.onTranslationDownloadFailed(translationToDownload)
            }
        }
    }

    fun removeTranslation(translationToRemove: TranslationInfo) {
        view?.onTranslationDeleteStarted()

        coroutineScope.launch(Dispatchers.Main) {
            try {
                translationInteractor.removeTranslation(translationToRemove)
                view?.onTranslationDeleted()
            } catch (e: Exception) {
                Log.e(tag, "Failed to remove translation", e)
                view?.onTranslationDeleteFailed(translationToRemove)
            }
        }
    }

    fun updateCurrentTranslation(translationShortName: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                translationInteractor.saveCurrentTranslation(translationShortName)
                finish()
            } catch (e: Exception) {
                Log.e(tag, "Failed to select translation and close translation management activity", e)
                view?.onCurrentTranslationUpdateFailed(translationShortName)
            }
        }
    }

    fun finish() {
        translationInteractor.finish()
    }
}
