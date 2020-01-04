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

package me.xizzhu.android.joshua.core.repository.local.android.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class StrongNumberWordDaoTest : BaseSqliteTest() {
    fun testEmptyTable() {
        assertTrue(androidDatabase.strongNumberWordDao.read(MockContents.strongNumberWords.keys.toList()).isEmpty())
    }

    @Test
    fun testReplaceThenRead() {
        androidDatabase.strongNumberWordDao.replace(MockContents.strongNumberWords.keys.associateWith { "word" })
        androidDatabase.strongNumberWordDao.replace(MockContents.strongNumberWords)

        MockContents.strongNumberIndex.forEach { (verseIndex, list) ->
            assertEquals(MockContents.strongNumber[verseIndex], androidDatabase.strongNumberWordDao.read(list))
        }
    }

    @Test
    fun testReplaceThenReadWithNonExistingStrongNumber() {
        androidDatabase.strongNumberWordDao.replace(MockContents.strongNumberWords.keys.associateWith { "word" })
        androidDatabase.strongNumberWordDao.replace(MockContents.strongNumberWords)

        MockContents.strongNumberIndex.forEach { (verseIndex, list) ->
            val expected = MockContents.strongNumber[verseIndex]
            val actual = androidDatabase.strongNumberWordDao.read(
                    list.toMutableList().apply {
                        add("random")
                        add("things")
                        add("added")
                    }
            )
            assertEquals(expected, actual)
        }
    }
}
