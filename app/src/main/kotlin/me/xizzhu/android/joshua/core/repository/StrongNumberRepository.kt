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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberStorage

class StrongNumberRepository(private val localStrongNumberStorage: LocalStrongNumberStorage,
                             private val remoteStrongNumberStorage: RemoteStrongNumberStorage) {
    suspend fun read(verseIndex: VerseIndex): List<StrongNumber> = localStrongNumberStorage.read(verseIndex)

    fun download(): Flow<Int> = channelFlow {
        val downloadProgressChannel = Channel<Int>(Channel.CONFLATED)
        launch { downloadProgressChannel.consumeEach { offer(it) } }

        val remoteVerses = remoteStrongNumberStorage.fetchVerses(downloadProgressChannel)
        downloadProgressChannel.send(100)

        // TODO saves to local storage

        downloadProgressChannel.send(101)
        downloadProgressChannel.close()
    }
}
