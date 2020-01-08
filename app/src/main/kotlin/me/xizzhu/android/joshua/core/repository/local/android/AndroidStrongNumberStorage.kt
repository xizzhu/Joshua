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
import me.xizzhu.android.ask.db.transaction
import me.xizzhu.android.ask.db.withTransaction
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase

class AndroidStrongNumberStorage(private val androidDatabase: AndroidDatabase) : LocalStrongNumberStorage {
    override suspend fun readStrongNumber(strongNumber: String): StrongNumber = withContext(Dispatchers.IO) {
        androidDatabase.strongNumberWordDao.read(strongNumber)
    }

    override suspend fun readStrongNumber(verseIndex: VerseIndex): List<StrongNumber> = withContext(Dispatchers.IO) {
        return@withContext androidDatabase.writableDatabase.withTransaction {
            return@withTransaction androidDatabase.strongNumberWordDao.read(
                    androidDatabase.strongNumberIndexDao.read(verseIndex))
        }
    }

    override suspend fun readVerseIndexes(strongNumber: String): List<VerseIndex> = withContext(Dispatchers.IO) {
        androidDatabase.strongNumberReverseIndexDao.read(strongNumber)
    }

    override suspend fun save(strongNumberIndex: Map<VerseIndex, List<String>>,
                              strongNumberReverseIndexes: Map<String, List<VerseIndex>>,
                              strongNumberWords: Map<String, String>) {
        withContext(Dispatchers.IO) {
            androidDatabase.writableDatabase.transaction {
                androidDatabase.strongNumberIndexDao.replace(strongNumberIndex)
                androidDatabase.strongNumberReverseIndexDao.replace(strongNumberReverseIndexes)
                androidDatabase.strongNumberWordDao.replace(strongNumberWords)
            }
        }
    }
}
