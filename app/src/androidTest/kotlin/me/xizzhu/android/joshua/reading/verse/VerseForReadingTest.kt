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

package me.xizzhu.android.joshua.reading.verse

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class VerseForReadingTest : BaseUnitTest() {
    @Test
    fun testGetIndexForDisplay() {
        assertEquals("1", VerseForReading(MockContents.kjvVerses[0], 9).indexForDisplay)

        assertEquals(" 1", VerseForReading(MockContents.kjvVerses[0], 10).indexForDisplay)
        assertEquals("10", VerseForReading(MockContents.kjvVerses[9], 10).indexForDisplay)
        assertEquals("99", VerseForReading(Verse(VerseIndex(1, 2, 98), Verse.Text("", "", ""), emptyList()), 99).indexForDisplay)

        assertEquals("  1", VerseForReading(MockContents.kjvVerses[0], 100).indexForDisplay)
        assertEquals(" 10", VerseForReading(MockContents.kjvVerses[9], 100).indexForDisplay)
        assertEquals("100", VerseForReading(Verse(VerseIndex(1, 2, 99), Verse.Text("", "", ""), emptyList()), 100).indexForDisplay)
    }

    @Test
    fun testGetIndexForDisplayWithParallelTranslations() {
        assertTrue(VerseForReading(MockContents.kjvVersesWithCuvParallel[0], 1).indexForDisplay.isEmpty())
    }

    @Test
    fun testGetTextForDisplay() {
        val expected = "In the beginning God created the heaven and the earth."
        val actual = VerseForReading(MockContents.kjvVerses[0], 1).textForDisplay.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testGetTextForDisplayWithParallelTranslations() {
        val expected = "KJV 1:1\nIn the beginning God created the heaven and the earth.\n\n中文和合本 1:1\n起初神创造天地。"
        val actual = VerseForReading(MockContents.kjvVersesWithCuvParallel[0], 1).textForDisplay.toString()
        assertEquals(expected, actual)
    }
}
