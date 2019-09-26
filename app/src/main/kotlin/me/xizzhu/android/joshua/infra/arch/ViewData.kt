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

package me.xizzhu.android.joshua.infra.arch

import androidx.annotation.IntDef

data class ViewData<T> private constructor(@Status val status: Int, val data: T, val exception: Throwable?) {
    companion object {
        const val STATUS_SUCCESS = 0
        const val STATUS_ERROR = 1
        const val STATUS_LOADING = 2

        @IntDef(STATUS_SUCCESS, STATUS_ERROR, STATUS_LOADING)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Status

        fun <T> success(data: T): ViewData<T> = ViewData(STATUS_SUCCESS, data, null)
        fun <T> error(data: T, exception: Throwable? = null): ViewData<T> = ViewData(STATUS_ERROR, data, exception)
        fun <T> loading(data: T): ViewData<T> = ViewData(STATUS_LOADING, data, null)
    }

    fun toUnit(): ViewData<Unit> = ViewData(status, Unit, exception)
}
