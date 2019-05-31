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

package me.xizzhu.android.joshua.ui.recyclerview

import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VerseTextItemTest : BaseUnitTest() {
    @Test
    fun testTextForDisplay() {
        val expected = "KJV, Genesis 1:1\nIn the beginning God created the heaven and the earth."
        val actual = VerseTextItem(MockContents.kjvVerses[0].verseIndex, MockContents.kjvVerses[0].text).textForDisplay.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testTextForDisplayWithInvalidVerse() {
        assertTrue(VerseTextItem(VerseIndex.INVALID, Verse.Text.INVALID).textForDisplay.isEmpty())
        assertTrue(VerseTextItem(VerseIndex.INVALID, MockContents.kjvVerses[0].text).textForDisplay.isEmpty())
        assertTrue(VerseTextItem(MockContents.kjvVerses[0].verseIndex, Verse.Text.INVALID).textForDisplay.isEmpty())
    }
}
