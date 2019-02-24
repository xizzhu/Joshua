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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BackendService {
    companion object {
        private const val BASE_URL = "https://xizzhu.me/bible/download/"
        private const val OKHTTP_TIMEOUT_IN_SECONDS = 30L
    }

    private val moshi = Moshi.Builder().build()
    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder()
                    .connectTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    val translationService: TranslationService by lazy { retrofit.create(TranslationService::class.java) }

    val booksAdapter: JsonAdapter<BackendBooks> by lazy { moshi.adapter(BackendBooks::class.java) }
    val chapterAdapter: JsonAdapter<BackendChapter> by lazy { moshi.adapter(BackendChapter::class.java) }
}

suspend fun <T> Call<T>.await(): T = suspendCoroutine { cont ->
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                cont.resume(response.body()!!)
            } else {
                cont.resumeWithException(HttpException(response))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            cont.resumeWithException(t)
        }
    })
}

data class BackendTranslationInfo(@Json(name = "shortName") val shortName: String,
                                  @Json(name = "name") val name: String,
                                  @Json(name = "language") val language: String,
                                  @Json(name = "size") val size: Long)

data class BackendTranslationList(@Json(name = "translations") val translations: List<BackendTranslationInfo>)

interface TranslationService {
    @GET("list.json")
    fun fetchTranslationList(): Call<BackendTranslationList>

    @GET("{translationShortName}.zip")
    @Streaming
    fun fetchTranslation(@Path("translationShortName") translationShortName: String): Call<ResponseBody>
}

data class BackendBooks(@Json(name = "shortName") val shortName: String,
                        @Json(name = "name") val name: String,
                        @Json(name = "language") val language: String,
                        @Json(name = "books") val books: List<String>)

data class BackendChapter(@Json(name = "verses") val verses: List<String>)
