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

package me.xizzhu.android.joshua.tests.robots

import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.matcher.ViewMatchers.withText
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchResult
import me.xizzhu.android.joshua.tests.action.clickOnItem
import me.xizzhu.android.joshua.tests.action.typeText
import me.xizzhu.android.joshua.tests.assertions.isDisplayed
import me.xizzhu.android.joshua.tests.assertions.isTextDisplayedBelow
import me.xizzhu.android.joshua.tests.matchers.onView

class SearchActivityRobot(activity: SearchActivity) : BaseRobot<SearchActivity, SearchActivityRobot>(activity) {
    fun typeToSearchBox(text: String): SearchActivityRobot {
        typeText(R.id.search_src_text, text)
        return self()
    }

    fun startSearch(): SearchActivityRobot {
        onView(R.id.search_src_text).perform(pressImeActionButton())
        return self()
    }

    fun waitUntilSearchFinished(): SearchActivityRobot {
        // TODO find a better way
        Thread.sleep(500L)
        return self()
    }

    fun clickSearchResultItem(position: Int): SearchActivityRobot {
        clickOnItem(R.id.search_result, position + 1) // first one is book name
        return self()
    }

    fun hasSearchResultShown(searchResult: SearchResult): SearchActivityRobot {
        isDisplayed(searchResult.bookNames[0])
        searchResult.verses.forEachIndexed { index, verse ->
            isTextDisplayedBelow(
                    "${searchResult.bookShortNames[0]} ${verse.verseIndex.chapterIndex + 1}:${verse.verseIndex.verseIndex + 1}\n${verse.text.text}",
                    withText(if (index == 0) {
                        searchResult.bookNames[0]
                    } else {
                        val previousVerse = searchResult.verses[index - 1]
                        "${searchResult.bookShortNames[0]} ${previousVerse.verseIndex.chapterIndex + 1}:${previousVerse.verseIndex.verseIndex + 1}\n${previousVerse.text.text}"
                    })
            )
        }
        return self()
    }
}
