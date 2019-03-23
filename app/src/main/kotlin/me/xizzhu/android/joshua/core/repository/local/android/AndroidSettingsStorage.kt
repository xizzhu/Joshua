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
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.repository.local.LocalSettingsStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.db.MetadataDao

class AndroidSettingsStorage(private val androidDatabase: AndroidDatabase) : LocalSettingsStorage {
    override suspend fun readSettings(): Settings {
        return withContext(Dispatchers.IO) {
            val values = androidDatabase.metadataDao.read(listOf(
                    Pair(MetadataDao.KEY_SCREEN_ON, Settings.DEFAULT.keepScreenOn.toString())
            ))
            return@withContext Settings(values.getValue(MetadataDao.KEY_SCREEN_ON).toBoolean())
        }
    }

    override suspend fun saveSettings(settings: Settings) {
        withContext(Dispatchers.IO) {
            androidDatabase.metadataDao.save(listOf(
                    Pair(MetadataDao.KEY_SCREEN_ON, settings.keepScreenOn.toString())
            ))
        }
    }
}
