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
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel

data class SearchRequest(val query: String, val instantSearch: Boolean)

data class SearchResult(
        val query: String, val verses: List<Verse>,
        val bookNames: List<String>, val bookShortNames: List<String>
)

class SearchViewModel(private val bibleReadingManager: BibleReadingManager,
                      settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/2034 is done
    private val searchRequest: BroadcastChannel<SearchRequest> = ConflatedBroadcastChannel()

    fun requestSearch(request: SearchRequest) {
        this.searchRequest.offer(request)
    }

    fun searchRequest(): Flow<SearchRequest> = searchRequest.asFlow()

    fun search(query: String): Flow<SearchResult> = flow {
        val currentTranslation = bibleReadingManager.currentTranslation().first { it.isNotEmpty() }
        emit(SearchResult(
                query, bibleReadingManager.search(currentTranslation, query),
                bibleReadingManager.readBookNames(currentTranslation),
                bibleReadingManager.readBookShortNames(currentTranslation)
        ))
    }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}
