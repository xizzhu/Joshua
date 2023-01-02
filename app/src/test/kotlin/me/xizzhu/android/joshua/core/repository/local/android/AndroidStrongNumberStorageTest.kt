/*
 * Copyright (C) 2023 Xizhi Zhu
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

import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
class AndroidStrongNumberStorageTest : BaseSqliteTest() {
    private lateinit var androidStrongNumberStorage: AndroidStrongNumberStorage

    @BeforeTest
    override fun setup() {
        super.setup()
        androidStrongNumberStorage = AndroidStrongNumberStorage(androidDatabase)
    }

    @Test
    fun testEmptyStrongNumber() = runTest {
        (0 until Bible.BOOK_COUNT).forEach { bookIndex ->
            (0 until Bible.getChapterCount(bookIndex)).forEach { chapterIndex ->
                assertTrue(androidStrongNumberStorage.readStrongNumber(VerseIndex(bookIndex, chapterIndex, 0)).isEmpty())
            }
        }

        MockContents.strongNumberWords.keys.forEach {
            assertFalse(androidStrongNumberStorage.readStrongNumber(it).isValid())
        }
    }

    @Test
    fun testSaveThenRead() = runTest {
        androidStrongNumberStorage.save(MockContents.strongNumberIndex, MockContents.strongNumberReverseIndex, MockContents.strongNumberWords)

        MockContents.strongNumberIndex.keys.forEach { verseIndex ->
            assertEquals(MockContents.strongNumber[verseIndex], androidStrongNumberStorage.readStrongNumber(verseIndex))
        }
        MockContents.strongNumberReverseIndex.forEach { (sn, verseIndexes) ->
            assertEquals(verseIndexes, androidDatabase.strongNumberReverseIndexDao.read(sn))
        }
        MockContents.strongNumberWords.keys.forEach {
            assertEquals(StrongNumber(it, MockContents.strongNumberWords.getValue(it)), androidStrongNumberStorage.readStrongNumber(it))
        }
    }
}
