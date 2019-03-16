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

import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase

class AndroidTranslationStorage(private val androidDatabase: AndroidDatabase) : LocalTranslationStorage {
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

    override suspend fun saveTranslation(translationInfo: TranslationInfo, bookNames: List<String>,
                                         verses: Map<Pair<Int, Int>, List<String>>) {
        withContext(Dispatchers.IO) {
            var db: SQLiteDatabase? = null
            try {
                db = androidDatabase.writableDatabase
                db.beginTransaction()

                androidDatabase.bookNamesDao.save(translationInfo.shortName, bookNames)
                if (translationInfo.downloaded) {
                    androidDatabase.translationInfoDao.save(translationInfo)
                } else {
                    androidDatabase.translationInfoDao.save(TranslationInfo(
                            translationInfo.shortName, translationInfo.name,
                            translationInfo.language, translationInfo.size, true))
                }
                androidDatabase.translationDao.createTable(translationInfo.shortName)
                androidDatabase.translationDao.save(translationInfo.shortName, verses)

                db.setTransactionSuccessful()
            } finally {
                db?.endTransaction()
            }
        }
    }

    override suspend fun removeTranslation(translationInfo: TranslationInfo) {
        withContext(Dispatchers.IO) {
            var db: SQLiteDatabase? = null
            try {
                db = androidDatabase.writableDatabase
                db.beginTransaction()

                androidDatabase.bookNamesDao.remove(translationInfo.shortName)
                if (translationInfo.downloaded) {
                    androidDatabase.translationInfoDao.save(TranslationInfo(
                            translationInfo.shortName, translationInfo.name,
                            translationInfo.language, translationInfo.size, false))
                } else {
                    androidDatabase.translationInfoDao.save(translationInfo)
                }
                androidDatabase.translationDao.removeTable(translationInfo.shortName)

                db.setTransactionSuccessful()
            } finally {
                db?.endTransaction()
            }
        }
    }
}
