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
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.db.MetadataDao
import me.xizzhu.android.joshua.core.repository.local.android.db.withTransaction

class AndroidTranslationStorage(private val androidDatabase: AndroidDatabase) : LocalTranslationStorage {
    override suspend fun readTranslationListRefreshTimestamp(): Long = withContext(Dispatchers.IO) {
        androidDatabase.metadataDao.read(MetadataDao.KEY_TRANSLATION_LIST_REFRESH_TIMESTAMP, "0").toLong()
    }

    override suspend fun saveTranslationListRefreshTimestamp(timestamp: Long) {
        withContext(Dispatchers.IO) {
            androidDatabase.metadataDao.save(MetadataDao.KEY_TRANSLATION_LIST_REFRESH_TIMESTAMP, timestamp.toString())
        }
    }

    override suspend fun readTranslations(): List<TranslationInfo> {
        return withContext(Dispatchers.IO) {
            androidDatabase.translationInfoDao.read()
        }
    }

    override suspend fun replaceTranslations(translations: List<TranslationInfo>) {
        withContext(Dispatchers.IO) {
            androidDatabase.translationInfoDao.replace(translations)
        }
    }

    override suspend fun saveTranslation(translationInfo: TranslationInfo,
                                         bookNames: List<String>,
                                         bookShortNames: List<String>,
                                         verses: Map<Pair<Int, Int>, List<String>>) {
        withContext(Dispatchers.IO) {
            androidDatabase.writableDatabase.withTransaction {
                androidDatabase.bookNamesDao.save(translationInfo.shortName, bookNames, bookShortNames)
                if (translationInfo.downloaded) {
                    androidDatabase.translationInfoDao.save(translationInfo)
                } else {
                    androidDatabase.translationInfoDao.save(translationInfo.copy(downloaded = true))
                }
                androidDatabase.translationDao.createTable(translationInfo.shortName)
                androidDatabase.translationDao.save(translationInfo.shortName, verses)
            }
        }
    }

    override suspend fun removeTranslation(translationInfo: TranslationInfo) {
        withContext(Dispatchers.IO) {
            androidDatabase.writableDatabase.withTransaction {
                androidDatabase.bookNamesDao.remove(translationInfo.shortName)
                if (translationInfo.downloaded) {
                    androidDatabase.translationInfoDao.save(translationInfo.copy(downloaded = false))
                } else {
                    androidDatabase.translationInfoDao.save(translationInfo)
                }
                androidDatabase.translationDao.removeTable(translationInfo.shortName)
            }
        }
    }
}
