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
    private val maxMemory = Runtime.getRuntime().maxMemory()
    private val bookNamesCache = object : LruCache<String, List<String>>((maxMemory / 16L).toInt()) {
        override fun sizeOf(key: String, bookNames: List<String>): Int {
            // strings are UTF-16 encoded (with a length of one or two 16-bit code units)
            var length = 0
            for (bookName in bookNames) {
                length += bookName.length * 4
            }
            return length
        }
    }
    private val versesCache = object : LruCache<String, List<Verse>>((maxMemory / 8L).toInt()) {
        override fun sizeOf(key: String, verses: List<Verse>): Int {
            // each Verse contains 3 integers and 2 strings (we don't cache parallel translations yet)
            // strings are UTF-16 encoded (with a length of one or two 16-bit code units)
            var length = 0
            for (verse in verses) {
                length += 12 + (verse.text.bookName.length + verse.text.translationShortName.length + verse.text.text.length) * 4
            }
            return length
        }
    }

    suspend fun readCurrentTranslation(): String = localReadingStorage.readCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        localReadingStorage.saveCurrentTranslation(translationShortName)
    }

    suspend fun readCurrentVerseIndex(): VerseIndex = localReadingStorage.readCurrentVerseIndex()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        localReadingStorage.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun readBookNames(translationShortName: String): List<String> {
        var bookNames = bookNamesCache.get(translationShortName)
        if (bookNames == null) {
            bookNames = localReadingStorage.readBookNames(translationShortName)
            bookNamesCache.put(translationShortName, bookNames)
        }
        return bookNames
    }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> {
        val key = "$translationShortName-$bookIndex-$chapterIndex"
        var verses = versesCache.get(key)
        if (verses == null) {
            verses = localReadingStorage.readVerses(
                    translationShortName, bookIndex, chapterIndex, readBookNames(translationShortName)[bookIndex])
            versesCache.put(key, verses)
        }
        return verses
    }

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> =
            localReadingStorage.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex)

    suspend fun search(translationShortName: String, query: String): List<Verse> =
            localReadingStorage.search(translationShortName, readBookNames(translationShortName), query)
}
