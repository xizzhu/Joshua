/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.infra.arch

import androidx.annotation.IntDef
import kotlinx.coroutines.flow.*

data class ViewData<T> private constructor(@Status val status: Int, val data: T?, val exception: Throwable?) {
    companion object {
        const val STATUS_SUCCESS = 0
        const val STATUS_ERROR = 1
        const val STATUS_LOADING = 2

        @IntDef(STATUS_SUCCESS, STATUS_ERROR, STATUS_LOADING)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Status

        fun <T> success(data: T): ViewData<T> = ViewData(STATUS_SUCCESS, data, null)
        fun <T> error(data: T? = null, exception: Throwable? = null): ViewData<T> = ViewData(STATUS_ERROR, data, exception)
        fun <T> loading(data: T? = null): ViewData<T> = ViewData(STATUS_LOADING, data, null)
    }
}

inline fun <T> flowFrom(crossinline block: suspend () -> T): Flow<ViewData<T>> = flow {
    emit(ViewData.loading())
    try {
        emit(ViewData.success(block()))
    } catch (e: Exception) {
        emit(ViewData.error(exception = e))
    }
}

fun <T> Flow<T>.toViewData(): Flow<ViewData<T>> = map { ViewData.success(it) }.catch { ViewData.error<T>(exception = it) }

inline fun <T> Flow<ViewData<T>>.onEach(
        crossinline onLoading: suspend (value: T?) -> Unit,
        crossinline onSuccess: suspend (value: T) -> Unit,
        crossinline onError: suspend (value: T?, exception: Throwable?) -> Unit): Flow<ViewData<T>> = onEach { viewData ->
    when (viewData.status) {
        ViewData.STATUS_LOADING -> onLoading(viewData.data)
        ViewData.STATUS_SUCCESS -> onSuccess(viewData.data!!)
        ViewData.STATUS_ERROR -> onError(viewData.data, viewData.exception)
        else -> throw IllegalStateException("Unsupported status: ${viewData.status}")
    }
}

inline fun <T> Flow<ViewData<T>>.onEachSuccess(crossinline action: suspend (value: T) -> Unit): Flow<ViewData<T>> = onEach { viewData ->
    if (viewData.status == ViewData.STATUS_SUCCESS) action(viewData.data!!)
}

fun <T> Flow<ViewData<T>>.filterOnSuccess(): Flow<T> = transform { viewData ->
    if (viewData.status == ViewData.STATUS_SUCCESS) emit(viewData.data!!)
}

suspend inline fun <T> Flow<ViewData<T>>.firstSuccess(): T = first { it.status == ViewData.STATUS_SUCCESS }.data!!

fun <T1, T2, R> Flow<ViewData<T1>>.combineOnSuccess(flow: Flow<ViewData<T2>>, transform: suspend (a: T1, b: T2) -> R): Flow<R> =
        filterOnSuccess().combine(flow.filterOnSuccess()) { a, b -> transform(a, b) }
