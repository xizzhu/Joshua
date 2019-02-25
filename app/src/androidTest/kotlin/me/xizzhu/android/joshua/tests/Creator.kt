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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.squareup.moshi.Moshi
import me.xizzhu.android.joshua.core.repository.BackendService
import me.xizzhu.android.joshua.core.repository.LocalStorage
import me.xizzhu.android.joshua.core.repository.android.LocalStorageImpl
import me.xizzhu.android.joshua.core.repository.retrofit.BackendServiceImpl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

fun createLocalStorage(): LocalStorage = LocalStorageImpl(ApplicationProvider.getApplicationContext<Context>())

fun clearLocalStorage() {
    ApplicationProvider.getApplicationContext<Context>().deleteDatabase(LocalStorageImpl.DATABASE_NAME)
}

fun createBackendService(): BackendService = BackendServiceImpl(Moshi.Builder().build(),
        OkHttpClient.Builder()
                .connectTimeout(BackendServiceImpl.OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(BackendServiceImpl.OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(BackendServiceImpl.OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .build())
