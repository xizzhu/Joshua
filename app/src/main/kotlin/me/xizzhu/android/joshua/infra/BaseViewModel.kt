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

package me.xizzhu.android.joshua.infra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager

abstract class BaseViewModel<BA : BaseActivity<*>>(
        protected val settingsManager: SettingsManager, protected val activity: BA, protected val coroutineScope: CoroutineScope
) {
    sealed class ViewData<T> {
        data class Loading<T>(val data: T? = null) : ViewData<T>()
        data class Success<T>(val data: T) : ViewData<T>()
        data class Failure<T>(val throwable: Throwable) : ViewData<T>()
    }

    protected val tag: String = javaClass.simpleName

    fun settings(): Flow<Settings> = settingsManager.settings()
}

inline fun <R> act(crossinline op: suspend () -> R): Flow<BaseViewModel.ViewData<R>> = flow {
    try {
        emit(BaseViewModel.ViewData.Loading())
        emit(BaseViewModel.ViewData.Success(op()))
    } catch (e: Exception) {
        emit(BaseViewModel.ViewData.Failure(e))
    }
}

inline fun <D> Flow<BaseViewModel.ViewData<D>>.onSuccess(crossinline onSuccess: (D) -> Unit): Flow<BaseViewModel.ViewData<D>> = transform { value ->
    if (value is BaseViewModel.ViewData.Success) onSuccess(value.data)
    return@transform emit(value)
}

inline fun <D> Flow<BaseViewModel.ViewData<D>>.onFailure(crossinline onFailure: (Throwable) -> Unit): Flow<BaseViewModel.ViewData<D>> = transform { value ->
    if (value is BaseViewModel.ViewData.Failure) onFailure(value.throwable)
    return@transform emit(value)
}

inline fun <D> Flow<BaseViewModel.ViewData<D>>.onEach(
        crossinline onLoading: (D?) -> Unit, crossinline onSuccess: (D) -> Unit, crossinline onFailure: (Throwable) -> Unit
): Flow<BaseViewModel.ViewData<D>> = transform { value ->
    when (value) {
        is BaseViewModel.ViewData.Loading<D> -> onLoading(value.data)
        is BaseViewModel.ViewData.Success -> onSuccess(value.data)
        is BaseViewModel.ViewData.Failure -> onFailure(value.throwable)
    }
    return@transform emit(value)
}
