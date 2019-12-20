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

import androidx.annotation.IntDef
import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwareViewModel
import me.xizzhu.android.joshua.reading.chapter.ChapterListInteractor
import me.xizzhu.android.joshua.reading.detail.VerseDetailInteractor
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbarInteractor
import me.xizzhu.android.joshua.reading.verse.VerseInteractor

data class VerseDetailRequest(val verseIndex: VerseIndex, @Content val content: Int) {
    companion object {
        const val VERSES = 0
        const val NOTE = 1

        @IntDef(VERSES, NOTE)
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

class ReadingViewModel(private val bibleReadingManager: BibleReadingManager,
                       private val readingProgressManager: ReadingProgressManager,
                       settingsManager: SettingsManager,
                       readingToolbarInteractor: ReadingToolbarInteractor,
                       chapterListInteractor: ChapterListInteractor,
                       private val verseInteractor: VerseInteractor,
                       private val verseDetailInteractor: VerseDetailInteractor,
                       dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAwareViewModel(settingsManager, listOf(readingToolbarInteractor, chapterListInteractor, verseInteractor, verseDetailInteractor), dispatcher) {
    @UiThread
    override fun onStart() {
        super.onStart()

        coroutineScope.launch { verseInteractor.verseDetailRequest().collect { verseDetailInteractor.requestVerseDetail(it) } }
        coroutineScope.launch { verseDetailInteractor.verseUpdates().collect { verseInteractor.updateVerse(it) } }
    }

    @UiThread
    override fun onResume() {
        super.onResume()

        readingProgressManager.startTracking()
    }

    @UiThread
    override fun onPause() {
        super.onPause()

        // uses GlobalScope to make sure this will be executed without being canceled
        // uses Dispatchers.Main.immediate to make sure this will be executed immediately
        GlobalScope.launch(Dispatchers.Main.immediate) { readingProgressManager.stopTracking() }
    }

    fun showNoteInVerseDetail() {
        coroutineScope.launch {
            bibleReadingManager.observeCurrentVerseIndex().first().let { verseIndex ->
                if (verseIndex.isValid()) {
                    // NOTE It's a hack here, because the only thing needed by verse interactor is to select the verse
                    verseInteractor.updateVerse(VerseUpdate(verseIndex, VerseUpdate.VERSE_SELECTED))

                    verseDetailInteractor.requestVerseDetail(VerseDetailRequest(verseIndex, VerseDetailRequest.NOTE))
                }
            }
        }
    }
}
