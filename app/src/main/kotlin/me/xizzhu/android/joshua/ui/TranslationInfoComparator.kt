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

package me.xizzhu.android.joshua.ui

import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import me.xizzhu.android.joshua.core.TranslationInfo
import java.util.*
import kotlin.Comparator

class TranslationInfoComparator(@SortOrder private val sortOrder: Int) : Comparator<TranslationInfo> {
    companion object {
        const val SORT_ORDER_LANGUAGE_THEN_NAME = 0
        const val SORT_ORDER_LANGUAGE_THEN_SHORT_NAME = 1

        @IntDef(SORT_ORDER_LANGUAGE_THEN_NAME, SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)
        @Retention(AnnotationRetention.SOURCE)
        annotation class SortOrder
    }

    override fun compare(t1: TranslationInfo, t2: TranslationInfo): Int {
        val userLanguage = userLanguage()
        val language1 = Locale(t1.language.split("_")[0]).displayLanguage
        val language2 = Locale(t2.language.split("_")[0]).displayLanguage
        val score1 = if (userLanguage == language1) 1 else 0
        val score2 = if (userLanguage == language2) 1 else 0
        var r = score2 - score1
        if (r == 0) {
            r = language1.compareTo(language2)
        }
        if (r == 0) {
            r = when (sortOrder) {
                SORT_ORDER_LANGUAGE_THEN_NAME -> t1.name.compareTo(t2.name)
                SORT_ORDER_LANGUAGE_THEN_SHORT_NAME -> t1.shortName.compareTo(t2.shortName)
                else -> throw IllegalArgumentException("Unsupported sort order: $sortOrder")
            }
        }
        return r
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun userLanguage(): String = Locale.getDefault().displayLanguage
}
