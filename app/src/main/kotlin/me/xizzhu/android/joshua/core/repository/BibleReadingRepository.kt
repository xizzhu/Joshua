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

package me.xizzhu.android.joshua.core.repository

import androidx.collection.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.VerseSearchQuery
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage
import me.xizzhu.android.logger.Log

class BibleReadingRepository(private val localReadingStorage: LocalReadingStorage, appScope: CoroutineScope) {
    companion object {
        private val TAG = BibleReadingRepository::class.java.simpleName
    }

    private val cache: BibleReadingCache = BibleReadingCache()

    private val _currentVerseIndex = MutableStateFlow<VerseIndex?>(null)
    val currentVerseIndex: Flow<VerseIndex> = _currentVerseIndex.filterNotNull()

    private val _currentTranslation = MutableStateFlow<String?>(null)
    val currentTranslation: Flow<String> = _currentTranslation.filterNotNull()

    private val _parallelTranslations = MutableStateFlow<List<String>?>(null)
    val parallelTranslations: Flow<List<String>> = _parallelTranslations.filterNotNull()

    init {
        appScope.launch {
            try {
                _currentVerseIndex.value = localReadingStorage.readCurrentVerseIndex()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize current verse index", e)
                _currentVerseIndex.value = VerseIndex.INVALID
            }
            try {
                _currentTranslation.value = localReadingStorage.readCurrentTranslation()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize current translation", e)
                _currentTranslation.value = ""
            }
            try {
                _parallelTranslations.value = localReadingStorage.readParallelTranslations()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize parallel translations", e)
                _parallelTranslations.value = emptyList()
            }
        }
    }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        _currentVerseIndex.value = verseIndex
        localReadingStorage.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun saveCurrentTranslation(translationShortName: String) {
        _currentTranslation.value = translationShortName
        localReadingStorage.saveCurrentTranslation(translationShortName)
    }

    suspend fun requestParallelTranslation(translationShortName: String) {
        (_parallelTranslations.value?.toMutableSet() ?: mutableSetOf()).run {
            if (add(translationShortName)) saveParallelTranslations(toList())
        }
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
        (_parallelTranslations.value?.toMutableSet() ?: mutableSetOf()).run {
            if (remove(translationShortName)) saveParallelTranslations(toList())
        }
    }

    suspend fun clearParallelTranslation() {
        saveParallelTranslations(emptyList())
    }

    suspend fun saveParallelTranslations(parallelTranslations: List<String>) {
        _parallelTranslations.value = parallelTranslations
        localReadingStorage.saveParallelTranslations(parallelTranslations)
    }

    suspend fun readBookNames(translationShortName: String): List<String> {
        return cache.getBookNames(translationShortName)
                ?: localReadingStorage.readBookNames(translationShortName).also { cache.putBookNames(translationShortName, it) }
    }

    suspend fun readBookShortNames(translationShortName: String): List<String> {
        return cache.getBookShortNames(translationShortName)
                ?: localReadingStorage.readBookShortNames(translationShortName).also { cache.putBookShortNames(translationShortName, it) }
    }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> {
        val key = "$translationShortName-$bookIndex-$chapterIndex"
        return cache.getVerses(key)
                ?: localReadingStorage.readVerses(translationShortName, bookIndex, chapterIndex).also { cache.putVerses(key, it) }
    }

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> =
            localReadingStorage.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex)

    suspend fun readVerses(translationShortName: String, verseIndexes: List<VerseIndex>): Map<VerseIndex, Verse> =
            localReadingStorage.readVerses(translationShortName, verseIndexes)

    suspend fun search(query: VerseSearchQuery): List<Verse> = localReadingStorage.search(query)
}

private class LruStringCache(maxSize: Int) : LruCache<String, List<String>>(maxSize) {
    // strings are UTF-16 encoded (with a length of one or two 16-bit code units)
    override fun sizeOf(key: String, value: List<String>): Int =
            key.length * 4 + value.sumOf { it.length * 4 }
}

private class LruVerseCache(maxSize: Int) : LruCache<String, List<Verse>>(maxSize) {
    // each Verse contains 3 integers and 2 strings (we don't cache parallel translations yet)
    // strings are UTF-16 encoded (with a length of one or two 16-bit code units)
    override fun sizeOf(key: String, verses: List<Verse>): Int =
            key.length * 4 + verses.sumOf { 12 + (it.text.translationShortName.length + it.text.text.length) * 4 }
}

private class BibleReadingCache {
    private val maxMemory = Runtime.getRuntime().maxMemory()
    private val bookNamesCache: LruStringCache = LruStringCache((maxMemory / 16L).toInt())
    private val bookShortNamesCache: LruStringCache = LruStringCache((maxMemory / 16L).toInt())
    private val versesCache: LruVerseCache = LruVerseCache((maxMemory / 8L).toInt())

    fun getBookNames(key: String): List<String>? = bookNamesCache.get(key)

    fun putBookNames(key: String, bookNames: List<String>) {
        bookNamesCache.put(key, bookNames)
    }

    fun getBookShortNames(key: String): List<String>? = bookShortNamesCache.get(key)

    fun putBookShortNames(key: String, bookShortNames: List<String>) {
        bookShortNamesCache.put(key, bookShortNames)
    }

    fun getVerses(key: String): List<Verse>? = versesCache.get(key)

    fun putVerses(key: String, verses: List<Verse>) {
        versesCache.put(key, verses)
    }
}
