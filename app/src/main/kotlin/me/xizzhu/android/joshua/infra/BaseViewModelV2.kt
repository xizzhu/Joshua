/*
 * Copyright (C) 2023 Xizhi Zhu
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

package me.xizzhu.android.joshua.infra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModelV2<ViewAction, ViewState>(initialViewState: ViewState) : ViewModel() {
    protected val tag: String = javaClass.simpleName

    private val viewAction: MutableSharedFlow<ViewAction> = MutableSharedFlow()
    private val viewState: MutableStateFlow<ViewState> = MutableStateFlow(initialViewState)

    fun viewAction(): Flow<ViewAction> = viewAction

    fun emitViewAction(action: ViewAction) {
        viewModelScope.launch { viewAction.emit(action) }
    }

    fun viewState(): Flow<ViewState> = viewState

    protected fun updateViewState(block: (currentViewState: ViewState) -> ViewState?) {
        viewState.update { block(it) ?: it }
    }
}
