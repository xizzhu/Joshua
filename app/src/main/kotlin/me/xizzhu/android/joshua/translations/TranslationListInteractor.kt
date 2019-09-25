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

import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwareInteractor
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.logger.Log

data class TranslationList(val currentTranslation: String,
                           val availableTranslations: List<TranslationInfo>,
                           val downloadedTranslations: List<TranslationInfo>) {
    companion object {
        val EMPTY = TranslationList("", emptyList(), emptyList())
    }
}

data class TranslationDownload(val translationInfo: TranslationInfo, val progress: Int)

class TranslationListInteractor(private val bibleReadingManager: BibleReadingManager,
                                private val translationManager: TranslationManager,
                                settingsManager: SettingsManager,
                                dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAwareInteractor(settingsManager, dispatcher) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val translationList: BroadcastChannel<ViewData<TranslationList>> = ConflatedBroadcastChannel()
    private val translationDownload: BroadcastChannel<ViewData<TranslationDownload>> = ConflatedBroadcastChannel()
    private val translationRemoval: BroadcastChannel<ViewData<TranslationInfo>> = ConflatedBroadcastChannel()

    private var currentTranslation: String? = null
    private var availableTranslations: List<TranslationInfo>? = null
    private var downloadedTranslations: List<TranslationInfo>? = null

    @UiThread
    override fun onStarted() {
        super.onStarted()

        coroutineScope.launch {
            bibleReadingManager.observeCurrentTranslation().collect {
                currentTranslation = it
                onTranslationsUpdated()
            }
        }
        coroutineScope.launch {
            translationManager.observeAvailableTranslations().collect {
                availableTranslations = it
                onTranslationsUpdated()
            }
        }
        coroutineScope.launch {
            translationManager.observeDownloadedTranslations().collect {
                downloadedTranslations = it
                onTranslationsUpdated()
            }
        }
    }

    private fun onTranslationsUpdated() {
        if (currentTranslation == null || availableTranslations == null || downloadedTranslations == null) {
            return
        }
        translationList.offer(ViewData.success(TranslationList(currentTranslation!!, availableTranslations!!, downloadedTranslations!!)))
    }

    fun translationList(): Flow<ViewData<TranslationList>> = translationList.asFlow()

    fun loadTranslationList(forceRefresh: Boolean) {
        coroutineScope.launch {
            try {
                translationList.offer(ViewData.loading(TranslationList.EMPTY))
                translationManager.reload(forceRefresh)
            } catch (e: Exception) {
                Log.e(tag, "Failed to load translation list", e)
                translationList.offer(ViewData.error(TranslationList.EMPTY, e))
            }
        }
    }

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun translationDownload(): Flow<ViewData<TranslationDownload>> = translationDownload.asFlow()

    fun downloadTranslation(translationToDownload: TranslationInfo) {
        downloadTranslation(translationToDownload, Channel())
    }

    @VisibleForTesting
    fun downloadTranslation(translationToDownload: TranslationInfo, downloadProgressChannel: Channel<Int>) {
        translationDownload.offer(ViewData.loading(TranslationDownload(translationToDownload, 0)))

        coroutineScope.launch {
            downloadProgressChannel.consumeAsFlow().collect { progress ->
                try {
                    translationDownload.offer(ViewData.loading(TranslationDownload(translationToDownload, progress)))
                } catch (e: Exception) {
                    Log.e(tag, "Error when download progress is updated", e)
                }
            }
        }

        coroutineScope.launch {
            try {
                translationManager.downloadTranslation(downloadProgressChannel, translationToDownload)
                if (bibleReadingManager.observeCurrentTranslation().first().isEmpty()) {
                    bibleReadingManager.saveCurrentTranslation(translationToDownload.shortName)
                }
                translationDownload.offer(ViewData.success(TranslationDownload(translationToDownload, 0)))
            } catch (e: Exception) {
                Log.e(tag, "Failed to download translation", e)
                translationDownload.offer(ViewData.error(TranslationDownload(translationToDownload, 0)))
            }
        }
    }

    fun translationRemoval(): Flow<ViewData<TranslationInfo>> = translationRemoval.asFlow()

    fun removeTranslation(translationToRemove: TranslationInfo) {
        coroutineScope.launch {
            try {
                translationRemoval.offer(ViewData.loading(translationToRemove))
                translationManager.removeTranslation(translationToRemove)
                translationRemoval.offer(ViewData.success(translationToRemove))
            } catch (e: Exception) {
                Log.e(tag, "Failed to remove translation", e)
                translationRemoval.offer(ViewData.error(translationToRemove, e))
            }
        }
    }
}
