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

package me.xizzhu.android.joshua.tests

import android.app.Activity
import android.view.WindowManager
import androidx.test.espresso.IdlingPolicies
import androidx.test.rule.ActivityTestRule
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.repository.BookmarksRepository
import me.xizzhu.android.joshua.core.repository.HighlightsRepository
import me.xizzhu.android.joshua.core.repository.NotesRepository
import java.util.*
import java.util.concurrent.TimeUnit

open class EspressoTestRule<T : Activity>(activityClass: Class<T>,
                                          launchActivity: Boolean = true,
                                          private val resetBeforeLaunchingActivity: Boolean = true)
    : ActivityTestRule<T>(activityClass, false, launchActivity) {
    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()

        IdlingPolicies.setMasterPolicyTimeout(5L, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(5L, TimeUnit.MINUTES)

        Locale.setDefault(Locale.ENGLISH)

        if (resetBeforeLaunchingActivity) {
            reset()
        }
    }

    private fun reset() {
        BibleReadingManager.reset()
        BookmarksRepository.reset()
        HighlightsRepository.reset()
        NotesRepository.reset()
        ReadingProgressManager.reset()
        SettingsManager.reset()
        TranslationManager.reset()
    }

    override fun afterActivityLaunched() {
        super.afterActivityLaunched()

        activity.runOnUiThread {
            activity.window.addFlags(
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    override fun afterActivityFinished() {
        reset()
        super.afterActivityFinished()
    }
}
