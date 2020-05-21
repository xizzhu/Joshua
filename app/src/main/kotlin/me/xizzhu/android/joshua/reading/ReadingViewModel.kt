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

package me.xizzhu.android.joshua.reading

import androidx.annotation.IntDef
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.utils.currentTimeMillis
import me.xizzhu.android.joshua.utils.filterNotEmpty
import me.xizzhu.android.joshua.utils.firstNotEmpty

data class ChapterListViewData(val currentVerseIndex: VerseIndex, val bookNames: List<String>)

data class CurrentVerseIndexViewData(val verseIndex: VerseIndex, val bookName: String, val bookShortName: String)

data class CurrentTranslationViewData(val currentTranslation: String, val parallelTranslations: List<String>)

data class VersesViewData(
        val simpleReadingModeOn: Boolean, val verses: List<Verse>,
        val bookmarks: List<Bookmark>, val highlights: List<Highlight>, val notes: List<Note>
)

data class VerseDetailViewData(
        val verseIndex: VerseIndex, val followingEmptyVerseCount: Int, val verseTexts: List<VerseText>,
        val bookmarked: Boolean, @Highlight.Companion.AvailableColor val highlightColor: Int,
        val note: String, val strongNumbers: List<StrongNumber>
) {
    data class VerseText(val text: Verse.Text, val bookName: String)
}

data class VerseDetailRequest(val verseIndex: VerseIndex, @Content val content: Int) {
    companion object {
        const val VERSES = 0
        const val NOTE = 1
        const val STRONG_NUMBER = 2

        @IntDef(VERSES, NOTE, STRONG_NUMBER)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Content
    }
}

data class VerseUpdate(val verseIndex: VerseIndex, @Operation val operation: Int, val data: Any? = null) {
    companion object {
        const val VERSE_SELECTED = 1
        const val VERSE_DESELECTED = 2
        const val NOTE_ADDED = 3
        const val NOTE_REMOVED = 4
        const val BOOKMARK_ADDED = 5
        const val BOOKMARK_REMOVED = 6
        const val HIGHLIGHT_UPDATED = 7

        @IntDef(VERSE_SELECTED, VERSE_DESELECTED, NOTE_ADDED, NOTE_REMOVED,
                BOOKMARK_ADDED, BOOKMARK_REMOVED, HIGHLIGHT_UPDATED)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Operation
    }
}

class ReadingViewModel(
        private val bibleReadingManager: BibleReadingManager, private val readingProgressManager: ReadingProgressManager,
        private val translationManager: TranslationManager, private val bookmarkManager: VerseAnnotationManager<Bookmark>,
        private val highlightManager: VerseAnnotationManager<Highlight>, private val noteManager: VerseAnnotationManager<Note>,
        private val strongNumberManager: StrongNumberManager, settingsManager: SettingsManager
) : BaseSettingsViewModel(settingsManager) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/2034 is done
    private val verseDetailRequest: BroadcastChannel<VerseDetailRequest> = ConflatedBroadcastChannel()
    private val verseUpdates: BroadcastChannel<VerseUpdate> = ConflatedBroadcastChannel()

    fun downloadedTranslations(): Flow<List<TranslationInfo>> =
            translationManager.downloadedTranslations().distinctUntilChanged()

    suspend fun hasDownloadedTranslation(): Boolean =
            translationManager.downloadedTranslations().first().isNotEmpty()

    fun currentTranslation(): Flow<String> =
            bibleReadingManager.currentTranslation().filterNotEmpty()

    fun currentTranslationViewData(): Flow<CurrentTranslationViewData> =
            combine(bibleReadingManager.currentTranslation().filterNotEmpty(),
                    bibleReadingManager.parallelTranslations()) { current, parallel ->
                CurrentTranslationViewData(current, parallel)
            }

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    suspend fun requestParallelTranslation(translationShortName: String) {
        bibleReadingManager.requestParallelTranslation(translationShortName)
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
        bibleReadingManager.removeParallelTranslation(translationShortName)
    }

    suspend fun clearParallelTranslation() {
        bibleReadingManager.clearParallelTranslation()
    }

    fun currentVerseIndex(): Flow<VerseIndex> =
            bibleReadingManager.currentVerseIndex().filter { it.isValid() }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    fun currentVerseIndexViewData(): Flow<CurrentVerseIndexViewData> =
            combine(bibleReadingManager.currentTranslation().filterNotEmpty(),
                    bibleReadingManager.currentVerseIndex().filter { it.isValid() }) { currentTranslation, currentVerseIndex ->
                CurrentVerseIndexViewData(
                        currentVerseIndex,
                        bibleReadingManager.readBookNames(currentTranslation)[currentVerseIndex.bookIndex],
                        bibleReadingManager.readBookShortNames(currentTranslation)[currentVerseIndex.bookIndex]
                )
            }

    fun chapterList(): Flow<ChapterListViewData> {
        val currentVerseIndexFlow = bibleReadingManager.currentVerseIndex().filter { it.isValid() }
        val bookNamesFlow = bibleReadingManager.currentTranslation()
                .filterNotEmpty()
                .map { bibleReadingManager.readBookNames(it) }
        return combine(currentVerseIndexFlow, bookNamesFlow) { currentVerseIndex, bookNames ->
            ChapterListViewData(currentVerseIndex, bookNames)
        }
    }

    fun bookName(translationShortName: String, bookIndex: Int): Flow<String> = flow {
        emit(bibleReadingManager.readBookNames(translationShortName)[bookIndex])
    }

    fun verses(bookIndex: Int, chapterIndex: Int): Flow<VersesViewData> = flow {
        coroutineScope {
            val bookmarks = async { bookmarkManager.read(bookIndex, chapterIndex) }
            val highlights = async { highlightManager.read(bookIndex, chapterIndex) }
            val notes = async { noteManager.read(bookIndex, chapterIndex) }
            val verses = async {
                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val parallelTranslations = bibleReadingManager.parallelTranslations().first()
                if (parallelTranslations.isEmpty()) {
                    bibleReadingManager.readVerses(currentTranslation, bookIndex, chapterIndex)
                } else {
                    bibleReadingManager.readVerses(currentTranslation, parallelTranslations, bookIndex, chapterIndex)
                }
            }
            emit(VersesViewData(
                    settings().first().simpleReadingModeOn, verses.await(),
                    bookmarks.await(), highlights.await(), notes.await()
            ))
        }
    }

    fun verseDetail(verseIndex: VerseIndex): Flow<VerseDetailViewData> = flow {
        coroutineScope {
            val bookmarked = async { bookmarkManager.read(verseIndex).isValid() }
            val highlightColor = async { highlightManager.read(verseIndex).color }
            val note = async { noteManager.read(verseIndex).note }
            val strongNumbers = async { strongNumberManager.readStrongNumber(verseIndex) }

            // build the verse
            val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
            val parallelTranslations = translationManager.downloadedTranslations().first()
                    .filter { it.shortName != currentTranslation }
                    .sortedWith(TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME))
                    .map { it.shortName }
            val verses = bibleReadingManager.readVerses(currentTranslation, parallelTranslations,
                    verseIndex.bookIndex, verseIndex.chapterIndex)

            // step 1: finds the verse
            var start: VerseIndex? = null
            for (verse in verses) {
                if (verse.text.text.isNotEmpty()) start = verse.verseIndex // skip empty verses
                if (verse.verseIndex.verseIndex >= verseIndex.verseIndex) break
            }

            val verseIterator = verses.iterator()
            var verse: Verse? = null
            while (verseIterator.hasNext()) {
                val v = verseIterator.next()
                if (v.verseIndex == start) {
                    verse = v
                    break
                }
            }
            if (verse == null) throw IllegalStateException("Failed to find target verse")

            // step 2: builds the parallel
            val parallel = Array(parallelTranslations.size) { StringBuilder() }
            val parallelBuilder: (index: Int, Verse.Text) -> Unit = { index, text ->
                with(parallel[index]) {
                    if (isNotEmpty()) append(' ')
                    append(text.text)
                }
            }
            verse.parallel.forEachIndexed(parallelBuilder)

            var followingEmptyVerseCount = 0
            while (verseIterator.hasNext()) {
                val v = verseIterator.next()
                if (v.text.text.isNotEmpty()) break
                v.parallel.forEachIndexed(parallelBuilder)
                followingEmptyVerseCount++
            }

            // step 3: constructs verse texts
            val verseTexts = ArrayList<VerseDetailViewData.VerseText>(parallelTranslations.size + 1)
            verseTexts.add(VerseDetailViewData.VerseText(
                    verse.text,
                    bibleReadingManager.readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex]
            ))
            parallelTranslations.forEachIndexed { index, translation ->
                verseTexts.add(VerseDetailViewData.VerseText(
                        Verse.Text(translation, parallel[index].toString()),
                        bibleReadingManager.readBookNames(translation)[verse.verseIndex.bookIndex]
                ))
            }

            emit(VerseDetailViewData(
                    verseIndex, followingEmptyVerseCount, verseTexts,
                    bookmarked.await(), highlightColor.await(), note.await(), strongNumbers.await()
            ))
        }
    }

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex)

    suspend fun saveBookmark(verseIndex: VerseIndex, hasBookmark: Boolean) {
        if (hasBookmark) {
            bookmarkManager.remove(verseIndex)
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.BOOKMARK_REMOVED))
        } else {
            bookmarkManager.save(Bookmark(verseIndex, currentTimeMillis()))
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.BOOKMARK_ADDED))
        }
    }

    suspend fun saveHighlight(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor color: Int) {
        if (color == Highlight.COLOR_NONE) {
            highlightManager.remove(verseIndex)
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_NONE))
        } else {
            highlightManager.save(Highlight(verseIndex, color, currentTimeMillis()))
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.HIGHLIGHT_UPDATED, color))
        }
    }

    suspend fun saveNote(verseIndex: VerseIndex, note: String) {
        if (note.isEmpty()) {
            noteManager.remove(verseIndex)
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.NOTE_REMOVED))
        } else {
            noteManager.save(Note(verseIndex, note, currentTimeMillis()))
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.NOTE_ADDED))
        }
    }

    suspend fun readStrongNumber(verseIndex: VerseIndex): List<StrongNumber> = strongNumberManager.readStrongNumber(verseIndex)

    fun downloadStrongNumber(): Flow<Int> = strongNumberManager.download()

    fun startTracking() {
        readingProgressManager.startTracking()
    }

    fun stopTracking() {
        // uses GlobalScope to make sure this will be executed without being canceled
        // uses Dispatchers.Main.immediate to make sure this will be executed immediately
        GlobalScope.launch(Dispatchers.Main.immediate) { readingProgressManager.stopTracking() }
    }

    fun verseUpdates(): Flow<VerseUpdate> = verseUpdates.asFlow()

    fun verseDetailRequest(): Flow<VerseDetailRequest> = verseDetailRequest.asFlow()

    fun requestVerseDetail(request: VerseDetailRequest) {
        verseDetailRequest.offer(request)
    }

    fun showNoteInVerseDetail() {
        viewModelScope.launch {
            bibleReadingManager.currentVerseIndex().first().let { verseIndex ->
                if (verseIndex.isValid()) {
                    // NOTE It's a hack here, because the only thing needed by verse interactor is to select the verse
                    verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.VERSE_SELECTED))
                    verseDetailRequest.offer(VerseDetailRequest(verseIndex, VerseDetailRequest.NOTE))
                }
            }
        }
    }

    fun closeVerseDetail(verseIndex: VerseIndex) {
        // NOTE It's a hack here, because the only thing needed by the other end (verse interactor) is to deselect the verse
        verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.VERSE_DESELECTED))
    }
}
