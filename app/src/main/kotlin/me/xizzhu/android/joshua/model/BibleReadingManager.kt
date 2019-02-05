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

import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BibleReadingManager @Inject constructor(private val localStorage: LocalStorage) {
    var currentTranslation: String
        @WorkerThread get() = localStorage.metadataDao.load(MetadataDao.KEY_CURRENT_TRANSLATION, "")
        @WorkerThread set(value) {
            localStorage.metadataDao.save(MetadataDao.KEY_CURRENT_TRANSLATION, value)
        }
}
