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

package me.xizzhu.android.joshua.end2end

import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.repository.remote.android.prepareTranslationList
import me.xizzhu.android.joshua.translations.TranslationsActivity
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
@LargeTest
class TranslationsActivityTest : BaseE2ETest() {
    @get:Rule
    val activityRule: ActivityTestRule<TranslationsActivity> = ActivityTestRule(TranslationsActivity::class.java, true, false)

    @Test
    fun testLoadTranslationList() {
        TranslationsActivityRobot(activityRule)
                .launch()
                .verifyNoneDownloaded()
    }
}

private class TranslationsActivityRobot(activityRule: ActivityTestRule<TranslationsActivity>)
    : BaseRobot<TranslationsActivity, TranslationsActivityRobot>(activityRule) {
    override fun launch(): TranslationsActivityRobot {
        prepareTranslationList()
        return super.launch()
    }

    fun verifyNoneDownloaded(): TranslationsActivityRobot {
        hasText(R.string.header_available_translations)
                .isTextDisplayedBelow(Locale("en").displayLanguage, withText(R.string.header_available_translations))
                .isTextDisplayedBelow("King James Version", withText(Locale("en").displayLanguage))
                .isTextDisplayedBelow(Locale("zh").displayLanguage, withText("King James Version"))
                .isTextDisplayedBelow("中文和合本（简体）", withText(Locale("zh").displayLanguage))
        return self()
    }
}
