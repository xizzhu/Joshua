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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.ui.SwipeRefresherState
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.ui.recyclerview.toTranslationItems
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter
import java.util.*
import kotlin.Comparator

class TranslationInfoComparator : Comparator<TranslationInfo> {
    override fun compare(t1: TranslationInfo, t2: TranslationInfo): Int {
        val userLanguage = userLanguage()
        val language1 = t1.language.split(("_"))[0]
        val language2 = t2.language.split(("_"))[0]
        val score1 = if (userLanguage == language1) 1 else 0
        val score2 = if (userLanguage == language2) 1 else 0
        var r = score2 - score1
        if (r == 0) {
            r = t1.language.compareTo(t2.language)
        }
        if (r == 0) {
            r = t1.name.compareTo(t2.name)
        }
        return r
    }

    @VisibleForTesting
    fun userLanguage(): String {
        val userLocale = Locale.getDefault()
        return userLocale.language.toLowerCase(userLocale).split(("_"))[0]
    }
}

class TranslationPresenter(private val translationInteractor: TranslationInteractor,
                           private val context: Context) : BaseSettingsPresenter<TranslationView>(translationInteractor) {
    private val translationComparator = TranslationInfoComparator()

    private var currentTranslation: String? = null
    private var availableTranslations: List<TranslationInfo>? = null
    private var downloadedTranslations: List<TranslationInfo>? = null

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            translationInteractor.observeCurrentTranslation().consumeEach {
                currentTranslation = it
                updateTranslations()
            }
        }
        launch(Dispatchers.Main) {
            translationInteractor.observeAvailableTranslations().consumeEach {
                availableTranslations = it.sortedWith(translationComparator)
                updateTranslations()
            }
        }
        launch(Dispatchers.Main) {
            translationInteractor.observeDownloadedTranslations().consumeEach {
                downloadedTranslations = it.sortedWith(translationComparator)
                updateTranslations()
            }
        }
        launch(Dispatchers.Main) {
            translationInteractor.observeTranslationsLoadingState().consumeEach { loadingState ->
                when (loadingState) {
                    SwipeRefresherState.IS_REFRESHING -> view?.onTranslationsLoadingStarted()
                    SwipeRefresherState.NOT_REFRESHING -> view?.onTranslationsLoadingCompleted()
                }
            }
        }
        launch(Dispatchers.Main) {
            translationInteractor.observeTranslationsLoadingRequest().consumeEach { loadTranslationList(true) }
        }

        loadTranslationList(false)
    }

    private fun updateTranslations() {
        if (currentTranslation == null || availableTranslations == null || downloadedTranslations == null) {
            return
        }

        val items: ArrayList<BaseItem> = ArrayList()
        items.addAll(downloadedTranslations!!.toTranslationItems(currentTranslation!!))
        if (availableTranslations!!.isNotEmpty()) {
            items.add(TitleItem(context.getString(R.string.header_available_translations)))
        }
        items.addAll(availableTranslations!!.toTranslationItems(currentTranslation!!))

        if (items.isEmpty()) {
            view?.onNoTranslationsAvailable()
        } else {
            view?.onTranslationsUpdated(items)
        }
    }

    fun loadTranslationList(forceRefresh: Boolean) {
        launch(Dispatchers.Main) {
            try {
                translationInteractor.reload(forceRefresh)
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load translation list")
                view?.onTranslationsLoadingFailed(forceRefresh)
            }
        }
    }

    fun downloadTranslation(translationToDelete: TranslationInfo, downloadProgressChannel: Channel<Int> = Channel()) {
        view?.onTranslationDownloadStarted()

        launch(Dispatchers.Main) {
            downloadProgressChannel.consumeEach { progress ->
                try {
                    view?.onTranslationDownloadProgressed(progress)
                } catch (e: Exception) {
                    Log.e(tag, e, "Error when download progress is updated")
                }
            }
        }

        launch(Dispatchers.Main) {
            try {
                translationInteractor.downloadTranslation(downloadProgressChannel, translationToDelete)
                if (translationInteractor.observeCurrentTranslation().first().isEmpty()) {
                    translationInteractor.saveCurrentTranslation(translationToDelete.shortName)
                }
                view?.onTranslationDownloaded()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to download translation")
                view?.onTranslationDownloadFailed(translationToDelete)
            }
        }
    }

    fun removeTranslation(translationToRemove: TranslationInfo) {
        view?.onTranslationDeleteStarted()

        launch(Dispatchers.Main) {
            try {
                translationInteractor.removeTranslation(translationToRemove)
                view?.onTranslationDeleted()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to remove translation")
                view?.onTranslationDeleteFailed(translationToRemove)
            }
        }
    }

    fun updateCurrentTranslation(translationShortName: String) {
        launch(Dispatchers.Main) {
            try {
                translationInteractor.saveCurrentTranslation(translationShortName)
                translationInteractor.finish()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to select translation and close translation management activity")
                view?.onCurrentTranslationUpdateFailed(translationShortName)
            }
        }
    }

    fun finish() {
        translationInteractor.finish()
    }
}
