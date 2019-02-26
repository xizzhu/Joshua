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
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.AndroidReadingStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.core.repository.remote.retrofit.RetrofitTranslationService
import okhttp3.OkHttpClient

private fun createAndroidDatabase(): AndroidDatabase =
        AndroidDatabase(ApplicationProvider.getApplicationContext<Context>())

fun createLocalReadingStorage(): LocalReadingStorage = AndroidReadingStorage(createAndroidDatabase())

fun createLocalTranslationStorage(): LocalTranslationStorage = AndroidTranslationStorage(createAndroidDatabase())

fun clearLocalStorage() {
    ApplicationProvider.getApplicationContext<Context>().deleteDatabase(AndroidDatabase.DATABASE_NAME)
}

fun createRemoteTranslationService(): RemoteTranslationService =
        RetrofitTranslationService(Moshi.Builder().build(), OkHttpClient.Builder().build())
