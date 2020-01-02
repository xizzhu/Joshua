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
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class StrongNumberWordDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.strongNumberWordDao.read(MockContents.strongNumberWords.keys.toList()).isEmpty())
    }

    @Test
    fun testReplaceThenRead() {
        androidDatabase.strongNumberWordDao.replace(
                mapOf(
                        "H1" to "Value for H1",
                        "H2" to "Value for H2",
                        "G1" to "Value for G1",
                        "G2" to "Value for G2"
                )
        )
        androidDatabase.strongNumberWordDao.replace(MockContents.strongNumberWords)

        MockContents.strongNumberWords.keys.forEach { key ->
            assertEquals(
                    listOf(StrongNumber(key, MockContents.strongNumberWords.getValue(key))),
                    androidDatabase.strongNumberWordDao.read(listOf(key))
            )
        }
    }
}
