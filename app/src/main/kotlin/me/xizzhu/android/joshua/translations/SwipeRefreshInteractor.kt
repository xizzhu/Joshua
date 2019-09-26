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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewData

class SwipeRefreshInteractor(dispatcher: CoroutineDispatcher = Dispatchers.Default) : Interactor(dispatcher) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val loadingState: BroadcastChannel<ViewData<Unit>> = ConflatedBroadcastChannel()
    private val refreshRequest: BroadcastChannel<ViewData<Unit>> = ConflatedBroadcastChannel()

    fun loadingState(): Flow<ViewData<Unit>> = loadingState.asFlow()

    fun updateLoadingState(state: ViewData<Unit>) {
        loadingState.offer(state)
    }

    fun refreshRequested(): Flow<ViewData<Unit>> = refreshRequest.asFlow()

    fun requestRefresh() {
        refreshRequest.offer(ViewData.success(Unit))
    }
}
