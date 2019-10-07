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

package me.xizzhu.android.joshua.progress

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAndLoadingAwareInteractor

class ReadingProgressInteractor(private val readingProgressManager: ReadingProgressManager,
                                private val bibleReadingManager: BibleReadingManager,
                                settingsManager: SettingsManager,
                                dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAndLoadingAwareInteractor(settingsManager, dispatcher) {
    suspend fun readReadingProgress(): Pair<List<String>, ReadingProgress> {
        try {
            updateLoadingState(ViewData.loading(Unit))
            val bookNames = bibleReadingManager.readBookNames(bibleReadingManager.observeCurrentTranslation().first())
            val readingProgress = readingProgressManager.read()
            updateLoadingState(ViewData.success(Unit))
            return Pair(bookNames, readingProgress)
        } catch (e: Exception) {
            updateLoadingState(ViewData.error(Unit, e))
            throw e
        }
    }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) = bibleReadingManager.saveCurrentVerseIndex(verseIndex)
}
