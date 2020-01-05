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

package me.xizzhu.android.joshua.strongnumber

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.viewData
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAndLoadingAwareInteractor

class StrongNumberListInteractor(private val strongNumberManager: StrongNumberManager,
                                 private val bibleReadingManager: BibleReadingManager,
                                 settingsManager: SettingsManager,
                                 dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAndLoadingAwareInteractor(settingsManager, dispatcher) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val strongNumberRequest: BroadcastChannel<String> = ConflatedBroadcastChannel()

    fun strongNumberRequest(): Flow<String> = strongNumberRequest.asFlow()

    fun requestStrongNumber(sn: String) {
        strongNumberRequest.offer(sn)
    }

    suspend fun readVerseIndexes(sn: String): ViewData<List<VerseIndex>> =
            viewData { strongNumberManager.read(sn) }

    suspend fun currentTranslation(): ViewData<String> =
            viewData { bibleReadingManager.currentTranslation().first() }

    suspend fun bookNames(currentTranslation: String): ViewData<List<String>> =
            viewData { bibleReadingManager.readBookNames(currentTranslation) }

    suspend fun bookShortNames(currentTranslation: String): ViewData<List<String>> =
            viewData { bibleReadingManager.readBookShortNames(currentTranslation) }

    suspend fun verses(currentTranslation: String, verseIndexes: List<VerseIndex>): ViewData<Map<VerseIndex, Verse>> =
            viewData { bibleReadingManager.readVerses(currentTranslation, verseIndexes) }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}
