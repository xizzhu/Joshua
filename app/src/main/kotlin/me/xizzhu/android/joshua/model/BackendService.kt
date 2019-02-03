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

package me.xizzhu.android.joshua.model

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import io.reactivex.Single
import me.xizzhu.android.joshua.BASE_URL
import me.xizzhu.android.joshua.OKHTTP_TIMEOUT_IN_SECONDS
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendService @Inject constructor() {
    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder()
                    .connectTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .build())
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private var translationService: TranslationService? = null

    fun translationService(): TranslationService {
        if (translationService == null) {
            translationService = retrofit.create(TranslationService::class.java)
        }
        return translationService!!
    }
}

data class BackendTranslationInfo(@Json(name = "shortName") val shortName: String,
                                  @Json(name = "name") val name: String,
                                  @Json(name = "language") val language: String,
                                  @Json(name = "size") val size: Long) {
    fun toTranslationInfo() = TranslationInfo(shortName, name, language, size)
}

data class BackendTranslationList(@Json(name = "translations") val translations: List<BackendTranslationInfo>)

interface TranslationService {
    @GET("list.json")
    fun fetchTranslationList(): Single<BackendTranslationList>
}
