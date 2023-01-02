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

package me.xizzhu.android.joshua.core.repository.local.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalVerseAnnotationStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.MetadataDao
import me.xizzhu.android.joshua.core.repository.local.android.db.VerseAnnotationDao

abstract class AndroidVerseAnnotationStorage<T : VerseAnnotation>(
        private val verseAnnotationDao: VerseAnnotationDao<T>,
        private val metadataDao: MetadataDao,
        private val sortOrderKey: String) : LocalVerseAnnotationStorage<T> {
    override suspend fun readSortOrder(): Int = withContext(Dispatchers.IO) {
        metadataDao.read(sortOrderKey, "0").toInt()
    }

    override suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        withContext(Dispatchers.IO) { metadataDao.save(sortOrderKey, sortOrder.toString()) }
    }

    override suspend fun read(@Constants.SortOrder sortOrder: Int): List<T> = withContext(Dispatchers.IO) {
        verseAnnotationDao.read(sortOrder)
    }

    override suspend fun read(bookIndex: Int, chapterIndex: Int): List<T> = withContext(Dispatchers.IO) {
        verseAnnotationDao.read(bookIndex, chapterIndex)
    }

    override suspend fun read(verseIndex: VerseIndex): T = withContext(Dispatchers.IO) {
        verseAnnotationDao.read(verseIndex)
    }

    override suspend fun search(query: String): List<T> = withContext(Dispatchers.IO) {
        verseAnnotationDao.search(query)
    }

    override suspend fun save(verseAnnotation: T) {
        withContext(Dispatchers.IO) { verseAnnotationDao.save(verseAnnotation) }
    }

    override suspend fun save(verseAnnotations: List<T>) {
        withContext(Dispatchers.IO) { verseAnnotationDao.save(verseAnnotations) }
    }

    override suspend fun remove(verseIndex: VerseIndex) {
        withContext(Dispatchers.IO) { verseAnnotationDao.remove(verseIndex) }
    }
}
