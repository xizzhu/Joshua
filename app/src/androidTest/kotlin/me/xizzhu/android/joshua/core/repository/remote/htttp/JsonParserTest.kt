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

package me.xizzhu.android.joshua.core.repository.remote.htttp

import android.util.JsonReader
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.http.readListJson
import me.xizzhu.android.joshua.core.repository.remote.http.readTranslation
import me.xizzhu.android.joshua.core.repository.remote.http.readTranslationsArray
import me.xizzhu.android.joshua.tests.BaseUnitTest
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JsonParserTest : BaseUnitTest() {
    @Test
    fun testReadListJson() {
        JsonReader(StringReader("{\n" +
                "\t\"translations\": [{\n" +
                "\t\t\"name\": \"Authorized King James\",\n" +
                "\t\t\"shortName\": \"KJV\",\n" +
                "\t\t\"language\": \"en_gb\",\n" +
                "\t\t\"size\": 1861134\n" +
                "\t}, {\n" +
                "\t\t\"name\": \"中文和合本（简体）\",\n" +
                "\t\t\"shortName\": \"中文和合本\",\n" +
                "\t\t\"language\": \"zh_cn\",\n" +
                "\t\t\"size\": 1781720\n" +
                "\t}]\n" +
                "}")).use {
            assertEquals(
                    listOf(
                            RemoteTranslationInfo("KJV", "Authorized King James", "en_gb", 1861134L),
                            RemoteTranslationInfo("中文和合本", "中文和合本（简体）", "zh_cn", 1781720L)
                    ), it.readListJson())
        }
    }

    @Test
    fun testReadListJsonWithExtraField() {
        JsonReader(StringReader("{\n" +
                "\t\"random\": \"something random\",\n" +
                "\t\"translations\": [{\n" +
                "\t\t\"name\": \"Authorized King James\",\n" +
                "\t\t\"shortName\": \"KJV\",\n" +
                "\t\t\"language\": \"en_gb\",\n" +
                "\t\t\"size\": 1861134\n" +
                "\t}, {\n" +
                "\t\t\"name\": \"中文和合本（简体）\",\n" +
                "\t\t\"shortName\": \"中文和合本\",\n" +
                "\t\t\"language\": \"zh_cn\",\n" +
                "\t\t\"size\": 1781720\n" +
                "\t}]" +
                "}")).use {
            assertEquals(
                    listOf(
                            RemoteTranslationInfo("KJV", "Authorized King James", "en_gb", 1861134L),
                            RemoteTranslationInfo("中文和合本", "中文和合本（简体）", "zh_cn", 1781720L)
                    ), it.readListJson())
        }
    }

    @Test
    fun testReadListJsonMissingField() {
        JsonReader(StringReader("{\n" +
                "\t\"random\": \"something random\"\n" +
                "}")).use {
            assertTrue(it.readListJson().isEmpty())
        }
    }

    @Test
    fun testReadTranslationsArray() {
        JsonReader(StringReader("[{\n" +
                "\t\t\"name\": \"Authorized King James\",\n" +
                "\t\t\"shortName\": \"KJV\",\n" +
                "\t\t\"language\": \"en_gb\",\n" +
                "\t\t\"size\": 1861134\n" +
                "\t}, {\n" +
                "\t\t\"name\": \"中文和合本（简体）\",\n" +
                "\t\t\"shortName\": \"中文和合本\",\n" +
                "\t\t\"language\": \"zh_cn\",\n" +
                "\t\t\"size\": 1781720\n" +
                "\t}]")).use {
            assertEquals(
                    listOf(
                            RemoteTranslationInfo("KJV", "Authorized King James", "en_gb", 1861134L),
                            RemoteTranslationInfo("中文和合本", "中文和合本（简体）", "zh_cn", 1781720L)
                    ), it.readTranslationsArray())
        }
    }

    @Test
    fun testReadTranslationsArrayWithTranslationMissingField() {
        JsonReader(StringReader("[{\n" +
                "\t\t\"name\": \"Authorized King James\",\n" +
                "\t\t\"shortName\": \"KJV\",\n" +
                "\t\t\"language\": \"en_gb\",\n" +
                "\t\t\"size\": 1861134\n" +
                "\t}, {\n" +
                "\t\t\"name\": \"中文和合本（简体）\",\n" +
                "\t\t\"language\": \"zh_cn\",\n" +
                "\t\t\"size\": 1781720\n" +
                "\t}]")).use {
            assertEquals(listOf(RemoteTranslationInfo("KJV", "Authorized King James", "en_gb", 1861134L)),
                    it.readTranslationsArray())
        }
    }

    @Test
    fun testReadTranslation() {
        JsonReader(StringReader("{\n" +
                "\t\t\"name\": \"Authorized King James\",\n" +
                "\t\t\"shortName\": \"KJV\",\n" +
                "\t\t\"language\": \"en_gb\",\n" +
                "\t\t\"size\": 1861134\n" +
                "\t}")).use {
            assertEquals(RemoteTranslationInfo("KJV", "Authorized King James", "en_gb", 1861134L),
                    it.readTranslation())
        }
    }

    @Test
    fun testReadTranslationWithExtraField() {
        JsonReader(StringReader("{\n" +
                "\t\t\"name\": \"Authorized King James\",\n" +
                "\t\t\"shortName\": \"KJV\",\n" +
                "\t\t\"language\": \"en_gb\",\n" +
                "\t\t\"size\": 1861134,\n" +
                "\t\t\"extra\": \"something random\"\n" +
                "\t}")).use {
            assertEquals(RemoteTranslationInfo("KJV", "Authorized King James", "en_gb", 1861134L),
                    it.readTranslation())
        }
    }

    @Test
    fun testReadTranslationMissingField() {
        JsonReader(StringReader("{\n" +
                "\t\t\"name\": \"Authorized King James\",\n" +
                "\t\t\"shortName\": \"KJV\",\n" +
                "\t\t\"language\": \"en_gb\"\n" +
                "\t}")).use {
            assertNull(it.readTranslation())
        }
    }
}
