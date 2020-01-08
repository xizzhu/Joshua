/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading.chapter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.toViewData

class ChapterListInteractor(private val bibleReadingManager: BibleReadingManager,
                            dispatcher: CoroutineDispatcher = Dispatchers.Default) : Interactor(dispatcher) {
    fun bookNames(): Flow<ViewData<List<String>>> = bibleReadingManager.currentTranslation()
            .filter { it.isNotEmpty() }
            .map { ViewData.success(bibleReadingManager.readBookNames(it)) }

    fun currentVerseIndex(): Flow<ViewData<VerseIndex>> = bibleReadingManager.currentVerseIndex()
            .filter { it.isValid() }
            .toViewData()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}
