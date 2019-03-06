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

package me.xizzhu.android.joshua.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.firstOrNull
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.VerseIndex

class SearchViewController(private val searchActivity: SearchActivity,
                           private val navigator: Navigator,
                           private val bibleReadingManager: BibleReadingManager) {
    private val searchState: BroadcastChannel<Boolean> = ConflatedBroadcastChannel(false)
    private val searchResult: BroadcastChannel<SearchResult> = ConflatedBroadcastChannel(SearchResult.INVALID)

    fun observeSearchState(): ReceiveChannel<Boolean> = searchState.openSubscription()

    fun observeSearchResult(): ReceiveChannel<SearchResult> = searchResult.openSubscription()

    suspend fun selectVerse(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun search(query: String) {
        searchState.send(true)

        try {
            val currentTranslation = bibleReadingManager.observeCurrentTranslation().firstOrNull()
                    ?: throw IllegalStateException("No translation selected")
            searchResult.send(withContext(Dispatchers.Default) {
                val bookNamesAsync = async { bibleReadingManager.readBookNames(currentTranslation) }
                val versesAsync = async { bibleReadingManager.search(currentTranslation, query) }
                val bookNames = bookNamesAsync.await()
                val verses = versesAsync.await()
                val searchedVerses = ArrayList<SearchResult.Verse>()
                for (verse in verses) {
                    searchedVerses.add(SearchResult.Verse(
                            verse.verseIndex, bookNames[verse.verseIndex.bookIndex], verse.text))
                }
                SearchResult(currentTranslation, searchedVerses)
            })
        } finally {
            searchState.send(false)
        }
    }

    fun openReading() {
        navigator.navigate(searchActivity, Navigator.SCREEN_READING)
    }
}
