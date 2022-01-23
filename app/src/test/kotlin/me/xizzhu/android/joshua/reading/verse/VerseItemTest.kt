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

package me.xizzhu.android.joshua.reading.verse

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class VerseItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_verse, VerseItem(Verse.INVALID, 0, false, 0, false).viewType)
    }

    @Test
    fun testTextForDisplay() {
        assertEquals(
                "1:1 ${MockContents.kjvVerses[0].text.text}",
                VerseItem(MockContents.kjvVerses[0], 0, false, 0, false).textForDisplay.toString()
        )
    }

    @Test
    fun testTextForDisplayWithParallelTranslations() {
        assertEquals(
                "${MockContents.kjvShortName} 1:1\n${MockContents.kjvVersesWithCuvParallel[0].text.text}\n\n${MockContents.cuvShortName} 1:1\n${MockContents.kjvVersesWithCuvParallel[0].parallel[0].text}",
                VerseItem(MockContents.kjvVersesWithCuvParallel[0], 0, false, 0, false).textForDisplay.toString()
        )
    }
}
