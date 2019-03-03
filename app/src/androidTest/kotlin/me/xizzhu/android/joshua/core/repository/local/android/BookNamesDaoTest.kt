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
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class BookNamesDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.bookNamesDao.read("not_exist").isEmpty())
    }

    @Test
    fun testSaveThenRead() {
        androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames)
        assertEquals(MockContents.kjvBookNames, androidDatabase.bookNamesDao.read(MockContents.kjvShortName))
    }

    @Test
    fun testSaveOverrideThenReadBookNames() {
        runBlocking {
            androidDatabase.bookNamesDao.save(MockContents.kjvShortName, listOf("random_1", "whatever_2"))
            androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames)
            assertEquals(MockContents.kjvBookNames, androidDatabase.bookNamesDao.read(MockContents.kjvShortName))
        }
    }
}
