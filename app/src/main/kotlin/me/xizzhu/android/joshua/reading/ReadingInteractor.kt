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
import kotlinx.coroutines.channels.ReceiveChannel
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*

class ReadingInteractor(private val readingActivity: ReadingActivity,
                        private val navigator: Navigator,
                        private val bibleReadingManager: BibleReadingManager,
                        private val translationManager: TranslationManager) {
    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> =
            translationManager.observeDownloadedTranslations()

    fun observeCurrentTranslation(): ReceiveChannel<String> = bibleReadingManager.observeCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = bibleReadingManager.observeCurrentVerseIndex()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, bookIndex, chapterIndex)

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookNames(translationShortName)

    fun openSearch() {
        navigator.navigate(readingActivity, Navigator.SCREEN_SEARCH)
    }

    fun openTranslationManagement() {
        navigator.navigate(readingActivity, Navigator.SCREEN_TRANSLATION_MANAGEMENT)
    }

    fun finish() {
        readingActivity.finish()
    }

    fun startActionMode(callback: ActionMode.Callback): ActionMode? =
            readingActivity.startSupportActionMode(callback)
}
