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

package me.xizzhu.android.joshua.reading.toolbar

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.filterOnSuccess
import me.xizzhu.android.joshua.infra.arch.toViewData

class ReadingToolbarInteractor(private val bibleReadingManager: BibleReadingManager,
                               private val translationManager: TranslationManager,
                               dispatcher: CoroutineDispatcher = Dispatchers.Default) : Interactor(dispatcher) {
    fun downloadedTranslations(): Flow<ViewData<List<TranslationInfo>>> = translationManager.observeDownloadedTranslations()
            .distinctUntilChanged()
            .toViewData()

    fun currentTranslation(): Flow<ViewData<String>> = bibleReadingManager.observeCurrentTranslation()
            .filter { it.isNotEmpty() }
            .toViewData()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun parallelTranslations(): Flow<ViewData<List<String>>> = bibleReadingManager.observeParallelTranslations().toViewData()

    fun requestParallelTranslation(translationShortName: String) {
        bibleReadingManager.requestParallelTranslation(translationShortName)
    }

    fun removeParallelTranslation(translationShortName: String) {
        bibleReadingManager.removeParallelTranslation(translationShortName)
    }

    fun clearParallelTranslation() {
        bibleReadingManager.clearParallelTranslation()
    }

    fun currentVerseIndex(): Flow<ViewData<VerseIndex>> = bibleReadingManager.observeCurrentVerseIndex()
            .filter { it.isValid() }
            .toViewData()

    fun bookShortNames(): Flow<ViewData<List<String>>> = currentTranslation()
            .filterOnSuccess()
            .map { ViewData.success(bibleReadingManager.readBookShortNames(it)) }
}
