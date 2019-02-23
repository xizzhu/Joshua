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

package me.xizzhu.android.joshua.core

import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import me.xizzhu.android.joshua.repository.TranslationRepository

class TranslationManager(private val translationRepository: TranslationRepository) {
    fun observeAvailableTranslations(): ReceiveChannel<List<TranslationInfo>> =
            translationRepository.observeAvailableTranslations()

    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> =
            translationRepository.observeDownloadedTranslations()

    @WorkerThread
    suspend fun reload(forceRefresh: Boolean) {
        translationRepository.reload(forceRefresh)
    }

    @WorkerThread
    suspend fun downloadTranslation(progressChannel: SendChannel<Int>, translationInfo: TranslationInfo) {
        translationRepository.downloadTranslation(progressChannel, translationInfo)
    }
}
