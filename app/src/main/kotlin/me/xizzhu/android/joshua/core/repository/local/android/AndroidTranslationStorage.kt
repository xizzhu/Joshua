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
import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage

class AndroidTranslationStorage(private val androidDatabase: AndroidDatabase) : LocalTranslationStorage {
    @WorkerThread
    override fun readTranslations(): List<TranslationInfo> {
        return androidDatabase.translationInfoDao.read()
    }

    @WorkerThread
    override fun replaceTranslations(translations: List<TranslationInfo>) {
        androidDatabase.translationInfoDao.replace(translations)
    }

    @WorkerThread
    override fun saveTranslation(translationInfo: TranslationInfo, bookNames: List<String>,
                                 verses: Map<Pair<Int, Int>, List<String>>) {
        var db: SQLiteDatabase? = null
        try {
            db = androidDatabase.writableDatabase
            db.beginTransaction()

            androidDatabase.bookNamesDao.save(translationInfo.shortName, bookNames)
            androidDatabase.translationInfoDao.save(translationInfo)
            androidDatabase.translationDao.createTable(translationInfo.shortName)
            for (entry in verses) {
                androidDatabase.translationDao.save(translationInfo.shortName, entry.key.first, entry.key.second, entry.value)
            }

            db.setTransactionSuccessful()
        } finally {
            db?.endTransaction()
        }
    }
}
