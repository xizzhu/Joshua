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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.db.MetadataDao
import me.xizzhu.android.joshua.core.repository.local.android.db.withTransaction

class AndroidReadingStorage(private val androidDatabase: AndroidDatabase) : LocalReadingStorage {
    override suspend fun readCurrentVerseIndex(): VerseIndex {
        return withContext(Dispatchers.IO) {
            val keys = listOf(
                    Pair(MetadataDao.KEY_CURRENT_BOOK_INDEX, "0"),
                    Pair(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, "0"),
                    Pair(MetadataDao.KEY_CURRENT_VERSE_INDEX, "0")
            )
            val values = androidDatabase.metadataDao.read(keys)
            VerseIndex(values.getValue(MetadataDao.KEY_CURRENT_BOOK_INDEX).toInt(),
                    values.getValue(MetadataDao.KEY_CURRENT_CHAPTER_INDEX).toInt(),
                    values.getValue(MetadataDao.KEY_CURRENT_VERSE_INDEX).toInt())
        }
    }

    override suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        withContext(Dispatchers.IO) {
            val entries = listOf(
                    Pair(MetadataDao.KEY_CURRENT_BOOK_INDEX, verseIndex.bookIndex.toString()),
                    Pair(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, verseIndex.chapterIndex.toString()),
                    Pair(MetadataDao.KEY_CURRENT_VERSE_INDEX, verseIndex.verseIndex.toString())
            )
            androidDatabase.metadataDao.save(entries)
        }
    }

    override suspend fun readCurrentTranslation(): String = withContext(Dispatchers.IO) {
        androidDatabase.metadataDao.read(MetadataDao.KEY_CURRENT_TRANSLATION, "")
    }

    override suspend fun saveCurrentTranslation(translationShortName: String) {
        withContext(Dispatchers.IO) {
            androidDatabase.metadataDao.save(MetadataDao.KEY_CURRENT_TRANSLATION, translationShortName)
        }
    }

    override suspend fun readBookNames(translationShortName: String): List<String> = withContext(Dispatchers.IO) {
        androidDatabase.bookNamesDao.read(translationShortName)
    }

    override suspend fun readBookShortNames(translationShortName: String): List<String> = withContext(Dispatchers.IO) {
        androidDatabase.bookNamesDao.readShortName(translationShortName)
    }

    override suspend fun readVerses(translationShortName: String, bookIndex: Int,
                                    chapterIndex: Int): List<Verse> = withContext(Dispatchers.IO) {
        androidDatabase.translationDao.read(translationShortName, bookIndex, chapterIndex)
    }

    override suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                                    bookIndex: Int, chapterIndex: Int): List<Verse> = withContext(Dispatchers.IO) {
        androidDatabase.readableDatabase.withTransaction {
            val translations = mutableListOf(translationShortName)
            translations.addAll(parallelTranslations)
            val translationToTexts = androidDatabase.translationDao.read(translations, bookIndex, chapterIndex)
            val primaryTexts = translationToTexts.getValue(translationShortName)
            val verses = ArrayList<Verse>(primaryTexts.size)
            for ((i, primaryText) in primaryTexts.withIndex()) {
                val parallel = ArrayList<Verse.Text>(translationToTexts.size - 1)
                for ((translation, texts) in translationToTexts) {
                    if (translationShortName == translation) {
                        continue
                    }
                    parallel.add(if (texts.size > i) {
                        texts[i]
                    } else {
                        Verse.Text(translation, "")
                    })
                }

                verses.add(Verse(VerseIndex(bookIndex, chapterIndex, i), primaryText, parallel))
            }
            return@withContext verses
        }
    }

    override suspend fun readVerse(translationShortName: String, verseIndex: VerseIndex): Verse = withContext(Dispatchers.IO) {
        androidDatabase.translationDao.read(translationShortName, verseIndex)
    }

    override suspend fun readVerse(translationShortName: String, parallelTranslations: List<String>,
                                   verseIndex: VerseIndex): Verse = withContext(Dispatchers.IO) {
        androidDatabase.readableDatabase.withTransaction {
            val translations = mutableListOf(translationShortName)
            translations.addAll(parallelTranslations)
            val translationToText = androidDatabase.translationDao.read(translations, verseIndex).toMutableMap()
            val primaryText = translationToText.remove(translationShortName)!!
            return@withContext Verse(verseIndex, primaryText,
                    mutableListOf<Verse.Text>().apply { parallelTranslations.forEach { add(translationToText[it]!!) } })
        }
    }

    override suspend fun search(translationShortName: String, query: String): List<Verse> = withContext(Dispatchers.IO) {
        androidDatabase.translationDao.search(translationShortName, query)
    }
}
