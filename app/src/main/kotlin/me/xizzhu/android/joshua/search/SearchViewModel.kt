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

package me.xizzhu.android.joshua.search

import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.utils.firstNotEmpty

data class SearchRequest(
        val query: String, val instantSearch: Boolean,
        val includeBookmarks: Boolean, val includeHighlights: Boolean, val includeNotes: Boolean
)

data class SearchResult(
        val query: String, val verses: List<Verse>,
        val bookmarks: List<Pair<Bookmark, Verse>>,
        val highlights: List<Pair<Highlight, Verse>>,
        val notes: List<Pair<Note, Verse>>,
        val bookNames: List<String>, val bookShortNames: List<String>
)

class SearchViewModel(private val bibleReadingManager: BibleReadingManager,
                      private val bookmarkManager: VerseAnnotationManager<Bookmark>,
                      private val highlightManager: VerseAnnotationManager<Highlight>,
                      private val noteManager: VerseAnnotationManager<Note>,
                      settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    private val _searchRequest = MutableStateFlow<SearchRequest?>(null)
    val searchRequest: Flow<SearchRequest> = _searchRequest.filterNotNull()

    fun requestSearch(request: SearchRequest) {
        _searchRequest.value = request
    }

    fun search(query: String, includeBookmarks: Boolean, includeHighlights: Boolean, includeNotes: Boolean): Flow<SearchResult> = flow {
        val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()

        val verses = bibleReadingManager.search(currentTranslation, query)
        val bookmarks: List<Pair<Bookmark, Verse>>
        val highlights: List<Pair<Highlight, Verse>>
        if (includeBookmarks || includeHighlights) {
            val versesWithIndexes = verses.associateBy { it.verseIndex }
            bookmarks = if (includeBookmarks) {
                bookmarkManager.read(Constants.SORT_BY_BOOK).mapNotNull { bookmark ->
                    versesWithIndexes[bookmark.verseIndex]?.let { verse -> Pair(bookmark, verse) }
                }
            } else {
                emptyList()
            }
            highlights = if (includeHighlights) {
                highlightManager.read(Constants.SORT_BY_BOOK).mapNotNull { highlight ->
                    versesWithIndexes[highlight.verseIndex]?.let { verse -> Pair(highlight, verse) }
                }
            } else {
                emptyList()
            }
        } else {
            bookmarks = emptyList()
            highlights = emptyList()
        }

        val notes = if (includeNotes) {
            noteManager.search(query).let { notes ->
                val versesWithNotes = bibleReadingManager.readVerses(currentTranslation, notes.map { it.verseIndex })
                arrayListOf<Pair<Note, Verse>>().apply {
                    ensureCapacity(notes.size)
                    notes.forEach { note ->
                        add(Pair(note, versesWithNotes.getValue(note.verseIndex)))
                    }
                }
            }
        } else {
            emptyList()
        }

        emit(SearchResult(
                query, verses, bookmarks, highlights, notes,
                bibleReadingManager.readBookNames(currentTranslation),
                bibleReadingManager.readBookShortNames(currentTranslation)
        ))
    }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}
