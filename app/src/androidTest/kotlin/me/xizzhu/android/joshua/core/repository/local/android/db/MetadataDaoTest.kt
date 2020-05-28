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

package me.xizzhu.android.joshua.core.repository.local.android.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class MetadataDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        val value = "random-default-value"
        assertEquals(value, androidDatabase.metadataDao.read("not-exist", value))
    }

    @Test
    fun testEmptyTableMultiKeys() {
        val values = listOf("default-value", "")
        val actual = androidDatabase.metadataDao.read(listOf(Pair("not-exist", values[0]), Pair("still-not-exist", values[1])))
        assertEquals(2, actual.size)
        assertEquals(values[0], actual["not-exist"])
        assertEquals(values[1], actual["still-not-exist"])
    }

    @Test
    fun testSaveThenRead() {
        val key = "key"
        val value = "value"
        androidDatabase.metadataDao.save(key, value)
        assertEquals(value, androidDatabase.metadataDao.read(key, ""))
        assertEquals("default-value", androidDatabase.metadataDao.read("not-exist", "default-value"))
    }

    @Test
    fun testSaveOverrideThenRead() {
        val key = "key"
        val value = "value"
        androidDatabase.metadataDao.save(key, "random-value")
        androidDatabase.metadataDao.save(key, value)
        assertEquals(value, androidDatabase.metadataDao.read(key, ""))
        assertEquals("default-value", androidDatabase.metadataDao.read("not-exist", "default-value"))
    }

    @Test
    fun testSaveThenMultiReadEmpty() {
        androidDatabase.metadataDao.save("key", "value")
        assertTrue(androidDatabase.metadataDao.read(emptyList()).isEmpty())
    }

    @Test
    fun testSaveThenMultiReadWithPartialDefaultValues() {
        androidDatabase.metadataDao.save("key", "value")

        val expected = mapOf(Pair("key", "value"), Pair("key1", "default1"), Pair("key2", "default2"))
        val actual = androidDatabase.metadataDao.read(listOf(
                Pair("key", ""), Pair("key1", "default1"), Pair("key2", "default2")))
        assertEquals(expected, actual)
    }

    @Test
    fun testMultiSaveThenRead() {
        val entries = listOf(Pair("key1", "value1"), Pair("key2", "value2"))
        androidDatabase.metadataDao.save(entries)

        for (entry in entries) {
            assertEquals(entry.second, androidDatabase.metadataDao.read(entry.first, ""))
        }

        val keys = ArrayList<Pair<String, String>>()
        for (entry in entries) {
            keys.add(Pair(entry.first, ""))
        }
        val actual = androidDatabase.metadataDao.read(keys)
        assertEquals(entries.size, actual.size)
        entries.forEachIndexed { index, (_, value) ->
            assertEquals(value, actual[keys[index].first])
        }
    }

    @Test
    fun testMultiSaveOverrideThenRead() {
        val entries = listOf(Pair("key1", "value1"), Pair("key2", "value2"))
        androidDatabase.metadataDao.save(listOf(Pair("key1", "original-random-value")))
        androidDatabase.metadataDao.save(listOf(Pair("key1", "another-random-value"), Pair("key2", "yet-another-random-value")))
        androidDatabase.metadataDao.save(entries)

        for (entry in entries) {
            assertEquals(entry.second, androidDatabase.metadataDao.read(entry.first, ""))
        }

        val keys = ArrayList<Pair<String, String>>()
        for (entry in entries) {
            keys.add(Pair(entry.first, ""))
        }
        val actual = androidDatabase.metadataDao.read(keys)
        assertEquals(entries.size, actual.size)
        entries.forEachIndexed { index, (_, value) ->
            assertEquals(value, actual[keys[index].first])
        }
    }
}
