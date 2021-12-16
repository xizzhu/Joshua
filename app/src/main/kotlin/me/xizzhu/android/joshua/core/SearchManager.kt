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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.VerseAnnotationRepository
import me.xizzhu.android.joshua.utils.firstNotEmpty

data class SearchConfiguration(
        val includeOldTestament: Boolean, val includeNewTestament: Boolean,
        val includeBookmarks: Boolean, val includeHighlights: Boolean, val includeNotes: Boolean
)

data class VerseSearchQuery(val translation: String, val keyword: String, val includeOldTestament: Boolean, val includeNewTestament: Boolean)

data class SearchResult(
        val verses: List<Verse>,
        val bookmarks: List<Pair<Bookmark, Verse>>,
        val highlights: List<Pair<Highlight, Verse>>,
        val notes: List<Pair<Note, Verse>>
)

class SearchManager(
        private val bibleReadingRepository: BibleReadingRepository,
        private val bookmarkRepository: VerseAnnotationRepository<Bookmark>,
        private val highlightRepository: VerseAnnotationRepository<Highlight>,
        private val noteRepository: VerseAnnotationRepository<Note>
) {
    private val searchConfiguration: MutableStateFlow<SearchConfiguration> = MutableStateFlow(SearchConfiguration(
            includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true))

    fun configuration(): Flow<SearchConfiguration> = searchConfiguration

    fun saveConfiguration(config: SearchConfiguration) {
        searchConfiguration.value = config
    }

    suspend fun search(keyword: String): SearchResult {
        val config = searchConfiguration.value
        val currentTranslation = bibleReadingRepository.currentTranslation.firstNotEmpty()
        val verses = bibleReadingRepository.search(
                VerseSearchQuery(currentTranslation, keyword, config.includeOldTestament, config.includeNewTestament))

        val bookmarks: List<Pair<Bookmark, Verse>>
        val highlights: List<Pair<Highlight, Verse>>
        if (config.includeBookmarks || config.includeHighlights) {
            val versesWithIndexes = verses.associateBy { it.verseIndex }
            bookmarks = if (config.includeBookmarks) {
                bookmarkRepository.read(Constants.SORT_BY_BOOK).mapNotNull { bookmark ->
                    versesWithIndexes[bookmark.verseIndex]?.let { verse -> Pair(bookmark, verse) }
                }
            } else {
                emptyList()
            }
            highlights = if (config.includeHighlights) {
                highlightRepository.read(Constants.SORT_BY_BOOK).mapNotNull { highlight ->
                    versesWithIndexes[highlight.verseIndex]?.let { verse -> Pair(highlight, verse) }
                }
            } else {
                emptyList()
            }
        } else {
            bookmarks = emptyList()
            highlights = emptyList()
        }

        val notes = if (config.includeNotes) {
            noteRepository.search(keyword)
                    .takeIf { it.isNotEmpty() }
                    ?.let { notes ->
                        val versesWithNotes = bibleReadingRepository.readVerses(currentTranslation, notes.map { it.verseIndex })
                        arrayListOf<Pair<Note, Verse>>().apply {
                            ensureCapacity(notes.size)
                            notes.forEach { note ->
                                // TODO What to do if the notes is attached to a verse that is empty / not exist in this translation?
                                // https://github.com/xizzhu/Joshua/issues/153
                                versesWithNotes[note.verseIndex]?.let { add(Pair(note, it)) }
                            }
                        }
                    }
                    ?: emptyList()
        } else {
            emptyList()
        }

        return SearchResult(verses, bookmarks, highlights, notes)
    }
}
