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

package me.xizzhu.android.joshua.core.repository.local.android.db

import me.xizzhu.android.joshua.core.CrossReferences
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class CrossReferencesDaoTest : BaseSqliteTest() {
    fun testEmptyTable() {
        val actual = androidDatabase.crossReferencesDao.read(VerseIndex(0, 0, 0))
        assertEquals(VerseIndex(0, 0, 0), actual.verseIndex)
        assertTrue(actual.referenced.isEmpty())
    }

    @Test
    fun testReplaceThenRead() {
        androidDatabase.crossReferencesDao.replace(MockContents.crossReferences)

        MockContents.crossReferences.forEach { (verseIndex, referenced) ->
            assertEquals(CrossReferences(verseIndex, referenced), androidDatabase.crossReferencesDao.read(verseIndex))
        }
    }
}
