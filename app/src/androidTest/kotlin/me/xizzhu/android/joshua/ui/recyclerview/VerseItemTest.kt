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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class VerseItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(BaseItem.VERSE_ITEM, VerseItem(Verse.INVALID, false, false, {}, {}).getItemViewType())
    }

    @Test
    fun testTextForDisplay() {
        val expected = "Genesis 1:1\nIn the beginning God created the heaven and the earth."
        val actual = VerseItem(MockContents.kjvVerses[0], false, false, {}, {}).textForDisplay.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testTextForDisplayWithParallelTranslations() {
        val expected = "KJV 1:1\nIn the beginning God created the heaven and the earth.\n\n中文和合本 1:1\n起初神创造天地。"
        val actual = VerseItem(MockContents.kjvVersesWithCuvParallel[0], false, false, {}, {}).textForDisplay.toString()
        assertEquals(expected, actual)
    }
}
