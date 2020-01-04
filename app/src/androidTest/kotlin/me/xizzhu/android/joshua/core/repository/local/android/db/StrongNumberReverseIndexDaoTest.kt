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
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class StrongNumberReverseIndexDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        (1..Constants.STRONG_NUMBER_HEBREW_COUNT).forEach { sn ->
            assertTrue(androidDatabase.strongNumberReverseIndexDao.read(sn.toString()).isEmpty())
        }
        (1..Constants.STRONG_NUMBER_GREEK_COUNT).forEach { sn ->
            assertTrue(androidDatabase.strongNumberReverseIndexDao.read(sn.toString()).isEmpty())
        }
    }

    @Test
    fun testReplaceThenRead() {
        androidDatabase.strongNumberReverseIndexDao.replace(MockContents.strongNumberReverseIndex.keys.associateWith { listOf(VerseIndex(1, 2, 3)) })
        androidDatabase.strongNumberReverseIndexDao.replace(MockContents.strongNumberReverseIndex)

        MockContents.strongNumberReverseIndex.forEach { (sn, verseIndexes) ->
            assertEquals(verseIndexes, androidDatabase.strongNumberReverseIndexDao.read(sn))
        }
    }
}
