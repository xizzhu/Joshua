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

package me.xizzhu.android.joshua.core.repository.local.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.toMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TranslationDaoTest : BaseSqliteTest() {
    @Test
    fun testReadNonExistTranslation() {
        assertTrue(androidDatabase.translationDao.read("not_exist", 0, 0).isEmpty())
    }

    @Test
    fun testSaveThenRead() {
        saveTranslation()
        assertEquals(MockContents.verses,
                androidDatabase.translationDao.read(MockContents.translationShortName, 0, 0))
    }

    private fun saveTranslation() {
        androidDatabase.translationDao.createTable(MockContents.translationShortName)
        androidDatabase.translationDao.save(MockContents.translationShortName, MockContents.verses.toMap())
    }

    @Test
    fun testSearchNonExistTranslation() {
        assertTrue(androidDatabase.translationDao.search("not_exist", "keyword").isEmpty())
    }

    @Test
    fun testSaveThenSearch() {
        saveTranslation()

        assertEquals(MockContents.verses,
                androidDatabase.translationDao.search(MockContents.translationShortName, "God"))
        assertEquals(MockContents.verses,
                androidDatabase.translationDao.search(MockContents.translationShortName, "god"))
        assertEquals(MockContents.verses,
                androidDatabase.translationDao.search(MockContents.translationShortName, "GOD"))
    }

    @Test
    fun testSaveThenSearchMultiKeywords() {
        saveTranslation()

        assertEquals(listOf(MockContents.verses[0]),
                androidDatabase.translationDao.search(MockContents.translationShortName, "God created"))
        assertEquals(listOf(MockContents.verses[0]),
                androidDatabase.translationDao.search(MockContents.translationShortName, "beginning created"))
    }
}
