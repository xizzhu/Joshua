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

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.annotated.highlights.list.HighlightItem
import me.xizzhu.android.joshua.tests.action.clickOnItem
import me.xizzhu.android.joshua.tests.assertions.isDisplayed
import me.xizzhu.android.joshua.tests.matchers.atPositionOnRecyclerView

class HighlightsActivityRobot(activity: HighlightsActivity) : BaseRobot<HighlightsActivity, HighlightsActivityRobot>(activity) {
    fun isNoHighlightsDisplayed(): HighlightsActivityRobot {
        isDisplayed(atPositionOnRecyclerView(R.id.verse_list, 0, 0), R.string.text_no_highlights)
        return self()
    }

    fun areHighlightsDisplayed(highlights: List<HighlightItem>): HighlightsActivityRobot {
        highlights.forEachIndexed { index, highlight ->
            isDisplayed(atPositionOnRecyclerView(R.id.verse_list, index + 1, 0), highlight.textForDisplay.toString())
        }
        return self()
    }

    fun clickHighlight(position: Int): HighlightsActivityRobot {
        clickOnItem(R.id.verse_list, position + 1)
        return self()
    }
}
