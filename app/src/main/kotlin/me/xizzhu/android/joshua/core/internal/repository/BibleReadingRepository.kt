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

package me.xizzhu.android.joshua.core.internal.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex

class BibleReadingRepository(private val localStorage: LocalStorage) {
    suspend fun readCurrentVerseIndex(): VerseIndex =
            withContext(Dispatchers.IO) {
                val metadataDao = localStorage.metadataDao
                val bookIndex = metadataDao.read(MetadataDao.KEY_CURRENT_BOOK_INDEX, "0").toInt()
                val chapterIndex = metadataDao.read(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, "0").toInt()
                val verseIndex = metadataDao.read(MetadataDao.KEY_CURRENT_VERSE_INDEX, "0").toInt()
                VerseIndex(bookIndex, chapterIndex, verseIndex)
            }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex): Unit =
            withContext(Dispatchers.IO) {
                val entries = ArrayList<Pair<String, String>>(3)
                entries.add(Pair(MetadataDao.KEY_CURRENT_BOOK_INDEX, verseIndex.bookIndex.toString()))
                entries.add(Pair(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, verseIndex.chapterIndex.toString()))
                entries.add(Pair(MetadataDao.KEY_CURRENT_VERSE_INDEX, verseIndex.verseIndex.toString()))
                localStorage.metadataDao.save(entries)
            }

    suspend fun readCurrentTranslation(): String =
            withContext(Dispatchers.IO) {
                localStorage.metadataDao.read(MetadataDao.KEY_CURRENT_TRANSLATION, "")
            }

    suspend fun saveCurrentTranslation(translationShortName: String): Unit =
            withContext(Dispatchers.IO) {
                localStorage.metadataDao.save(MetadataDao.KEY_CURRENT_TRANSLATION, translationShortName)
            }

    suspend fun readBookNames(translationShortName: String): List<String> =
            withContext(Dispatchers.IO) { localStorage.bookNamesDao.read(translationShortName) }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            withContext(Dispatchers.IO) {
                localStorage.translationDao.read(translationShortName, bookIndex, chapterIndex)
            }
}
