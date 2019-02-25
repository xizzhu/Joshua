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

package me.xizzhu.android.joshua.core.repository.retrofit

import androidx.annotation.WorkerThread
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.channels.SendChannel
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.BackendService
import me.xizzhu.android.joshua.core.repository.Translation
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.buffer
import okio.source
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import java.io.IOException
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class BackendServiceImpl(moshi: Moshi, okHttpClient: OkHttpClient) : BackendService {
    companion object {
        const val OKHTTP_TIMEOUT_IN_SECONDS = 30L
        private const val BASE_URL = "https://xizzhu.me/bible/download/"
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
    }
    private val translationService: TranslationService by lazy { retrofit.create(TranslationService::class.java) }
    private val booksAdapter: JsonAdapter<BackendBooks> by lazy { moshi.adapter(BackendBooks::class.java) }
    private val chapterAdapter: JsonAdapter<BackendChapter> by lazy { moshi.adapter(BackendChapter::class.java) }

    @WorkerThread
    override fun fetchTranslations(): List<TranslationInfo> {
        val response = translationService.fetchTranslationList().execute()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val backendTranslations = response.body()!!.translations
        val translations = ArrayList<TranslationInfo>(backendTranslations.size)
        for (backend in backendTranslations) {
            translations.add(TranslationInfo(
                    backend.shortName, backend.name, backend.language, backend.size, false))
        }
        return translations
    }

    @WorkerThread
    override suspend fun fetchTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo): Translation {
        var inputStream: ZipInputStream? = null
        val bookNames = ArrayList<String>()
        val verses = HashMap<Pair<Int, Int>, List<String>>()
        try {
            val response = translationService.fetchTranslation(translationInfo.shortName).execute()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            inputStream = ZipInputStream(response.body()!!.byteStream())
            var zipEntry: ZipEntry?
            var downloaded = 0
            var progress = -1
            while (true) {
                zipEntry = inputStream.nextEntry
                if (zipEntry == null) {
                    break
                }

                val bufferedSource = inputStream.source().buffer()
                val entryName = zipEntry.name
                if (entryName == "books.json") {
                    bookNames.addAll(booksAdapter.fromJson(bufferedSource)!!.books)
                } else {
                    val split = entryName.substring(0, entryName.length - 5).split("-")
                    verses[Pair(split[0].toInt(), split[1].toInt())] =
                            chapterAdapter.fromJson(bufferedSource)!!.verses
                }

                // only emits if the progress is actually changed
                val currentProgress = ++downloaded / 12
                if (currentProgress > progress) {
                    progress = currentProgress
                    channel.send(progress)
                }
            }
        } catch (e: Exception) {
            channel.close(e)
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: IOException) {
            }
        }

        val updatedTranslationInfo = TranslationInfo(translationInfo.shortName, translationInfo.name,
                translationInfo.language, translationInfo.size, true)
        return Translation(updatedTranslationInfo, bookNames, verses)
    }
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
