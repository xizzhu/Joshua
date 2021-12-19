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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.VerseAnnotationRepository
import me.xizzhu.android.joshua.utils.firstNotEmpty
import java.util.*

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

    suspend fun search(keyword: String): SearchResult = withContext(Dispatchers.Default) {
        val config = searchConfiguration.value
        val currentTranslation = bibleReadingRepository.currentTranslation.firstNotEmpty()
        val verses = async {
            bibleReadingRepository.search(
                    VerseSearchQuery(currentTranslation, keyword, config.includeOldTestament, config.includeNewTestament)
            )
        }
        val bookmarks = async {
            if (config.includeBookmarks) {
                searchAnnotations(bookmarkRepository, currentTranslation, keyword)
            } else {
                emptyList()
            }
        }
        val highlights = async {
            if (config.includeHighlights) {
                searchAnnotations(highlightRepository, currentTranslation, keyword)
            } else {
                emptyList()
            }
        }
        val notes = async {
            if (config.includeNotes) {
                searchNotes(currentTranslation, keyword)
            } else {
                emptyList()
            }
        }
        return@withContext SearchResult(verses.await(), bookmarks.await(), highlights.await(), notes.await())
    }

    private suspend fun <V : VerseAnnotation> searchAnnotations(
            verseAnnotationRepository: VerseAnnotationRepository<V>, currentTranslation: String, keyword: String
    ): List<Pair<V, Verse>> =
            verseAnnotationRepository.read(Constants.SORT_BY_BOOK)
                    .takeIf { it.isNotEmpty() }
                    ?.let { annotations ->
                        val keywords = keyword.toKeywords()
                        val verses = bibleReadingRepository.readVerses(currentTranslation, annotations.map { it.verseIndex })
                        annotations.mapNotNull { annotation ->
                            // TODO What to do if the annotation is attached to a verse that is empty / not exist in this translation?
                            // https://github.com/xizzhu/Joshua/issues/153
                            verses[annotation.verseIndex]
                                    ?.takeIf { it.text.text.isNotEmpty() }
                                    ?.takeIf { verse -> keywords.any { verse.text.text.lowercase(Locale.getDefault()).contains(it, false) } }
                                    ?.let { Pair(annotation, it) }
                        }
                    }
                    ?: emptyList()

    private suspend fun searchNotes(currentTranslation: String, keyword: String): List<Pair<Note, Verse>> =
            noteRepository.search(keyword)
                    .takeIf { it.isNotEmpty() }
                    ?.let { notes ->
                        val verses = bibleReadingRepository.readVerses(currentTranslation, notes.map { it.verseIndex })
                        notes.mapNotNull { note ->
                            // TODO What to do if the note is attached to a verse that is empty / not exist in this translation?
                            // https://github.com/xizzhu/Joshua/issues/153
                            verses[note.verseIndex]?.takeIf { it.text.text.isNotEmpty() }?.let { Pair(note, it) }
                        }
                    }
                    ?: emptyList()
}

fun String.toKeywords(): List<String> {
    if (isEmpty()) return emptyList()

    val results = arrayListOf<String>()
    val sb = StringBuilder()
    lowercase(Locale.getDefault()).forEach { c ->
        if (c.isWhitespace()) {
            if (sb.isNotEmpty()) {
                results.add(sb.toString())
                sb.clear()
            }
        } else {
            sb.append(c)
        }
    }
    if (sb.isNotEmpty()) {
        results.add(sb.toString())
    }
    return results
}
