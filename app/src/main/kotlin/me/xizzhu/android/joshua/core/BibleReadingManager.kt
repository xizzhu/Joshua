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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.logger.Log

data class VerseIndex(val bookIndex: Int, val chapterIndex: Int, val verseIndex: Int) {
    companion object {
        val INVALID = VerseIndex(-1, -1, -1)
    }

    fun isValid(): Boolean =
            bookIndex >= 0 && bookIndex < Bible.BOOK_COUNT
                    && chapterIndex >= 0 && chapterIndex < Bible.getChapterCount(bookIndex)
                    && verseIndex >= 0
}

data class Verse(val verseIndex: VerseIndex, val text: Text, val parallel: List<Text>) {
    companion object {
        val INVALID = Verse(VerseIndex.INVALID, Text.INVALID, emptyList())
    }

    fun isValid(): Boolean {
        if (!verseIndex.isValid() || !text.isValid()) return false

        for (p in parallel) {
            if (!p.isValid()) return false
        }

        return true
    }

    data class Text(val translationShortName: String, val text: String) {
        companion object {
            val INVALID = Text("", "")
        }

        fun isValid(): Boolean = translationShortName.isNotEmpty() // text can be empty
    }
}

class BibleReadingManager(
        private val bibleReadingRepository: BibleReadingRepository,
        translationRepository: TranslationRepository,
        initDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private val TAG = BibleReadingManager::class.java.simpleName
    }

    init {
        GlobalScope.launch(initDispatcher) { translationRepository.downloadedTranslations.collect(::updateDownloadedTranslations) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun updateDownloadedTranslations(translations: List<TranslationInfo>) {
        try {
            val downloadedTranslations = translations.map { it.shortName }.toSet()

            if (downloadedTranslations.isEmpty()) {
                bibleReadingRepository.saveCurrentTranslation("")
                bibleReadingRepository.clearParallelTranslation()
            } else {
                var currentTranslation = currentTranslation().first()
                if (currentTranslation.isEmpty() || !downloadedTranslations.contains(currentTranslation)) {
                    currentTranslation = translations.first().shortName
                    bibleReadingRepository.saveCurrentTranslation(currentTranslation)
                }

                parallelTranslations().first().let { current ->
                    val updated = current.filter { currentTranslation != it && downloadedTranslations.contains(it) }
                    if (current != updated) bibleReadingRepository.saveParallelTranslations(updated)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while observing downloaded translations", e)
        }
    }

    fun currentVerseIndex(): Flow<VerseIndex> = bibleReadingRepository.currentVerseIndex

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingRepository.saveCurrentVerseIndex(verseIndex)
    }

    fun currentTranslation(): Flow<String> = bibleReadingRepository.currentTranslation

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingRepository.saveCurrentTranslation(translationShortName)
    }

    fun parallelTranslations(): Flow<List<String>> = bibleReadingRepository.parallelTranslations

    suspend fun requestParallelTranslation(translationShortName: String) {
        bibleReadingRepository.requestParallelTranslation(translationShortName)
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
        bibleReadingRepository.removeParallelTranslation(translationShortName)
    }

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingRepository.readBookNames(translationShortName)

    suspend fun readBookShortNames(translationShortName: String): List<String> =
            bibleReadingRepository.readBookShortNames(translationShortName)

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingRepository.readVerses(translationShortName, bookIndex, chapterIndex)

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingRepository.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex)

    suspend fun readVerses(translationShortName: String, verseIndexes: List<VerseIndex>): Map<VerseIndex, Verse> =
            bibleReadingRepository.readVerses(translationShortName, verseIndexes)
}
