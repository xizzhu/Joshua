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

package me.xizzhu.android.joshua.core.repository.local.android

import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage

class AndroidReadingStorage(private val androidDatabase: AndroidDatabase) : LocalReadingStorage {
    @WorkerThread
    override fun readCurrentVerseIndex(): VerseIndex {
        val bookIndex = androidDatabase.metadataDao.read(MetadataDao.KEY_CURRENT_BOOK_INDEX, "0").toInt()
        val chapterIndex = androidDatabase.metadataDao.read(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, "0").toInt()
        val verseIndex = androidDatabase.metadataDao.read(MetadataDao.KEY_CURRENT_VERSE_INDEX, "0").toInt()
        return VerseIndex(bookIndex, chapterIndex, verseIndex)
    }

    @WorkerThread
    override fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        val entries = ArrayList<Pair<String, String>>(3)
        entries.add(Pair(MetadataDao.KEY_CURRENT_BOOK_INDEX, verseIndex.bookIndex.toString()))
        entries.add(Pair(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, verseIndex.chapterIndex.toString()))
        entries.add(Pair(MetadataDao.KEY_CURRENT_VERSE_INDEX, verseIndex.verseIndex.toString()))
        androidDatabase.metadataDao.save(entries)
    }

    @WorkerThread
    override fun readCurrentTranslation(): String {
        return androidDatabase.metadataDao.read(MetadataDao.KEY_CURRENT_TRANSLATION, "")
    }

    @WorkerThread
    override fun saveCurrentTranslation(translationShortName: String) {
        androidDatabase.metadataDao.save(MetadataDao.KEY_CURRENT_TRANSLATION, translationShortName)
    }

    @WorkerThread
    override fun readBookNames(translationShortName: String): List<String> {
        return androidDatabase.bookNamesDao.read(translationShortName)
    }

    @WorkerThread
    override fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> {
        return androidDatabase.translationDao.read(translationShortName, bookIndex, chapterIndex)
    }

    @WorkerThread
    override fun search(translationShortName: String, query: String): List<Verse> {
        return androidDatabase.translationDao.search(translationShortName, query)
    }
}
