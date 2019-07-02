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

package me.xizzhu.android.joshua.core.repository.remote.retrofit

import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException

class RetrofitTranslationServiceTest : BaseUnitTest() {
    @Test(expected = HttpException::class)
    fun testFetchTranslationsWithErrorCode() {
        runBlocking {
            assertTrue(createRetrofitTranslationService("list.json", 500, "").fetchTranslations().isEmpty())
        }
    }

    private fun createRetrofitTranslationService(expectedRelativePath: String, responseCode: Int, responseBody: String): RetrofitTranslationService =
            RetrofitTranslationService(Moshi.Builder().build(),
                    OkHttpClient.Builder().addInterceptor(MockInterceptor(expectedRelativePath, responseCode, responseBody.toByteArray())).build())

    @Test(expected = JsonEncodingException::class)
    fun testFetchTranslationsWithMalformedBody() {
        runBlocking {
            assertTrue(createRetrofitTranslationService("list.json", 200, "random").fetchTranslations().isEmpty())
        }
    }

    @Test
    fun testFetchZeroTranslation() {
        runBlocking {
            assertTrue(createRetrofitTranslationService("list.json", 200, "{\"translations\":[]}").fetchTranslations().isEmpty())
        }
    }

    @Test
    fun testFetchOneTranslation() {
        runBlocking {
            val expected = listOf(MockContents.kjvRemoteTranslationInfo)
            val actual = createRetrofitTranslationService("list.json", 200, MockContents.kjvTranslationInfoJson).fetchTranslations()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testFetchMultipleTranslations() {
        runBlocking {
            val expected = listOf(MockContents.kjvRemoteTranslationInfo, MockContents.cuvRemoteTranslationInfo)
            val actual = createRetrofitTranslationService("list.json", 200, MockContents.kjvCuvTranslationInfoJson).fetchTranslations()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testFetchTranslation() {
        runBlocking {
            val channel = Channel<Int>()
            launch {
                channel.consumeEach {
                    assertTrue(it in 0..100)
                }
            }
            val actual = createRetrofitTranslationService("KJV.zip", 200, readAllFromResources("KJV.zip"))
                    .fetchTranslation(channel, MockContents.kjvRemoteTranslationInfo)
            channel.close()

            assertEquals(MockContents.kjvRemoteTranslationInfo, actual.translationInfo)
            assertEquals(Bible.BOOK_COUNT, actual.bookNames.size)
            assertEquals(Bible.TOTAL_CHAPTER_COUNT, actual.verses.size)
        }
    }

    private fun readAllFromResources(name: String): ByteArray {
        val input = javaClass.classLoader!!.getResource(name).openStream()
        val results = ByteArray(input.available())
        input.read(results)
        input.close()

        return results
    }

    private fun createRetrofitTranslationService(expectedRelativePath: String, responseCode: Int, responseBody: ByteArray): RetrofitTranslationService =
            RetrofitTranslationService(Moshi.Builder().build(),
                    OkHttpClient.Builder().addInterceptor(MockInterceptor(expectedRelativePath, responseCode, responseBody)).build())
}

private class MockInterceptor(private val expectedRelativePath: String, private val responseCode: Int, private val responseBody: ByteArray) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        assertEquals(RetrofitTranslationService.BASE_URL + expectedRelativePath, chain.request().url.toString())
        return Response.Builder()
                .code(responseCode)
                .message("")
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .body(responseBody.toResponseBody("application/json".toMediaType()))
                .build()
    }
}
