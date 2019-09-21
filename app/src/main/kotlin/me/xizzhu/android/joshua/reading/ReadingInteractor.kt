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

package me.xizzhu.android.joshua.reading

import androidx.appcompat.view.ActionMode
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.xizzhu.android.joshua.utils.activities.BaseSettingsInteractor
import me.xizzhu.android.logger.Log
import java.lang.StringBuilder

data class VerseUpdate(@Operation val operation: Int, val data: Any? = null) {
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

class ReadingInteractor(private val readingActivity: ReadingActivity,
                        private val navigator: Navigator,
                        private val bibleReadingManager: BibleReadingManager,
                        private val bookmarkManager: BookmarkManager,
                        private val highlightManager: HighlightManager,
                        private val noteManager: NoteManager,
                        private val readingProgressManager: ReadingProgressManager,
                        private val translationManager: TranslationManager,
                        settingsManager: SettingsManager) : BaseSettingsInteractor(settingsManager) {
    companion object {
        private val TAG = ReadingInteractor::class.java.simpleName
    }

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val verseDetailOpenState: ConflatedBroadcastChannel<Pair<VerseIndex, Int>> = ConflatedBroadcastChannel()
    private val verseUpdates: BroadcastChannel<Pair<VerseIndex, VerseUpdate>> = ConflatedBroadcastChannel()

    fun observeDownloadedTranslations(): Flow<List<TranslationInfo>> = translationManager.observeDownloadedTranslations()

    fun observeCurrentTranslation(): Flow<String> = bibleReadingManager.observeCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun observeParallelTranslations(): Flow<List<String>> = bibleReadingManager.observeParallelTranslations()

    fun observeVerseDetailOpenState(): Flow<Pair<VerseIndex, Int>> = verseDetailOpenState.asFlow()

    fun openVerseDetail(verseIndex: VerseIndex, page: Int) {
        verseDetailOpenState.offer(Pair(verseIndex, page))
    }

    /**
     * @return true if verse detail view was open, or false otherwise
     * */
    fun closeVerseDetail(): Boolean {
        verseDetailOpenState.valueOrNull?.let {
            if (it.first.isValid()) {
                verseDetailOpenState.offer(Pair(VerseIndex.INVALID, 0))
                return true
            }
        }
        return false
    }

    fun observeVerseUpdates(): Flow<Pair<VerseIndex, VerseUpdate>> = verseUpdates.asFlow()

    fun requestParallelTranslation(translationShortName: String) {
        bibleReadingManager.requestParallelTranslation(translationShortName)
    }

    fun removeParallelTranslation(translationShortName: String) {
        bibleReadingManager.removeParallelTranslation(translationShortName)
    }

    fun clearParallelTranslation() {
        bibleReadingManager.clearParallelTranslation()
    }

    fun observeCurrentVerseIndex(): Flow<VerseIndex> = bibleReadingManager.observeCurrentVerseIndex()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, bookIndex, chapterIndex)

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex)

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookNames(translationShortName)

    suspend fun readBookShortNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookShortNames(translationShortName)

    fun openSearch() {
        navigator.navigate(readingActivity, Navigator.SCREEN_SEARCH)
    }

    fun openTranslationManagement() {
        navigator.navigate(readingActivity, Navigator.SCREEN_TRANSLATION_MANAGEMENT)
    }

    fun openReadingProgress() {
        navigator.navigate(readingActivity, Navigator.SCREEN_READING_PROGRESS)
    }

    fun openBookmarks() {
        navigator.navigate(readingActivity, Navigator.SCREEN_BOOKMARKS)
    }

    fun openHighlights() {
        navigator.navigate(readingActivity, Navigator.SCREEN_HIGHLIGHTS)
    }

    fun openNotes() {
        navigator.navigate(readingActivity, Navigator.SCREEN_NOTES)
    }

    fun openSettings() {
        navigator.navigate(readingActivity, Navigator.SCREEN_SETTINGS)
    }

    fun finish() {
        readingActivity.finish()
    }

    fun startActionMode(callback: ActionMode.Callback): ActionMode? =
            readingActivity.startSupportActionMode(callback)

    suspend fun copyToClipBoard(verses: Collection<Verse>): Boolean {
        if (verses.isEmpty()) {
            return false
        }

        try {
            val verse = verses.first()
            val bookName = readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex]
            readingActivity.copy(verse.text.translationShortName + " " + bookName, toStringForSharing(verses, bookName))
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy", e)
            return false
        }
    }

    @VisibleForTesting
    fun toStringForSharing(verses: Collection<Verse>, bookName: String): String {
        val stringBuilder = StringBuilder()
        for (verse in verses.sortedBy { verse ->
            val verseIndex = verse.verseIndex
            verseIndex.bookIndex * 100000 + verseIndex.chapterIndex * 1000 + verseIndex.verseIndex
        }) {
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.append('\n')
            }
            if (verse.parallel.isEmpty()) {
                // format: <book name> <chapter index>:<verse index> <text>
                stringBuilder.append(bookName).append(' ')
                        .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1).append(' ')
                        .append(verse.text.text)
            } else {
                // format:
                // <book name> <chapter verseIndex>:<verse verseIndex>
                // <primary translation>: <verse text>
                // <parallel translation 1>: <verse text>
                // <parallel translation 2>: <verse text>
                stringBuilder.append(bookName).append(' ')
                        .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1).append('\n')
                        .append(verse.text.translationShortName).append(": ").append(verse.text.text).append('\n')
                for (text in verse.parallel) {
                    stringBuilder.append(text.translationShortName).append(": ").append(text.text).append('\n')
                }
                stringBuilder.setLength(stringBuilder.length - 1)
            }
        }
        return stringBuilder.toString()
    }

    suspend fun share(verses: Collection<Verse>): Boolean {
        if (verses.isEmpty()) {
            return false
        }

        try {
            val verse = verses.first()
            val bookName = readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex]
            return readingActivity.share(toStringForSharing(verses, bookName))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share", e)
            return false
        }
    }

    suspend fun startTrackingReadingProgress() {
        readingProgressManager.startTracking()
    }

    suspend fun stopTrackingReadingProgress() {
        readingProgressManager.stopTracking()
    }

    suspend fun readBookmarks(bookIndex: Int, chapterIndex: Int): List<Bookmark> = bookmarkManager.read(bookIndex, chapterIndex)

    suspend fun readBookmark(verseIndex: VerseIndex): Bookmark = bookmarkManager.read(verseIndex)

    suspend fun addBookmark(verseIndex: VerseIndex) {
        bookmarkManager.save(Bookmark(verseIndex, System.currentTimeMillis()))
        verseUpdates.offer(Pair(verseIndex, VerseUpdate(VerseUpdate.BOOKMARK_ADDED)))
    }

    suspend fun removeBookmark(verseIndex: VerseIndex) {
        bookmarkManager.remove(verseIndex)
        verseUpdates.offer(Pair(verseIndex, VerseUpdate(VerseUpdate.BOOKMARK_REMOVED)))
    }

    suspend fun readHighlights(bookIndex: Int, chapterIndex: Int): List<Highlight> =
            highlightManager.read(bookIndex, chapterIndex)

    suspend fun readHighlight(verseIndex: VerseIndex): Highlight = highlightManager.read(verseIndex)

    suspend fun saveHighlight(verseIndex: VerseIndex, @ColorInt color: Int) {
        highlightManager.save(Highlight(verseIndex, color, System.currentTimeMillis()))
        verseUpdates.offer(Pair(verseIndex, VerseUpdate(VerseUpdate.HIGHLIGHT_UPDATED, color)))
    }

    suspend fun removeHighlight(verseIndex: VerseIndex) {
        highlightManager.remove(verseIndex)
        verseUpdates.offer(Pair(verseIndex, VerseUpdate(VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_NONE)))
    }

    suspend fun readNotes(bookIndex: Int, chapterIndex: Int): List<Note> = noteManager.read(bookIndex, chapterIndex)

    suspend fun readNote(verseIndex: VerseIndex): Note = noteManager.read(verseIndex)

    suspend fun saveNote(verseIndex: VerseIndex, note: String) {
        noteManager.save(Note(verseIndex, note, System.currentTimeMillis()))
        verseUpdates.offer(Pair(verseIndex, VerseUpdate(VerseUpdate.NOTE_ADDED)))
    }

    suspend fun removeNote(verseIndex: VerseIndex) {
        noteManager.remove(verseIndex)
        verseUpdates.offer(Pair(verseIndex, VerseUpdate(VerseUpdate.NOTE_REMOVED)))
    }
}
