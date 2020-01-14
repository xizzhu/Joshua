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

package me.xizzhu.android.joshua.annotated

import android.content.res.Resources
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.utils.currentTimeMillis
import java.util.*

fun Long.formatDate(resources: Resources, calendar: Calendar): String {
    calendar.timeInMillis = currentTimeMillis()
    val currentYear = calendar.get(Calendar.YEAR)

    calendar.timeInMillis = this
    val year = calendar.get(Calendar.YEAR)
    return if (year == currentYear) {
        resources.getString(R.string.text_date_without_year,
                resources.getStringArray(R.array.text_months)[calendar.get(Calendar.MONTH)],
                calendar.get(Calendar.DATE))
    } else {
        resources.getString(R.string.text_date,
                resources.getStringArray(R.array.text_months)[calendar.get(Calendar.MONTH)],
                calendar.get(Calendar.DATE), year)
    }
}
