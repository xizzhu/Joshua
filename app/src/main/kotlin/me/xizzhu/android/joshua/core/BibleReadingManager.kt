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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
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
        if (!verseIndex.isValid() || !text.isValid()) {
            return false
        }
        for (p in parallel) {
            if (!p.isValid()) {
                return false
            }
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

class BibleReadingManager(private val bibleReadingRepository: BibleReadingRepository) {
    companion object {
        private val TAG = BibleReadingManager::class.java.simpleName
    }

    private val currentVerseIndex: BroadcastChannel<VerseIndex> = ConflatedBroadcastChannel()
    private val currentTranslationShortName: BroadcastChannel<String> = ConflatedBroadcastChannel()
    private val parallelTranslations: ConflatedBroadcastChannel<List<String>> = ConflatedBroadcastChannel(emptyList())

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                currentVerseIndex.send(bibleReadingRepository.readCurrentVerseIndex())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize current verse index", e)
                currentVerseIndex.send(VerseIndex.INVALID)
            }
            try {
                currentTranslationShortName.send(bibleReadingRepository.readCurrentTranslation())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize current translation", e)
                currentTranslationShortName.send("")
            }
        }
    }

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = currentVerseIndex.openSubscription()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        currentVerseIndex.send(verseIndex)
        bibleReadingRepository.saveCurrentVerseIndex(verseIndex)
    }

    fun observeCurrentTranslation(): ReceiveChannel<String> = currentTranslationShortName.openSubscription()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        currentTranslationShortName.send(translationShortName)
        bibleReadingRepository.saveCurrentTranslation(translationShortName)
    }

    fun observeParallelTranslations(): ReceiveChannel<List<String>> = parallelTranslations.openSubscription()

    suspend fun requestParallelTranslation(translationShortName: String) {
        val parallel = parallelTranslations.value.toMutableSet()
        if (parallel.add(translationShortName)) {
            parallelTranslations.send(parallel.toList())
        }
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
        val parallel = parallelTranslations.value.toMutableSet()
        if (parallel.remove(translationShortName)) {
            parallelTranslations.send(parallel.toList())
        }
    }

    suspend fun clearParallelTranslation() {
        parallelTranslations.send(emptyList())
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

    suspend fun readVerse(translationShortName: String, verseIndex: VerseIndex): Verse =
            bibleReadingRepository.readVerse(translationShortName, verseIndex)

    suspend fun readVerse(translationShortName: String, parallelTranslations: List<String>,
                          verseIndex: VerseIndex): Verse =
            bibleReadingRepository.readVerse(translationShortName, parallelTranslations, verseIndex)

    suspend fun search(translationShortName: String, query: String): List<Verse> =
            bibleReadingRepository.search(translationShortName, query)
}
