/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.progress

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.robots.ReadingProgressActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
@LargeTest
class ReadingProgressActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(ReadingProgressActivity::class.java, false, false)

    @Test
    fun testReadingProgress() {
        val readingProgress = ReadingProgress(5, 0L,
                listOf(
                        ReadingProgress.ChapterReadingStatus(0, 0, 1, 2L, 3L),
                        ReadingProgress.ChapterReadingStatus(0, 8, 1, 2L, 3L),
                        ReadingProgress.ChapterReadingStatus(0, 9, 1, 2L, 3L),
                        ReadingProgress.ChapterReadingStatus(63, 0, 1, 2L, 3L),
                        ReadingProgress.ChapterReadingStatus(64, 0, 1, 2L, 3L)
                ))
        BibleReadingManager.currentTranslation.offer(MockContents.kjvShortName)
        ReadingProgressManager.readingProgress = readingProgress
        activityRule.launchActivity(Intent())

        ReadingProgressActivityRobot(activityRule.activity)
                .isReadingProgressDisplayed(ReadingProgressViewData(readingProgress, MockContents.kjvBookNames), 0, 2)
    }
}
