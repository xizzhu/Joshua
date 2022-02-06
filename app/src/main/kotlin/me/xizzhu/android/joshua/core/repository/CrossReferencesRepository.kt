/*
 * Copyright (C) 2022 Xizhi Zhu
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.CrossReferences
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalCrossReferencesStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteCrossReferencesStorage
import me.xizzhu.android.logger.Log

class CrossReferencesRepository(private val localCrossReferencesStorage: LocalCrossReferencesStorage,
                                private val remoteCrossReferencesStorage: RemoteCrossReferencesStorage) {
    companion object {
        private val TAG: String = CrossReferencesRepository::class.java.simpleName
    }

    suspend fun readCrossReferences(verseIndex: VerseIndex): CrossReferences = localCrossReferencesStorage.readCrossReferences(verseIndex)

    fun download(): Flow<Int> = channelFlow {
        val downloadProgressChannel = Channel<Int>(Channel.CONFLATED)
        launch { downloadProgressChannel.consumeEach { trySend(it) } }

        Log.i(TAG, "Start downloading cross references")
        val downloaded = remoteCrossReferencesStorage.fetchCrossReferences(downloadProgressChannel)
        Log.i(TAG, "Cross references downloaded")

        localCrossReferencesStorage.save(downloaded.references)
        remoteCrossReferencesStorage.removeCrossReferencesCache()
        Log.i(TAG, "Cross references saved to database")
        downloadProgressChannel.trySend(100)
        downloadProgressChannel.close()
    }
}
