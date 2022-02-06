/*
 * Copyright (C) 2022 Xizhi Zhu
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
import me.xizzhu.android.joshua.core.CrossReferences
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalCrossReferencesStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase

class AndroidCrossReferencesStorage(private val androidDatabase: AndroidDatabase) : LocalCrossReferencesStorage {
    override suspend fun readCrossReferences(verseIndex: VerseIndex): CrossReferences =
            withContext(Dispatchers.IO) { androidDatabase.crossReferencesDao.read(verseIndex) }

    override suspend fun save(references: Map<VerseIndex, List<VerseIndex>>) {
        withContext(Dispatchers.IO) { androidDatabase.crossReferencesDao.replace(references) }
    }
}
