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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.logger.Log
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

class TranslationPresenter(private val translationInteractor: TranslationInteractor) : BaseSettingsPresenter<TranslationView>(translationInteractor) {
    private val translationComparator = TranslationInfoComparator()

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            val currentTranslation = translationInteractor.observeCurrentTranslation()
            receiveChannels.add(currentTranslation)
            currentTranslation.consumeEach { view?.onCurrentTranslationUpdated(it) }
        }
        launch(Dispatchers.Main) {
            val availableTranslations = translationInteractor.observeAvailableTranslations()
            receiveChannels.add(availableTranslations)
            availableTranslations.consumeEach { view?.onAvailableTranslationsUpdated(it.sortedWith(translationComparator)) }
        }
        launch(Dispatchers.Main) {
            val downloadedTranslations = translationInteractor.observeDownloadedTranslations()
            receiveChannels.add(downloadedTranslations)
            downloadedTranslations.consumeEach { view?.onDownloadedTranslationsUpdated(it.sortedWith(translationComparator)) }
        }
        launch(Dispatchers.Main) {
            translationInteractor.reload(false)
        }
        launch(Dispatchers.Main) {
            val translationLoadingState = translationInteractor.observeTranslationsLoadingState()
            receiveChannels.add(translationLoadingState)
            translationLoadingState.consumeEach { loading ->
                if (loading) {
                    view?.onTranslationsLoadingStarted()
                } else {
                    view?.onTranslationsLoadingCompleted()
                }
            }
        }
    }

    fun downloadTranslation(translationInfo: TranslationInfo) {
        downloadTranslation(translationInfo, Channel())
    }

    @VisibleForTesting
    fun downloadTranslation(translationToDelete: TranslationInfo, downloadProgressChannel: Channel<Int>) {
        view?.onTranslationDownloadStarted()

        launch(Dispatchers.Main) {
            try {
                launch(Dispatchers.Main) {
                    translationInteractor.downloadTranslation(downloadProgressChannel, translationToDelete)
                }
                downloadProgressChannel.consumeEach { progress ->
                    view?.onTranslationDownloadProgressed(progress)
                }

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
}
