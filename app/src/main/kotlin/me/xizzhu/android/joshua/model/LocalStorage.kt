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

package me.xizzhu.android.joshua.model

import androidx.room.*
import io.reactivex.Single

@Database(entities = [(LocalTranslationInfo::class)], version = LocalStorage.DATABASE_VERSION)
abstract class LocalStorage : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "DATABASE_JOSHUA.db"
        const val DATABASE_VERSION = 1

        const val TABLE_TRANSLATION_INFO = "translation_info"
        const val COLUMN_SHORT_NAME = "shortName"
        const val COLUMN_NAME = "name"
        const val COLUMN_LANGUAGE = "language"
        const val COLUMN_SIZE = "size"
    }

    abstract fun localTranslationInfoDao(): LocalTranslationInfoDao
}

@Entity(tableName = LocalStorage.TABLE_TRANSLATION_INFO)
data class LocalTranslationInfo(
        @PrimaryKey @ColumnInfo(name = LocalStorage.COLUMN_SHORT_NAME) val shortName: String,
        @ColumnInfo(name = LocalStorage.COLUMN_NAME) val name: String,
        @ColumnInfo(name = LocalStorage.COLUMN_LANGUAGE) val language: String,
        @ColumnInfo(name = LocalStorage.COLUMN_SIZE) val size: Long) {
    companion object {
        fun fromTranslationInfo(translationInfo: TranslationInfo) =
                LocalTranslationInfo(translationInfo.shortName, translationInfo.name,
                        translationInfo.language, translationInfo.size)
    }

    fun toTranslationInfo() = TranslationInfo(shortName, name, language, size)
}

@Dao
interface LocalTranslationInfoDao {
    @Query("SELECT * FROM " + LocalStorage.TABLE_TRANSLATION_INFO)
    fun load(): Single<List<LocalTranslationInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(translations: List<LocalTranslationInfo>)
}
