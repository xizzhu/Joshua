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

package me.xizzhu.android.joshua.search

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.robots.SearchActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class SearchActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(SearchActivity::class.java)

    @Test
    fun testSearch() {
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName

        SearchActivityRobot(activityRule.activity)
                .typeToSearchBox("query")
                .startSearch()
                .waitUntilSearchFinished()
                .hasSearchResultShown(SearchResult("query", MockContents.kjvVerses.subList(0, 5), MockContents.kjvBookNames, MockContents.kjvBookShortNames))
                .clickSearchResultItem(1)

        assertEquals(VerseIndex(0, 0, 1), BibleReadingManager.currentVerseIndex.value)
    }
}
