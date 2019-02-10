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

import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.core.VerseIndex

class BibleReadingRepository(private val localStorage: LocalStorage) {
    @WorkerThread
    fun readCurrentVerseIndex(): VerseIndex {
        val metadataDao = localStorage.metadataDao
        val bookIndex = metadataDao.load(MetadataDao.KEY_CURRENT_BOOK_INDEX, "0").toInt()
        val chapterIndex = metadataDao.load(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, "0").toInt()
        val verseIndex = metadataDao.load(MetadataDao.KEY_CURRENT_VERSE_INDEX, "0").toInt()
        return VerseIndex(bookIndex, chapterIndex, verseIndex)
    }

    @WorkerThread
    fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        val entries = ArrayList<Pair<String, String>>(3)
        entries.add(Pair(MetadataDao.KEY_CURRENT_BOOK_INDEX, verseIndex.bookIndex.toString()))
        entries.add(Pair(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, verseIndex.chapterIndex.toString()))
        entries.add(Pair(MetadataDao.KEY_CURRENT_VERSE_INDEX, verseIndex.verseIndex.toString()))
        localStorage.metadataDao.save(entries)
    }

    @WorkerThread
    fun readCurrentTranslation(): String =
            localStorage.metadataDao.load(MetadataDao.KEY_CURRENT_TRANSLATION, "")

    @WorkerThread
    fun saveCurrentTranslation(translationShortName: String) {
        localStorage.metadataDao.save(MetadataDao.KEY_CURRENT_TRANSLATION, translationShortName)
    }

    @WorkerThread
    fun readBookNames(translationShortName: String): List<String> =
            localStorage.bookNamesDao.load(translationShortName)
}
