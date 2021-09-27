/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.core.repository.remote

import kotlinx.coroutines.channels.SendChannel
import me.xizzhu.android.joshua.core.TranslationInfo

data class RemoteTranslationInfo(val shortName: String, val name: String, val language: String, val size: Long) {
    companion object {
        fun fromTranslationInfo(translationInfo: TranslationInfo): RemoteTranslationInfo =
                RemoteTranslationInfo(translationInfo.shortName, translationInfo.name, translationInfo.language, translationInfo.size)
    }

    fun toTranslationInfo(downloaded: Boolean): TranslationInfo =
            TranslationInfo(shortName, name, language, size, downloaded)
}

data class RemoteTranslation(
        val translationInfo: RemoteTranslationInfo,
        val bookNames: List<String>,
        val bookShortNames: List<String>,
        val verses: Map<Pair<Int, Int>, List<String>>
)

interface RemoteTranslationService {
    suspend fun fetchTranslations(): List<RemoteTranslationInfo>

    suspend fun fetchTranslation(channel: SendChannel<Int>, translationInfo: RemoteTranslationInfo): RemoteTranslation
}
