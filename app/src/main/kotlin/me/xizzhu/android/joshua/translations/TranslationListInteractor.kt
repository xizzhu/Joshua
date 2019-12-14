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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwareInteractor
import me.xizzhu.android.logger.Log

data class TranslationList(val currentTranslation: String,
                           val availableTranslations: List<TranslationInfo>,
                           val downloadedTranslations: List<TranslationInfo>)

class TranslationListInteractor(private val bibleReadingManager: BibleReadingManager,
                                private val translationManager: TranslationManager,
                                settingsManager: SettingsManager,
                                dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAwareInteractor(settingsManager, dispatcher) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val translationList: BroadcastChannel<ViewData<TranslationList>> = ConflatedBroadcastChannel()

    @UiThread
    override fun onStart() {
        super.onStart()

        combine(bibleReadingManager.observeCurrentTranslation(),
                translationManager.observeAvailableTranslations(),
                translationManager.observeDownloadedTranslations()
        ) { currentTranslation, availableTranslations, downloadedTranslations ->
            TranslationList(currentTranslation, availableTranslations, downloadedTranslations)
        }.onEach { translationList ->
            this@TranslationListInteractor.translationList.offer(ViewData.success(translationList))
        }.launchIn(coroutineScope)
    }

    fun translationList(): Flow<ViewData<TranslationList>> = translationList.asFlow()

    fun loadTranslationList(forceRefresh: Boolean) {
        coroutineScope.launch {
            try {
                translationList.offer(ViewData.loading())
                translationManager.reload(forceRefresh)
            } catch (e: Exception) {
                Log.e(tag, "Failed to load translation list", e)
                translationList.offer(ViewData.error(exception = e))
            }
        }
    }

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<ViewData<Int>> =
            translationManager.downloadTranslation(translationToDownload)
                    .map { progress ->
                        if (progress <= 100) {
                            ViewData.loading(progress)
                        } else {
                            // Ideally, we should use onCompletion() to handle this. However, it doesn't
                            // distinguish between a successful completion and a cancellation.
                            // See https://github.com/Kotlin/kotlinx.coroutines/issues/1693
                            if (bibleReadingManager.observeCurrentTranslation().first().isEmpty()) {
                                bibleReadingManager.saveCurrentTranslation(translationToDownload.shortName)
                            }

                            ViewData.success(-1)
                        }
                    }
                    .catch { cause ->
                        Log.e(tag, "Failed to download translation", cause)
                        emit(ViewData.error(exception = cause))
                    }

    suspend fun removeTranslation(translationToRemove: TranslationInfo) {
        translationManager.removeTranslation(translationToRemove)
    }
}
