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

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.progress.ReadingProgressActivity
import me.xizzhu.android.joshua.progress.ReadingProgressViewData
import me.xizzhu.android.joshua.tests.assertions.isDisplayed
import me.xizzhu.android.joshua.tests.matchers.atPositionOnRecyclerView

class ReadingProgressActivityRobot(activity: ReadingProgressActivity)
    : BaseRobot<ReadingProgressActivity, ReadingProgressActivityRobot>(activity) {
    fun isReadingProgressDisplayed(
            data: ReadingProgressViewData, finishedOldTestament: Int, finishedNewTestament: Int
    ): ReadingProgressActivityRobot {
        isDisplayed(atPositionOnRecyclerView(R.id.reading_progress_list, 0, R.id.continuous_reading_days_value),
                activity.resources.getString(R.string.text_continuous_reading_count, data.readingProgress.continuousReadingDays))
        isDisplayed(atPositionOnRecyclerView(R.id.reading_progress_list, 0, R.id.chapters_read_value),
                data.readingProgress.chapterReadingStatus.size.toString())
        isDisplayed(atPositionOnRecyclerView(R.id.reading_progress_list, 0, R.id.finished_books_value),
                (finishedOldTestament + finishedNewTestament).toString())
        isDisplayed(atPositionOnRecyclerView(R.id.reading_progress_list, 0, R.id.finished_old_testament_value),
                finishedOldTestament.toString())
        isDisplayed(atPositionOnRecyclerView(R.id.reading_progress_list, 0, R.id.finished_new_testament_value),
                finishedNewTestament.toString())

        isDisplayed(atPositionOnRecyclerView(R.id.reading_progress_list, 1, R.id.book_name),
                data.bookNames[0])

        return self()
    }
}
