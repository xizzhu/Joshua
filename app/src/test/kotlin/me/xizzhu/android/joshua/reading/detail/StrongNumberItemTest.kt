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

package me.xizzhu.android.joshua.reading.detail

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class StrongNumberItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_strong_number, StrongNumberItem(StrongNumber("", "")).viewType)
    }

    @Test
    fun testTextForDisplay() {
        assertEquals(
                "H7225\nbeginning, chief(-est), first(-fruits, part, time), principal thing.",
                StrongNumberItem(StrongNumber("H7225", MockContents.strongNumberWords.getValue("H7225"))).textForDisplay.toString()
        )
    }

    @Test
    fun testTextForDisplayWithInvalidStrongNumber() {
        assertTrue(StrongNumberItem(StrongNumber.INVALID).textForDisplay.isEmpty())
        assertTrue(StrongNumberItem(StrongNumber("random", "")).textForDisplay.isEmpty())
        assertTrue(StrongNumberItem(StrongNumber("", "random")).textForDisplay.isEmpty())
    }
}
