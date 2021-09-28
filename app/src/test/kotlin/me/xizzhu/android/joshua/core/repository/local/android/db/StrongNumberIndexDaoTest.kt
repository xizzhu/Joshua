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

package me.xizzhu.android.joshua.core.repository.local.android.db

import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class StrongNumberIndexDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        (0 until Bible.BOOK_COUNT).forEach { bookIndex ->
            (0 until Bible.getChapterCount(bookIndex)).forEach { chapterIndex ->
                assertTrue(androidDatabase.strongNumberIndexDao.read(VerseIndex(bookIndex, chapterIndex, 0)).isEmpty())
            }
        }
    }

    @Test
    fun testReplaceThenRead() {
        androidDatabase.strongNumberIndexDao.replace(MockContents.strongNumberIndex.keys.associateWith { listOf("1", "2", "3") })
        androidDatabase.strongNumberIndexDao.replace(MockContents.strongNumberIndex)

        MockContents.strongNumberIndex.forEach { (verseIndex, list) ->
            assertEquals(list, androidDatabase.strongNumberIndexDao.read(verseIndex))
        }
    }
}
