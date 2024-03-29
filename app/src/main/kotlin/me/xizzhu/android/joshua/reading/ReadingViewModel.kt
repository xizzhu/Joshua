/*
 * Copyright (C) 2023 Xizhi Zhu
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

import android.app.Application
import androidx.annotation.IntDef
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.CrossReferencesManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.preview.PreviewViewData
import me.xizzhu.android.joshua.preview.loadPreview
import me.xizzhu.android.joshua.preview.toVersePreviewItems
import me.xizzhu.android.joshua.reading.detail.CrossReferenceItem
import me.xizzhu.android.joshua.reading.detail.StrongNumberItem
import me.xizzhu.android.joshua.reading.detail.VerseTextItem
import me.xizzhu.android.joshua.reading.verse.toVerseItems
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.utils.currentTimeMillis
import me.xizzhu.android.joshua.utils.filterIsValid
import me.xizzhu.android.joshua.utils.filterNotEmpty
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class CurrentReadingStatusViewData(
    val currentTranslation: String, val parallelTranslations: List<String>,
    val currentVerseIndex: VerseIndex, val bookNames: List<String>
)

class VersesViewData(val items: List<BaseItem>)

class VerseDetailViewData(
    val verseIndex: VerseIndex, val verseTextItems: List<VerseTextItem>,
    var bookmarked: Boolean, @Highlight.Companion.AvailableColor var highlightColor: Int, var note: String,
    var crossReferencedItems: List<CrossReferenceItem>, var strongNumberItems: List<StrongNumberItem>
)

class VerseUpdate(val verseIndex: VerseIndex, @Operation val operation: Int, val data: Any? = null) {
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

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val bibleReadingManager: BibleReadingManager,
    private val readingProgressManager: ReadingProgressManager,
    private val translationManager: TranslationManager,
    private val bookmarkManager: VerseAnnotationManager<Bookmark>,
    private val highlightManager: VerseAnnotationManager<Highlight>,
    private val noteManager: VerseAnnotationManager<Note>,
    private val crossReferencesManager: CrossReferencesManager,
    private val strongNumberManager: StrongNumberManager,
    settingsManager: SettingsManager,
    application: Application
) : BaseViewModel(settingsManager, application) {
    private val currentReadingStatus: MutableStateFlow<ViewData<CurrentReadingStatusViewData>> = MutableStateFlow(ViewData.Loading())
    private val verseUpdates: MutableSharedFlow<VerseUpdate> = MutableSharedFlow()

    init {
        combine(
            bibleReadingManager.currentTranslation().filterNotEmpty(),
            bibleReadingManager.parallelTranslations(),
            bibleReadingManager.currentVerseIndex().filterIsValid(),
        ) { currentTranslation, parallelTranslations, currentVerseIndex ->
            currentReadingStatus.value = ViewData.Success(CurrentReadingStatusViewData(
                currentTranslation = currentTranslation,
                parallelTranslations = parallelTranslations,
                currentVerseIndex = currentVerseIndex,
                bookNames = bibleReadingManager.readBookNames(currentTranslation),
            ))
        }.launchIn(viewModelScope)
    }

    fun currentReadingStatus(): Flow<ViewData<CurrentReadingStatusViewData>> = currentReadingStatus

    suspend fun hasDownloadedTranslation(): Boolean = translationManager.downloadedTranslations().first().isNotEmpty()

    fun selectTranslation(translationToSelect: String): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentTranslation(translationToSelect)

        try {
            bibleReadingManager.removeParallelTranslation(translationToSelect)
        } catch (e: Exception) {
            Log.e(tag, "Failed to remove the selected translation from parallel", e)
        }
    }.onFailure { Log.e(tag, "Failed to select translation", it) }

    suspend fun currentVerseIndex(): VerseIndex = bibleReadingManager.currentVerseIndex().first { it.isValid() }

    fun selectCurrentVerseIndex(verseIndex: VerseIndex): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }.onFailure { Log.e(tag, "Failed to select verse index", it) }

    suspend fun bookName(translationShortName: String, bookIndex: Int): String {
        return bibleReadingManager.readBookNames(translationShortName)[bookIndex]
    }

    fun loadVerses(bookIndex: Int, chapterIndex: Int): Flow<ViewData<VersesViewData>> = viewData {
        coroutineScope {
            val highlights = async { highlightManager.read(bookIndex, chapterIndex) }

            val simpleReadingModeOn = settings().first().simpleReadingModeOn
            val bookmarks = async { bookmarkManager.read(bookIndex, chapterIndex) }
            val notes = async { noteManager.read(bookIndex, chapterIndex) }

            val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
            val parallelTranslations = bibleReadingManager.parallelTranslations().first()
            val verses = if (parallelTranslations.isEmpty()) {
                bibleReadingManager.readVerses(currentTranslation, bookIndex, chapterIndex)
            } else {
                bibleReadingManager.readVerses(currentTranslation, parallelTranslations, bookIndex, chapterIndex)
            }

            VersesViewData(
                items = verses.toVerseItems(application, bookmarks.await(), highlights.await(), notes.await(), simpleReadingModeOn)
            )
        }
    }

    fun loadVerseDetail(verseIndex: VerseIndex): Flow<ViewData<VerseDetailViewData>> = viewData {
        coroutineScope {
            val bookmarked = async { bookmarkManager.read(verseIndex).isValid() }
            val highlightColor = async { highlightManager.read(verseIndex).takeIf { it.isValid() }?.color ?: Highlight.COLOR_NONE }
            val note = async { noteManager.read(verseIndex).takeIf { it.isValid() }?.note ?: "" }
            val crossReferences = async { readCrossReferences(verseIndex) }
            val strongNumbers = async { strongNumberManager.readStrongNumber(verseIndex).map { StrongNumberItem(it) } }

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
            if (verse == null) throw IllegalStateException("Failed to find verse '$verseIndex' for translation '$currentTranslation'")

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

            // step 3: constructs verse text items
            val verseTextItems = ArrayList<VerseTextItem>(parallelTranslations.size + 1)
            verseTextItems.add(VerseTextItem(
                verseIndex = verseIndex,
                followingEmptyVerseCount = followingEmptyVerseCount,
                verseText = verse.text,
                bookName = bibleReadingManager.readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex]
            ))
            parallelTranslations.forEachIndexed { index, translation ->
                verseTextItems.add(VerseTextItem(
                    verseIndex = verseIndex,
                    followingEmptyVerseCount = followingEmptyVerseCount,
                    verseText = Verse.Text(translation, parallel[index].toString()),
                    bookName = bibleReadingManager.readBookNames(translation)[verse.verseIndex.bookIndex]
                ))
            }

            VerseDetailViewData(
                verseIndex = verseIndex,
                verseTextItems = verseTextItems,
                bookmarked = bookmarked.await(),
                highlightColor = highlightColor.await(),
                note = note.await(),
                crossReferencedItems = crossReferences.await(),
                strongNumberItems = strongNumbers.await()
            )
        }
    }

    private suspend fun readCrossReferences(verseIndex: VerseIndex): List<CrossReferenceItem> =
        bibleReadingManager.readVerses(
            translationShortName = bibleReadingManager.currentTranslation().firstNotEmpty(),
            verseIndexes = crossReferencesManager.readCrossReferences(verseIndex).referenced
        ).map { (verseIndex, verse) ->
            CrossReferenceItem(
                verseIndex = verseIndex,
                verseText = verse.text,
                bookName = bibleReadingManager.readBookNames(verse.text.translationShortName)[verseIndex.bookIndex]
            )
        }.sortedBy { item -> item.verseIndex.bookIndex * 100000 + item.verseIndex.chapterIndex * 1000 + item.verseIndex.verseIndex }

    fun loadVersesForPreview(verseIndex: VerseIndex): Flow<ViewData<PreviewViewData>> =
        loadPreview(bibleReadingManager, settingsManager, verseIndex, ::toVersePreviewItems)
            .onFailure { Log.e(tag, "Failed to load verses for preview", it) }

    fun verseUpdates(): Flow<VerseUpdate> = verseUpdates

    fun updateVerse(update: VerseUpdate) {
        viewModelScope.launch { verseUpdates.emit(update) }
    }

    fun saveBookmark(verseIndex: VerseIndex, toBeBookmarked: Boolean): Flow<ViewData<Unit>> = viewData {
        if (toBeBookmarked) {
            bookmarkManager.save(Bookmark(verseIndex, currentTimeMillis()))
            verseUpdates.emit(VerseUpdate(verseIndex, VerseUpdate.BOOKMARK_ADDED))
        } else {
            bookmarkManager.remove(verseIndex)
            verseUpdates.emit(VerseUpdate(verseIndex, VerseUpdate.BOOKMARK_REMOVED))
        }
    }.onFailure { Log.e(tag, "Failed to update bookmark", it) }

    fun saveHighlight(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor color: Int): Flow<ViewData<Unit>> = viewData {
        if (color == Highlight.COLOR_NONE) {
            highlightManager.remove(verseIndex)
            verseUpdates.emit(VerseUpdate(verseIndex, VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_NONE))
        } else {
            highlightManager.save(Highlight(verseIndex, color, currentTimeMillis()))
            verseUpdates.emit(VerseUpdate(verseIndex, VerseUpdate.HIGHLIGHT_UPDATED, color))
        }
    }.onFailure { Log.e(tag, "Failed to update highlight", it) }

    fun saveNote(verseIndex: VerseIndex, note: String): Flow<ViewData<Unit>> = viewData {
        if (note.isEmpty()) {
            noteManager.remove(verseIndex)
            verseUpdates.emit(VerseUpdate(verseIndex, VerseUpdate.NOTE_REMOVED))
        } else {
            noteManager.save(Note(verseIndex, note, currentTimeMillis()))
            verseUpdates.emit(VerseUpdate(verseIndex, VerseUpdate.NOTE_ADDED))
        }
    }

    fun downloadCrossReferences(): Flow<ViewData<Int>> =
        crossReferencesManager.download()
            .map { progress ->
                when (progress) {
                    -1 -> ViewData.Failure(CancellationException("Cross references downloading cancelled by user"))
                    in 0 until 100 -> ViewData.Loading(progress)
                    else -> ViewData.Success(100)
                }
            }
            .catch { e ->
                Log.e(tag, "Failed to download cross references", e)
                emit(ViewData.Failure(e))
            }

    fun crossReferences(verseIndex: VerseIndex): Flow<ViewData<List<CrossReferenceItem>>> = viewData { readCrossReferences(verseIndex) }

    fun downloadStrongNumber(): Flow<ViewData<Int>> =
        strongNumberManager.download()
            .map { progress ->
                when (progress) {
                    -1 -> ViewData.Failure(CancellationException("Strong Number downloading cancelled by user"))
                    in 0 until 100 -> ViewData.Loading(progress)
                    else -> ViewData.Success(100)
                }
            }
            .catch { e ->
                Log.e(tag, "Failed to download Strong Number", e)
                emit(ViewData.Failure(e))
            }

    fun strongNumbers(verseIndex: VerseIndex): Flow<ViewData<List<StrongNumberItem>>> = viewData {
        strongNumberManager.readStrongNumber(verseIndex).map { StrongNumberItem(it) }
    }

    fun startTrackingReadingProgress() {
        readingProgressManager.startTracking()
    }

    fun stopTrackingReadingProgress() {
        readingProgressManager.stopTracking()
    }
}
