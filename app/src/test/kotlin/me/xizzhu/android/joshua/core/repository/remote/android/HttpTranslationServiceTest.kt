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

package me.xizzhu.android.joshua.core.repository.remote.android

import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class HttpTranslationServiceTest : BaseUnitTest() {
    private lateinit var httpTranslationService: HttpTranslationService

    @BeforeTest
    override fun setup() {
        super.setup()

        httpTranslationService = HttpTranslationService(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test toRemoteTranslations()`() {
        val inputStream = ByteArrayInputStream("""
        {
        	"translations": [{
        		"name": "King James Version",
        		"shortName": "KJV",
        		"language": "en_gb",
        		"size": 1861133
        	}, {
        		"name": "中文和合本（简体）",
        		"shortName": "中文和合本",
        		"language": "zh_cn",
        		"size": 1781720
        	}]
        }
        """.trimIndent().toByteArray())
        assertEquals(
                listOf(
                        RemoteTranslationInfo("KJV", "King James Version", "en_gb", 1861133L),
                        RemoteTranslationInfo("中文和合本", "中文和合本（简体）", "zh_cn", 1781720L)
                ),
                httpTranslationService.toRemoteTranslations(inputStream)
        )
    }

    @Test
    fun `test toRemoteTranslation()`() {
        val actual = httpTranslationService.toRemoteTranslation(
                MockContents.kjvRemoteTranslationInfo, javaClass.classLoader.getResourceAsStream("KJV.zip")
        )
        assertEquals(MockContents.kjvRemoteTranslationInfo, actual.translationInfo)
        assertEquals(Bible.BOOK_COUNT, actual.bookNames.size)
        assertEquals(Bible.TOTAL_CHAPTER_COUNT, actual.verses.size)
    }
}
