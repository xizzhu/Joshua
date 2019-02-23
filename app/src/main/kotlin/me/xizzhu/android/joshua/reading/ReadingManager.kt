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

import androidx.annotation.WorkerThread
import kotlinx.coroutines.channels.ReceiveChannel
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.repository.BibleReadingRepository
import me.xizzhu.android.joshua.repository.TranslationRepository

class ReadingManager(private val bibleReadingRepository: BibleReadingRepository,
                     private val translationRepository: TranslationRepository) {
    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> =
            translationRepository.observeDownloadedTranslations()

    fun observeCurrentTranslation(): ReceiveChannel<String> = bibleReadingRepository.observeCurrentTranslation()

    @WorkerThread
    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingRepository.saveCurrentTranslation(translationShortName)
    }

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = bibleReadingRepository.observeCurrentVerseIndex()

    @WorkerThread
    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingRepository.saveCurrentVerseIndex(verseIndex)
    }

    @WorkerThread
    fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingRepository.readVerses(translationShortName, bookIndex, chapterIndex)

    @WorkerThread
    fun readBookNames(translationShortName: String): List<String> =
            bibleReadingRepository.readBookNames(translationShortName)
}
