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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

class BibleReadingManager(private val bibleReadingRepository: BibleReadingRepository,
                          translationManager: TranslationManager) {
    companion object {
        private val TAG = BibleReadingManager::class.java.simpleName
    }

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val currentVerseIndex: BroadcastChannel<VerseIndex> = ConflatedBroadcastChannel()
    private val currentTranslationShortName: BroadcastChannel<String> = ConflatedBroadcastChannel()
    private val parallelTranslations: ConflatedBroadcastChannel<List<String>> = ConflatedBroadcastChannel(emptyList())
    private val downloadedTranslations: MutableSet<String> = mutableSetOf()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                currentVerseIndex.offer(bibleReadingRepository.readCurrentVerseIndex())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize current verse index", e)
                currentVerseIndex.offer(VerseIndex.INVALID)
            }
            try {
                currentTranslationShortName.offer(bibleReadingRepository.readCurrentTranslation())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize current translation", e)
                currentTranslationShortName.offer("")
            }
        }
        GlobalScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.IO) {
                parallelTranslations.offer(bibleReadingRepository.readParallelTranslations())
            }

            translationManager.observeDownloadedTranslations().collect {
                downloadedTranslations.clear()
                it.forEach { translation -> downloadedTranslations.add(translation.shortName) }

                parallelTranslations.value.toList().let { current ->
                    current.filter { parallel -> downloadedTranslations.contains(parallel) }.let { updated ->
                        if (current != updated) {
                            parallelTranslations.offer(updated)
                        }
                    }
                }
            }
        }
    }

    fun observeCurrentVerseIndex(): Flow<VerseIndex> = currentVerseIndex.asFlow()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        currentVerseIndex.offer(verseIndex)
        bibleReadingRepository.saveCurrentVerseIndex(verseIndex)
    }

    fun observeCurrentTranslation(): Flow<String> = currentTranslationShortName.asFlow()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        currentTranslationShortName.offer(translationShortName)
        bibleReadingRepository.saveCurrentTranslation(translationShortName)
    }

    fun observeParallelTranslations(): Flow<List<String>> = parallelTranslations.asFlow()

    suspend fun requestParallelTranslation(translationShortName: String) {
        val parallel = parallelTranslations.value.toMutableSet()
        if (parallel.add(translationShortName)) {
            parallel.toList().run {
                parallelTranslations.offer(this)
                bibleReadingRepository.saveParallelTranslations(this)
            }
        }
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
        val parallel = parallelTranslations.value.toMutableSet()
        if (parallel.remove(translationShortName)) {
            parallel.toList().run {
                parallelTranslations.offer(this)
                bibleReadingRepository.saveParallelTranslations(this)
            }
        }
    }

    suspend fun clearParallelTranslation() {
        parallelTranslations.offer(emptyList())
        bibleReadingRepository.saveParallelTranslations(emptyList())
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

    suspend fun search(translationShortName: String, query: String): List<Verse> =
            bibleReadingRepository.search(translationShortName, query)
}
