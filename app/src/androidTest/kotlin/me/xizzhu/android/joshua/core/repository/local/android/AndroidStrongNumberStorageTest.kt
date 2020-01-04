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
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidStrongNumberStorageTest : BaseSqliteTest() {
    private lateinit var androidStrongNumberStorage: AndroidStrongNumberStorage

    @BeforeTest
    override fun setup() {
        super.setup()
        androidStrongNumberStorage = AndroidStrongNumberStorage(androidDatabase)
    }

    @Test
    fun testEmptyStrongNumber() {
        runBlocking {
            (0 until Bible.BOOK_COUNT).forEach { bookIndex ->
                (0 until Bible.getChapterCount(bookIndex)).forEach { chapterIndex ->
                    assertTrue(androidStrongNumberStorage.read(VerseIndex(bookIndex, chapterIndex, 0)).isEmpty())
                }
            }
        }
    }

    @Test
    fun testSaveThenRead() {
        runBlocking {
            androidStrongNumberStorage.save(MockContents.strongNumberIndex, MockContents.strongNumberReverseIndex, MockContents.strongNumberWords)

            MockContents.strongNumberIndex.keys.forEach { verseIndex ->
                assertEquals(MockContents.strongNumber[verseIndex], androidStrongNumberStorage.read(verseIndex))
            }
        }
    }
}
