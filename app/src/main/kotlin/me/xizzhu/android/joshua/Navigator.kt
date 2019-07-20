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
import android.os.Bundle
import androidx.annotation.IntDef
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarksActivity
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.annotated.notes.NotesActivity
import me.xizzhu.android.joshua.progress.ReadingProgressActivity
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.settings.SettingsActivity
import me.xizzhu.android.joshua.translations.TranslationManagementActivity

class Navigator {
    companion object {
        const val SCREEN_READING = 0
        const val SCREEN_SEARCH = 1
        const val SCREEN_TRANSLATION_MANAGEMENT = 2
        const val SCREEN_READING_PROGRESS = 3
        const val SCREEN_BOOKMARKS = 4
        const val SCREEN_HIGHLIGHTS = 5
        const val SCREEN_NOTES = 6
        const val SCREEN_SETTINGS = 7

        @IntDef(SCREEN_READING, SCREEN_SEARCH, SCREEN_TRANSLATION_MANAGEMENT, SCREEN_READING_PROGRESS,
                SCREEN_BOOKMARKS, SCREEN_HIGHLIGHTS, SCREEN_NOTES, SCREEN_SETTINGS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Screen
    }

    fun navigate(activity: Activity, @Screen screen: Int, extras: Bundle? = null) {
        val intent = when (screen) {
            SCREEN_READING -> Intent(activity, ReadingActivity::class.java)
            SCREEN_SEARCH -> Intent(activity, SearchActivity::class.java)
            SCREEN_TRANSLATION_MANAGEMENT -> Intent(activity, TranslationManagementActivity::class.java)
            SCREEN_READING_PROGRESS -> Intent(activity, ReadingProgressActivity::class.java)
            SCREEN_BOOKMARKS -> Intent(activity, BookmarksActivity::class.java)
            SCREEN_HIGHLIGHTS -> Intent(activity, HighlightsActivity::class.java)
            SCREEN_NOTES -> Intent(activity, NotesActivity::class.java)
            SCREEN_SETTINGS -> Intent(activity, SettingsActivity::class.java)
            else -> throw IllegalArgumentException("Unknown screen - $screen")
        }
        extras?.let { intent.putExtras(it) }
        activity.startActivity(intent)
    }
}
