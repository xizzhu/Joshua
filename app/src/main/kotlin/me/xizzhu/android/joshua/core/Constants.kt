/*
 * Copyright (C) 2022 Xizhi Zhu
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

package me.xizzhu.android.joshua.core

import androidx.annotation.IntDef

object Constants {
    const val STRONG_NUMBER_HEBREW_COUNT = 8674
    const val STRONG_NUMBER_GREEK_COUNT = 5523

    const val SORT_BY_DATE = 0
    const val SORT_BY_BOOK = 1
    const val SORT_ORDER_COUNT = 2
    const val DEFAULT_SORT_ORDER = SORT_BY_DATE

    @IntDef(SORT_BY_DATE, SORT_BY_BOOK)
    @Retention(AnnotationRetention.SOURCE)
    annotation class SortOrder
}
