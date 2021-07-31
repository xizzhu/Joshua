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

package me.xizzhu.android.joshua.tests.robots

import androidx.test.espresso.matcher.ViewMatchers.withText
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.strongnumber.StrongNumberListActivity
import me.xizzhu.android.joshua.strongnumber.StrongNumberViewData
import me.xizzhu.android.joshua.strongnumber.VerseStrongNumberItem
import me.xizzhu.android.joshua.tests.action.clickOnItem
import me.xizzhu.android.joshua.tests.assertions.isDisplayed
import me.xizzhu.android.joshua.tests.assertions.isTextDisplayedBelow

class StrongNumberListActivityRobot(activity: StrongNumberListActivity)
    : BaseRobot<StrongNumberListActivity, StrongNumberListActivityRobot>(activity) {
    fun clickStrongNumberItem(position: Int): StrongNumberListActivityRobot {
        // first one is Strong number, second one is book name
        clickOnItem(R.id.strong_number_list, position + 2)
        return self()
    }

    fun isStrongNumberDisplayed(data: StrongNumberViewData): StrongNumberListActivityRobot {
        val strongNumberText = "${data.strongNumber.sn} ${data.strongNumber.meaning}"
        isDisplayed(strongNumberText)

        var previousBookIndex = -1
        var previousVerseText: String? = null
        data.verses.forEachIndexed { index, verse ->
            val verseText = VerseStrongNumberItem(
                    verse.verseIndex,
                    data.bookShortNames[verse.verseIndex.bookIndex],
                    verse.text.text,
                    {}
            ).textForDisplay.toString()
            if (verse.verseIndex.bookIndex > previousBookIndex) {
                isTextDisplayedBelow(
                        data.bookNames[verse.verseIndex.bookIndex],
                        if (index == 0) {
                            withText(strongNumberText)
                        } else {
                            withText(previousVerseText!!)
                        }
                )
                isTextDisplayedBelow(verseText, withText(data.bookNames[verse.verseIndex.bookIndex]))
            } else {
                // same book
                isTextDisplayedBelow(verseText, withText(previousVerseText!!))
            }

            previousBookIndex = verse.verseIndex.bookIndex
            previousVerseText = verseText
        }
        return self()
    }
}
