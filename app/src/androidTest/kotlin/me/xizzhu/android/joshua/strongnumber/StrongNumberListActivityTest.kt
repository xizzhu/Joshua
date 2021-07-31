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

package me.xizzhu.android.joshua.strongnumber

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.robots.StrongNumberListActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class StrongNumberListActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(StrongNumberListActivity::class.java, false)

    @Test
    fun testStrongNumber() {
        val sn = "H7225"
        activityRule.launchActivity(Intent().putExtras(StrongNumberListActivity.bundle(sn)))
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName

        StrongNumberListActivityRobot(activityRule.activity)
                .isStrongNumberDisplayed(StrongNumberViewData(
                        StrongNumber(sn, MockContents.strongNumberWords.getValue(sn)),
                        listOf(MockContents.kjvVerses[0]),
                        MockContents.kjvBookNames,
                        MockContents.kjvBookShortNames
                ))
                .clickStrongNumberItem(0)

        assertEquals(VerseIndex(0, 0, 0), BibleReadingManager.currentVerseIndex.value)
    }
}
