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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.tests.MockContents

object TranslationManager {
    private val availableTranslations = MutableStateFlow<List<TranslationInfo>?>(null)
    private val downloadedTranslations = MutableStateFlow<List<TranslationInfo>?>(null)
    var throwErrorWhenLoadingTranslationList = false
    var throwErrorWhenDownloadingTranslation = false

    init {
        reset()
    }

    fun reset() {
        availableTranslations.value = listOf(MockContents.kjvTranslationInfo, MockContents.cuvTranslationInfo)
        downloadedTranslations.value = emptyList()
        throwErrorWhenLoadingTranslationList = false
        throwErrorWhenDownloadingTranslation = false
    }

    fun availableTranslations(): Flow<List<TranslationInfo>> =
            if (throwErrorWhenLoadingTranslationList) flow { throw RuntimeException() } else availableTranslations.filterNotNull()

    fun downloadedTranslations(): Flow<List<TranslationInfo>> = downloadedTranslations.filterNotNull()

    suspend fun reload(forceRefresh: Boolean) {
    }

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<Int> = flow {
        emit(1)
        delay(1000L)
        if (throwErrorWhenDownloadingTranslation) throw RuntimeException()
        emit(99)

        availableTranslations.value = availableTranslations.value!!.toMutableList().apply {
            removeIf { it.shortName == translationToDownload.shortName }
        }
        downloadedTranslations.value = downloadedTranslations.value!!.toMutableList().apply {
            add(translationToDownload.copy(downloaded = true))
        }

        if (downloadedTranslations.value!!.size == 1) {
            BibleReadingManager.saveCurrentTranslation(translationToDownload.shortName)
        }

        emit(100)
    }

    suspend fun removeTranslation(translationToRemove: TranslationInfo) {
        availableTranslations.value = availableTranslations.value!!.toMutableList().apply {
            add(translationToRemove.copy(downloaded = false))
        }
        downloadedTranslations.value = downloadedTranslations.value!!.toMutableList().apply {
            removeIf { it.shortName == translationToRemove.shortName }
        }
    }
}
