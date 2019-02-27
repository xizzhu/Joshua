/*
 * Copyright (C) 2019 Xizhi Zhu
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

package me.xizzhu.android.joshua

import android.app.Activity
import android.content.Intent
import androidx.annotation.IntDef
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.translations.TranslationManagementActivity

object Navigator {
    const val SCREEN_READING = 0
    const val SCREEN_SEARCH = 1
    const val SCREEN_TRANSLATION_MANAGEMENT = 2

    @IntDef(SCREEN_READING, SCREEN_SEARCH, SCREEN_TRANSLATION_MANAGEMENT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Screen

    fun navigate(activity: Activity, @Screen screen: Int) {
        val intent = when (screen) {
            SCREEN_READING -> Intent(activity, ReadingActivity::class.java)
            SCREEN_SEARCH -> Intent(activity, SearchActivity::class.java)
            SCREEN_TRANSLATION_MANAGEMENT -> Intent(activity, TranslationManagementActivity::class.java)
            else -> throw IllegalArgumentException("Unknown screen - $screen")
        }
        activity.startActivity(intent)
    }
}
