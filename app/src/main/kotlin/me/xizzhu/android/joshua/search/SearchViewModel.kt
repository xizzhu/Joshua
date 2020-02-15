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

package me.xizzhu.android.joshua.search

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.infra.arch.ViewData

class SearchViewModel(private val bibleReadingManager: BibleReadingManager,
                      settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val query: BroadcastChannel<ViewData<String>> = ConflatedBroadcastChannel()

    fun query(): Flow<ViewData<String>> = query.asFlow()

    fun updateQuery(query: String) {
        this.query.offer(ViewData.loading(query))
    }

    fun submitQuery(query: String) {
        this.query.offer(ViewData.success(query))
    }

    suspend fun currentTranslation(): String =
            bibleReadingManager.currentTranslation().first { it.isNotEmpty() }

    suspend fun search(currentTranslation: String, query: String): List<Verse> =
            bibleReadingManager.search(currentTranslation, query)

    suspend fun bookNames(currentTranslation: String): List<String> =
            bibleReadingManager.readBookNames(currentTranslation)

    suspend fun bookShortNames(currentTranslation: String): List<String> =
            bibleReadingManager.readBookShortNames(currentTranslation)

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}
