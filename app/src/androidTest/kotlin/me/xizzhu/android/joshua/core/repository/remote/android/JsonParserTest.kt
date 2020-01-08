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

package me.xizzhu.android.joshua.core.repository.remote.android

import android.util.JsonReader
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test(expected = RuntimeException::class)
    fun testReadListJsonMissingField() {
        JsonReader(StringReader("{\n" +
                "\t\"random\": \"something random\"\n" +
                "}")).use { it.readListJson() }
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

    @Test
    fun testReadBooksJson() {
        JsonReader(StringReader("{\n" +
                "  \"name\": \"New International Version\",\n" +
                "  \"shortName\": \"NIV\",\n" +
                "  \"language\": \"en_us\",\n" +
                "  \"bookNames\": [\n" +
                "    \"Genesis\", \"Exodus\", \"Leviticus\", \"Numbers\", \"Deuteronomy\", \"Joshua\",\n" +
                "    \"Judges\", \"Ruth\", \"1 Samuel\", \"2 Samuel\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chronicles\", \"2 Chronicles\", \"Ezra\", \"Nehemiah\", \"Esther\", \"Job\",\n" +
                "    \"Psalms\", \"Proverbs\", \"Ecclesiastes\", \"Song of Solomon\", \"Isaiah\", \"Jeremiah\",\n" +
                "    \"Lamentations\", \"Ezekiel\", \"Daniel\", \"Hosea\", \"Joel\", \"Amos\",\n" +
                "    \"Obadiah\", \"Jonah\", \"Micah\", \"Nahum\", \"Habakkuk\", \"Zephaniah\",\n" +
                "    \"Haggai\", \"Zechariah\", \"Malachi\", \"St. Matthew\", \"St. Mark\", \"St. Luke\",\n" +
                "    \"St. John\", \"The Acts\", \"Romans\", \"1 Corinthians\", \"2 Corinthians\", \"Galatians\",\n" +
                "    \"Ephesians\", \"Philippians\", \"Colossians\", \"1 Thessalonians\", \"2 Thessalonians\", \"1 Timothy\",\n" +
                "    \"2 Timothy\", \"Titus\", \"Philemon\", \"Hebrews\", \"James\", \"1 Peter\",\n" +
                "    \"2 Peter\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Revelation\"\n" +
                "  ],\n" +
                "  \"bookShortNames\": [\n" +
                "    \"Gen.\", \"Ex.\", \"Lev.\", \"Num.\", \"Deut.\", \"Josh.\",\n" +
                "    \"Judg.\", \"Ruth\", \"1 Sam.\", \"2 Sam.\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chron.\", \"2 Chron.\", \"Ezra\", \"Neh.\", \"Est.\", \"Job\",\n" +
                "    \"Ps.\", \"Prov\", \"Eccles.\", \"Song\", \"Isa.\", \"Jer.\",\n" +
                "    \"Lam.\", \"Ezek.\", \"Dan.\", \"Hos.\", \"Joel\", \"Amos\",\n" +
                "    \"Obad.\", \"Jonah\", \"Mic.\", \"Nah.\", \"Hab.\", \"Zeph.\",\n" +
                "    \"Hag.\", \"Zech.\", \"Mal.\", \"Matt.\", \"Mark\", \"Luke\",\n" +
                "    \"John\", \"Acts\", \"Rom.\", \"1 Cor.\", \"2 Cor.\", \"Gal.\",\n" +
                "    \"Eph.\", \"Phil.\", \"Col.\", \"1 Thess.\", \"2 Thess.\", \"1 Tim.\",\n" +
                "    \"2 Tim.\", \"Titus\", \"Philem.\", \"Heb.\", \"James\", \"1 Pet.\",\n" +
                "    \"2 Pet.\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Rev\"\n" +
                "  ]\n" +
                "}")).use {
            assertEquals(
                    Pair(listOf(
                            "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua",
                            "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings",
                            "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job",
                            "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah",
                            "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
                            "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah",
                            "Haggai", "Zechariah", "Malachi", "St. Matthew", "St. Mark", "St. Luke",
                            "St. John", "The Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians",
                            "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy",
                            "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter",
                            "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"
                    ), listOf(
                            "Gen.", "Ex.", "Lev.", "Num.", "Deut.", "Josh.",
                            "Judg.", "Ruth", "1 Sam.", "2 Sam.", "1 Kings", "2 Kings",
                            "1 Chron.", "2 Chron.", "Ezra", "Neh.", "Est.", "Job",
                            "Ps.", "Prov", "Eccles.", "Song", "Isa.", "Jer.",
                            "Lam.", "Ezek.", "Dan.", "Hos.", "Joel", "Amos",
                            "Obad.", "Jonah", "Mic.", "Nah.", "Hab.", "Zeph.",
                            "Hag.", "Zech.", "Mal.", "Matt.", "Mark", "Luke",
                            "John", "Acts", "Rom.", "1 Cor.", "2 Cor.", "Gal.",
                            "Eph.", "Phil.", "Col.", "1 Thess.", "2 Thess.", "1 Tim.",
                            "2 Tim.", "Titus", "Philem.", "Heb.", "James", "1 Pet.",
                            "2 Pet.", "1 John", "2 John", "3 John", "Jude", "Rev"
                    )),
                    it.readBooksJson())
        }
    }

    @Test
    fun testReadBooksJsonWithExtraField() {
        JsonReader(StringReader("{\n" +
                "  \"name\": \"New International Version\",\n" +
                "  \"shortName\": \"NIV\",\n" +
                "  \"language\": \"en_us\",\n" +
                "  \"bookNames\": [\n" +
                "    \"Genesis\", \"Exodus\", \"Leviticus\", \"Numbers\", \"Deuteronomy\", \"Joshua\",\n" +
                "    \"Judges\", \"Ruth\", \"1 Samuel\", \"2 Samuel\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chronicles\", \"2 Chronicles\", \"Ezra\", \"Nehemiah\", \"Esther\", \"Job\",\n" +
                "    \"Psalms\", \"Proverbs\", \"Ecclesiastes\", \"Song of Solomon\", \"Isaiah\", \"Jeremiah\",\n" +
                "    \"Lamentations\", \"Ezekiel\", \"Daniel\", \"Hosea\", \"Joel\", \"Amos\",\n" +
                "    \"Obadiah\", \"Jonah\", \"Micah\", \"Nahum\", \"Habakkuk\", \"Zephaniah\",\n" +
                "    \"Haggai\", \"Zechariah\", \"Malachi\", \"St. Matthew\", \"St. Mark\", \"St. Luke\",\n" +
                "    \"St. John\", \"The Acts\", \"Romans\", \"1 Corinthians\", \"2 Corinthians\", \"Galatians\",\n" +
                "    \"Ephesians\", \"Philippians\", \"Colossians\", \"1 Thessalonians\", \"2 Thessalonians\", \"1 Timothy\",\n" +
                "    \"2 Timothy\", \"Titus\", \"Philemon\", \"Hebrews\", \"James\", \"1 Peter\",\n" +
                "    \"2 Peter\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Revelation\"\n" +
                "  ],\n" +
                "  \"bookShortNames\": [\n" +
                "    \"Gen.\", \"Ex.\", \"Lev.\", \"Num.\", \"Deut.\", \"Josh.\",\n" +
                "    \"Judg.\", \"Ruth\", \"1 Sam.\", \"2 Sam.\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chron.\", \"2 Chron.\", \"Ezra\", \"Neh.\", \"Est.\", \"Job\",\n" +
                "    \"Ps.\", \"Prov\", \"Eccles.\", \"Song\", \"Isa.\", \"Jer.\",\n" +
                "    \"Lam.\", \"Ezek.\", \"Dan.\", \"Hos.\", \"Joel\", \"Amos\",\n" +
                "    \"Obad.\", \"Jonah\", \"Mic.\", \"Nah.\", \"Hab.\", \"Zeph.\",\n" +
                "    \"Hag.\", \"Zech.\", \"Mal.\", \"Matt.\", \"Mark\", \"Luke\",\n" +
                "    \"John\", \"Acts\", \"Rom.\", \"1 Cor.\", \"2 Cor.\", \"Gal.\",\n" +
                "    \"Eph.\", \"Phil.\", \"Col.\", \"1 Thess.\", \"2 Thess.\", \"1 Tim.\",\n" +
                "    \"2 Tim.\", \"Titus\", \"Philem.\", \"Heb.\", \"James\", \"1 Pet.\",\n" +
                "    \"2 Pet.\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Rev\"\n" +
                "  ],\n" +
                "  \"extra\": \"something random\"\n" +
                "}")).use {
            assertEquals(
                    Pair(listOf(
                            "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua",
                            "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings",
                            "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job",
                            "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah",
                            "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
                            "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah",
                            "Haggai", "Zechariah", "Malachi", "St. Matthew", "St. Mark", "St. Luke",
                            "St. John", "The Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians",
                            "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy",
                            "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter",
                            "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"
                    ), listOf(
                            "Gen.", "Ex.", "Lev.", "Num.", "Deut.", "Josh.",
                            "Judg.", "Ruth", "1 Sam.", "2 Sam.", "1 Kings", "2 Kings",
                            "1 Chron.", "2 Chron.", "Ezra", "Neh.", "Est.", "Job",
                            "Ps.", "Prov", "Eccles.", "Song", "Isa.", "Jer.",
                            "Lam.", "Ezek.", "Dan.", "Hos.", "Joel", "Amos",
                            "Obad.", "Jonah", "Mic.", "Nah.", "Hab.", "Zeph.",
                            "Hag.", "Zech.", "Mal.", "Matt.", "Mark", "Luke",
                            "John", "Acts", "Rom.", "1 Cor.", "2 Cor.", "Gal.",
                            "Eph.", "Phil.", "Col.", "1 Thess.", "2 Thess.", "1 Tim.",
                            "2 Tim.", "Titus", "Philem.", "Heb.", "James", "1 Pet.",
                            "2 Pet.", "1 John", "2 John", "3 John", "Jude", "Rev"
                    )),
                    it.readBooksJson())
        }
    }

    @Test(expected = RuntimeException::class)
    fun testReadBooksJsonMissingBookNames() {
        JsonReader(StringReader("{\n" +
                "  \"name\": \"New International Version\",\n" +
                "  \"shortName\": \"NIV\",\n" +
                "  \"language\": \"en_us\",\n" +
                "  \"bookShortNames\": [\n" +
                "    \"Gen.\", \"Ex.\", \"Lev.\", \"Num.\", \"Deut.\", \"Josh.\",\n" +
                "    \"Judg.\", \"Ruth\", \"1 Sam.\", \"2 Sam.\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chron.\", \"2 Chron.\", \"Ezra\", \"Neh.\", \"Est.\", \"Job\",\n" +
                "    \"Ps.\", \"Prov\", \"Eccles.\", \"Song\", \"Isa.\", \"Jer.\",\n" +
                "    \"Lam.\", \"Ezek.\", \"Dan.\", \"Hos.\", \"Joel\", \"Amos\",\n" +
                "    \"Obad.\", \"Jonah\", \"Mic.\", \"Nah.\", \"Hab.\", \"Zeph.\",\n" +
                "    \"Hag.\", \"Zech.\", \"Mal.\", \"Matt.\", \"Mark\", \"Luke\",\n" +
                "    \"John\", \"Acts\", \"Rom.\", \"1 Cor.\", \"2 Cor.\", \"Gal.\",\n" +
                "    \"Eph.\", \"Phil.\", \"Col.\", \"1 Thess.\", \"2 Thess.\", \"1 Tim.\",\n" +
                "    \"2 Tim.\", \"Titus\", \"Philem.\", \"Heb.\", \"James\", \"1 Pet.\",\n" +
                "    \"2 Pet.\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Rev\"\n" +
                "  ]\n" +
                "}")).use { it.readBooksJson() }
    }

    @Test(expected = RuntimeException::class)
    fun testReadBooksJsonInvalidBookNames() {
        JsonReader(StringReader("{\n" +
                "  \"name\": \"New International Version\",\n" +
                "  \"shortName\": \"NIV\",\n" +
                "  \"language\": \"en_us\",\n" +
                "  \"bookNames\": [\n" +
                "    \"Genesis\", \"Exodus\", \"Leviticus\", \"Numbers\", \"Deuteronomy\", \"Joshua\",\n" +
                "    \"Judges\", \"Ruth\", \"1 Samuel\", \"2 Samuel\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chronicles\", \"2 Chronicles\", \"Ezra\", \"Nehemiah\", \"Esther\", \"Job\",\n" +
                "    \"Psalms\", \"Proverbs\", \"Ecclesiastes\", \"Song of Solomon\", \"Isaiah\", \"Jeremiah\",\n" +
                "    \"Lamentations\", \"Ezekiel\", \"Daniel\", \"Hosea\", \"Joel\", \"Amos\",\n" +
                "    \"Obadiah\", \"Jonah\", \"Micah\", \"Nahum\", \"Habakkuk\", \"Zephaniah\",\n" +
                "    \"Haggai\", \"Zechariah\", \"Malachi\", \"St. Matthew\", \"St. Mark\", \"St. Luke\",\n" +
                "    \"St. John\", \"The Acts\", \"Romans\", \"1 Corinthians\", \"2 Corinthians\", \"Galatians\",\n" +
                "    \"Ephesians\", \"Philippians\", \"Colossians\", \"1 Thessalonians\", \"2 Thessalonians\", \"1 Timothy\",\n" +
                "    \"2 Timothy\", \"Titus\", \"Philemon\", \"Hebrews\", \"James\", \"1 Peter\",\n" +
                "    \"2 Peter\", \"1 John\", \"2 John\", \"3 John\", \"Jude\"\n" +
                "  ],\n" +
                "  \"bookShortNames\": [\n" +
                "    \"Gen.\", \"Ex.\", \"Lev.\", \"Num.\", \"Deut.\", \"Josh.\",\n" +
                "    \"Judg.\", \"Ruth\", \"1 Sam.\", \"2 Sam.\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chron.\", \"2 Chron.\", \"Ezra\", \"Neh.\", \"Est.\", \"Job\",\n" +
                "    \"Ps.\", \"Prov\", \"Eccles.\", \"Song\", \"Isa.\", \"Jer.\",\n" +
                "    \"Lam.\", \"Ezek.\", \"Dan.\", \"Hos.\", \"Joel\", \"Amos\",\n" +
                "    \"Obad.\", \"Jonah\", \"Mic.\", \"Nah.\", \"Hab.\", \"Zeph.\",\n" +
                "    \"Hag.\", \"Zech.\", \"Mal.\", \"Matt.\", \"Mark\", \"Luke\",\n" +
                "    \"John\", \"Acts\", \"Rom.\", \"1 Cor.\", \"2 Cor.\", \"Gal.\",\n" +
                "    \"Eph.\", \"Phil.\", \"Col.\", \"1 Thess.\", \"2 Thess.\", \"1 Tim.\",\n" +
                "    \"2 Tim.\", \"Titus\", \"Philem.\", \"Heb.\", \"James\", \"1 Pet.\",\n" +
                "    \"2 Pet.\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Rev\"\n" +
                "  ]\n" +
                "}")).use { it.readBooksJson() }
    }

    @Test(expected = RuntimeException::class)
    fun testReadBooksJsonMissingBookShortNames() {
        JsonReader(StringReader("{\n" +
                "  \"name\": \"New International Version\",\n" +
                "  \"shortName\": \"NIV\",\n" +
                "  \"language\": \"en_us\",\n" +
                "  \"bookNames\": [\n" +
                "    \"Genesis\", \"Exodus\", \"Leviticus\", \"Numbers\", \"Deuteronomy\", \"Joshua\",\n" +
                "    \"Judges\", \"Ruth\", \"1 Samuel\", \"2 Samuel\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chronicles\", \"2 Chronicles\", \"Ezra\", \"Nehemiah\", \"Esther\", \"Job\",\n" +
                "    \"Psalms\", \"Proverbs\", \"Ecclesiastes\", \"Song of Solomon\", \"Isaiah\", \"Jeremiah\",\n" +
                "    \"Lamentations\", \"Ezekiel\", \"Daniel\", \"Hosea\", \"Joel\", \"Amos\",\n" +
                "    \"Obadiah\", \"Jonah\", \"Micah\", \"Nahum\", \"Habakkuk\", \"Zephaniah\",\n" +
                "    \"Haggai\", \"Zechariah\", \"Malachi\", \"St. Matthew\", \"St. Mark\", \"St. Luke\",\n" +
                "    \"St. John\", \"The Acts\", \"Romans\", \"1 Corinthians\", \"2 Corinthians\", \"Galatians\",\n" +
                "    \"Ephesians\", \"Philippians\", \"Colossians\", \"1 Thessalonians\", \"2 Thessalonians\", \"1 Timothy\",\n" +
                "    \"2 Timothy\", \"Titus\", \"Philemon\", \"Hebrews\", \"James\", \"1 Peter\",\n" +
                "    \"2 Peter\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Revelation\"\n" +
                "  ]\n" +
                "}")).use { it.readBooksJson() }
    }

    @Test(expected = RuntimeException::class)
    fun testReadBooksJsonInvalidBookShortNames() {
        JsonReader(StringReader("{\n" +
                "  \"name\": \"New International Version\",\n" +
                "  \"shortName\": \"NIV\",\n" +
                "  \"language\": \"en_us\",\n" +
                "  \"bookNames\": [\n" +
                "    \"Genesis\", \"Exodus\", \"Leviticus\", \"Numbers\", \"Deuteronomy\", \"Joshua\",\n" +
                "    \"Judges\", \"Ruth\", \"1 Samuel\", \"2 Samuel\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chronicles\", \"2 Chronicles\", \"Ezra\", \"Nehemiah\", \"Esther\", \"Job\",\n" +
                "    \"Psalms\", \"Proverbs\", \"Ecclesiastes\", \"Song of Solomon\", \"Isaiah\", \"Jeremiah\",\n" +
                "    \"Lamentations\", \"Ezekiel\", \"Daniel\", \"Hosea\", \"Joel\", \"Amos\",\n" +
                "    \"Obadiah\", \"Jonah\", \"Micah\", \"Nahum\", \"Habakkuk\", \"Zephaniah\",\n" +
                "    \"Haggai\", \"Zechariah\", \"Malachi\", \"St. Matthew\", \"St. Mark\", \"St. Luke\",\n" +
                "    \"St. John\", \"The Acts\", \"Romans\", \"1 Corinthians\", \"2 Corinthians\", \"Galatians\",\n" +
                "    \"Ephesians\", \"Philippians\", \"Colossians\", \"1 Thessalonians\", \"2 Thessalonians\", \"1 Timothy\",\n" +
                "    \"2 Timothy\", \"Titus\", \"Philemon\", \"Hebrews\", \"James\", \"1 Peter\",\n" +
                "    \"2 Peter\", \"1 John\", \"2 John\", \"3 John\", \"Jude\", \"Revelation\"\n" +
                "  ],\n" +
                "  \"bookShortNames\": [\n" +
                "    \"Gen.\", \"Ex.\", \"Lev.\", \"Num.\", \"Deut.\", \"Josh.\",\n" +
                "    \"Judg.\", \"Ruth\", \"1 Sam.\", \"2 Sam.\", \"1 Kings\", \"2 Kings\",\n" +
                "    \"1 Chron.\", \"2 Chron.\", \"Ezra\", \"Neh.\", \"Est.\", \"Job\",\n" +
                "    \"Ps.\", \"Prov\", \"Eccles.\", \"Song\", \"Isa.\", \"Jer.\",\n" +
                "    \"Lam.\", \"Ezek.\", \"Dan.\", \"Hos.\", \"Joel\", \"Amos\",\n" +
                "    \"Obad.\", \"Jonah\", \"Mic.\", \"Nah.\", \"Hab.\", \"Zeph.\",\n" +
                "    \"Hag.\", \"Zech.\", \"Mal.\", \"Matt.\", \"Mark\", \"Luke\",\n" +
                "    \"John\", \"Acts\", \"Rom.\", \"1 Cor.\", \"2 Cor.\", \"Gal.\",\n" +
                "    \"Eph.\", \"Phil.\", \"Col.\", \"1 Thess.\", \"2 Thess.\", \"1 Tim.\",\n" +
                "    \"2 Tim.\", \"Titus\", \"Philem.\", \"Heb.\", \"James\", \"1 Pet.\",\n" +
                "    \"2 Pet.\", \"1 John\", \"2 John\", \"3 John\", \"Jude\"\n" +
                "  ]\n" +
                "}")).use { it.readBooksJson() }
    }

    @Test
    fun testReadStringsArray() {
        JsonReader(StringReader("[\n" +
                "    \"Genesis\", \"Exodus\", \"Leviticus\", \"Numbers\", \"Deuteronomy\", \"Joshua\"" +
                "]")).use {
            assertEquals(listOf("Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua"),
                    it.readStringsArray())
        }
    }

    @Test
    fun testReadChapterJson() {
        JsonReader(StringReader("{\n" +
                "  \"verses\": [\n" +
                "    \"In the beginning God created the heaven and the earth.\",\n" +
                "    \"And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.\",\n" +
                "    \"And God said, Let there be light: and there was light.\"\n" +
                "  ]\n" +
                "}\n")).use {
            assertEquals(
                    listOf(
                            "In the beginning God created the heaven and the earth.",
                            "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
                            "And God said, Let there be light: and there was light."
                    ),
                    it.readChapterJson())
        }
    }

    @Test
    fun testReadChapterJsonWithExtraField() {
        JsonReader(StringReader("{\n" +
                "  \"extra\": \"random field\"," +
                "  \"verses\": [\n" +
                "    \"In the beginning God created the heaven and the earth.\",\n" +
                "    \"And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.\",\n" +
                "    \"And God said, Let there be light: and there was light.\"\n" +
                "  ]\n" +
                "}\n")).use {
            assertEquals(
                    listOf(
                            "In the beginning God created the heaven and the earth.",
                            "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
                            "And God said, Let there be light: and there was light."
                    ),
                    it.readChapterJson())
        }
    }

    @Test(expected = RuntimeException::class)
    fun testReadBooksJsonMissingVerses() {
        JsonReader(StringReader("{}")).use { it.readChapterJson() }
    }

    @Test(expected = RuntimeException::class)
    fun testReadBooksJsonEmptyVerses() {
        JsonReader(StringReader("{" +
                "  \"verses\": []\n" +
                "}")).use { it.readChapterJson() }
    }

    @Test
    fun testReadStrongNumberVerses() {
        JsonReader(StringReader("{\n" +
                "  \"1\": [7225, 1254, 430, 853, 8064, 853, 776],\n" +
                "  \"2\": [776, 1961, 8414, 922, 2822, 5921, 6440, 8415, 7307, 430, 7363, 5921, 6440, 4325]\n" +
                "}\n")).use {
            assertEquals(
                    mapOf(
                            1 to listOf(7225, 1254, 430, 853, 8064, 853, 776),
                            2 to listOf(776, 1961, 8414, 922, 2822, 5921, 6440, 8415, 7307, 430, 7363, 5921, 6440, 4325)
                    ),
                    it.readStrongNumberVerses())
        }
    }

    @Test
    fun testReadStrongNumberVersesWithNonIntField() {
        JsonReader(StringReader("{\n" +
                "  \"extra\": \"random field\"," +
                "  \"1\": [7225, 1254, 430, 853, 8064, 853, 776],\n" +
                "  \"2\": [776, 1961, 8414, 922, 2822, 5921, 6440, 8415, 7307, 430, 7363, 5921, 6440, 4325]\n" +
                "}\n")).use {
            assertEquals(
                    mapOf(
                            1 to listOf(7225, 1254, 430, 853, 8064, 853, 776),
                            2 to listOf(776, 1961, 8414, 922, 2822, 5921, 6440, 8415, 7307, 430, 7363, 5921, 6440, 4325)
                    ),
                    it.readStrongNumberVerses())
        }
    }

    @Test(expected = RuntimeException::class)
    fun testReadStrongNumberVersesMissingVerses() {
        JsonReader(StringReader("{}")).use { it.readStrongNumberVerses() }
    }

    @Test(expected = RuntimeException::class)
    fun testReadStrongNumberVersesEmptyVerses() {
        JsonReader(StringReader("{" +
                "  \"1\": []\n" +
                "}")).use { it.readStrongNumberVerses() }
    }

    @Test
    fun testReadIntsArray() {
        JsonReader(StringReader("[1989, 6, 4]")).use {
            assertEquals(listOf(1989, 6, 4), it.readIntsArray())
        }
    }

    @Test
    fun testReadStrongNumberWords() {
        JsonReader(StringReader("{\n" +
                "  \"1\": \"--Alpha\",\n" +
                "  \"2\": \"Aaron\",\n" +
                "  \"3\": \"Abaddon\",\n" +
                "  \"4\": \"from being burdensome\",\n" +
                "  \"5\": \"Abba\"" +
                "}\n")).use {
            assertEquals(
                    mapOf(
                            1 to "--Alpha",
                            2 to "Aaron",
                            3 to "Abaddon",
                            4 to "from being burdensome",
                            5 to "Abba"
                    ),
                    it.readStrongNumberWords())
        }
    }

    @Test
    fun testReadStrongNumberWordsWithNonIntField() {
        JsonReader(StringReader("{\n" +
                "  \"1\": \"--Alpha\",\n" +
                "  \"2\": \"Aaron\",\n" +
                "  \"extra\": \"random field\"," +
                "  \"3\": \"Abaddon\",\n" +
                "  \"4\": \"from being burdensome\",\n" +
                "  \"5\": \"Abba\"" +
                "}\n")).use {
            assertEquals(
                    mapOf(
                            1 to "--Alpha",
                            2 to "Aaron",
                            3 to "Abaddon",
                            4 to "from being burdensome",
                            5 to "Abba"
                    ),
                    it.readStrongNumberWords())
        }
    }

    @Test(expected = RuntimeException::class)
    fun testReadStrongNumberWordsMissingWords() {
        JsonReader(StringReader("{}")).use { it.readStrongNumberWords() }
    }
}
