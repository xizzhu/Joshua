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

package me.xizzhu.android.joshua.core.repository

import androidx.collection.LruCache
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage

class BibleReadingRepository(private val localReadingStorage: LocalReadingStorage) {
    private val cache: BibleReadingCache = BibleReadingCache()

    suspend fun readCurrentTranslation(): String = localReadingStorage.readCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        localReadingStorage.saveCurrentTranslation(translationShortName)
    }

    suspend fun readCurrentVerseIndex(): VerseIndex = localReadingStorage.readCurrentVerseIndex()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        localReadingStorage.saveCurrentVerseIndex(verseIndex)
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

    suspend fun readVerse(translationShortName: String, verseIndex: VerseIndex): Verse =
            localReadingStorage.readVerse(translationShortName, verseIndex)

    suspend fun search(translationShortName: String, query: String): List<Verse> =
            localReadingStorage.search(translationShortName, query)
}

private class LruStringCache(maxSize: Int) : LruCache<String, List<String>>(maxSize) {
    // strings are UTF-16 encoded (with a length of one or two 16-bit code units)
    override fun sizeOf(key: String, value: List<String>): Int =
            key.length * 4 + value.sumBy { it.length * 4 }
}

private class LruVerseCache(maxSize: Int) : LruCache<String, List<Verse>>(maxSize) {
    // each Verse contains 3 integers and 2 strings (we don't cache parallel translations yet)
    // strings are UTF-16 encoded (with a length of one or two 16-bit code units)
    override fun sizeOf(key: String, verses: List<Verse>): Int =
            key.length * 4 + verses.sumBy { 12 + (it.text.translationShortName.length + it.text.text.length) * 4 }
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
