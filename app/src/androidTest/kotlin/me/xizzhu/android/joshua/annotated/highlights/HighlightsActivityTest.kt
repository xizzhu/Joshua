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

package me.xizzhu.android.joshua.annotated.highlights

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.annotated.highlights.list.HighlightItem
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.core.repository.HighlightsRepository
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.robots.HighlightsActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class HighlightsActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(HighlightsActivity::class.java)

    @Test
    fun testEmptyHighlights() {
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName
        val robot = HighlightsActivityRobot(activityRule.activity)
                .isNoHighlightsDisplayed()

        HighlightsRepository._sortOrder.value = Constants.SORT_BY_BOOK
        robot.isNoHighlightsDisplayed()
    }

    @Test
    fun testHighlights() {
        val now = System.currentTimeMillis()
        val highlights = listOf(
                Highlight(VerseIndex(0, 0, 4), Highlight.COLOR_BLUE, now),
                Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_PINK, now)
        )
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName
        HighlightsRepository.annotations = highlights
        HighlightsActivityRobot(activityRule.activity)
                .areHighlightsDisplayed(highlights.map { highlight ->
                    HighlightItem(
                            highlight.verseIndex,
                            MockContents.kjvBookNames[highlight.verseIndex.bookIndex],
                            MockContents.kjvBookShortNames[highlight.verseIndex.bookIndex],
                            MockContents.kjvVerses[highlight.verseIndex.verseIndex].text.text,
                            highlight.color,
                            Constants.DEFAULT_SORT_ORDER,
                            {}
                    )
                })
                .clickHighlight(0)

        assertEquals(VerseIndex(0, 0, 4), BibleReadingManager.currentVerseIndex.value)
    }
}
