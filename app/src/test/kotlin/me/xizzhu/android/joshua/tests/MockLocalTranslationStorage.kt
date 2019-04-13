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

package me.xizzhu.android.joshua.tests

import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import java.util.*

class MockLocalTranslationStorage : LocalTranslationStorage {
    private val translations: ArrayList<TranslationInfo> = ArrayList()
    private var translationListRefreshTimestamp: Long = 0L

    override suspend fun readTranslationListRefreshTimestamp(): Long = translationListRefreshTimestamp

    override suspend fun saveTranslationListRefreshTimestamp(timestamp: Long) {
        translationListRefreshTimestamp = timestamp
    }

    override suspend fun readTranslations(): List<TranslationInfo> {
        return Collections.unmodifiableList(translations)
    }

    override suspend fun replaceTranslations(translations: List<TranslationInfo>) {
        this.translations.clear()
        this.translations.addAll(translations)
    }

    override suspend fun saveTranslation(translationInfo: TranslationInfo, bookNames: List<String>,
                                         bookShortNames: List<String>, verses: Map<Pair<Int, Int>, List<String>>) {
        for (i in 0 until translations.size) {
            if (translations[i].shortName == translationInfo.shortName) {
                translations[i] = translationInfo
                return
            }
        }
        translations.add(translationInfo)
    }

    override suspend fun removeTranslation(translationInfo: TranslationInfo) {
        translations.remove(translationInfo)
    }
}
