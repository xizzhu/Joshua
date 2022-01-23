/*
 * Copyright (C) 2022 Xizhi Zhu
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

import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class HttpStrongNumberServiceTest : BaseUnitTest() {
    private lateinit var strongNumberService: HttpStrongNumberService

    @BeforeTest
    override fun setup() {
        super.setup()

        strongNumberService = HttpStrongNumberService(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test toRemoteStrongNumberIndexes()`() {
        val actual = strongNumberService.toRemoteStrongNumberIndexes(javaClass.classLoader.getResourceAsStream("sn_indexes.zip"))
        (0 until Bible.BOOK_COUNT).forEach { bookIndex ->
            (0 until Bible.getChapterCount(bookIndex)).forEach { chapterIndex ->
                assertTrue(actual.indexes.containsKey(VerseIndex(bookIndex, chapterIndex, 0)))
            }
        }
        assertTrue(actual.reverseIndexes.isNotEmpty())
    }

    @Test
    fun `test toRemoteStrongNumberWords()`() {
        val actual = strongNumberService.toRemoteStrongNumberWords(javaClass.classLoader.getResourceAsStream("sn_en.zip"))
        assertEquals(Constants.STRONG_NUMBER_HEBREW_COUNT + Constants.STRONG_NUMBER_GREEK_COUNT, actual.words.size)
    }
}
