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

import kotlinx.coroutines.channels.SendChannel
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslation
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService

class MockRemoteTranslationService : RemoteTranslationService {
    override suspend fun fetchTranslations(): List<RemoteTranslationInfo> {
        return listOf(RemoteTranslationInfo.fromTranslationInfo(MockContents.translationInfo))
    }

    override suspend fun fetchTranslation(channel: SendChannel<Int>, translationInfo: RemoteTranslationInfo): RemoteTranslation {
        return RemoteTranslation(RemoteTranslationInfo.fromTranslationInfo(MockContents.translationInfo),
                MockContents.bookNames, MockContents.verses.toMap())
    }
}
