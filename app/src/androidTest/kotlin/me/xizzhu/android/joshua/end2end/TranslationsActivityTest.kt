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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.repository.remote.android.prepareCuv
import me.xizzhu.android.joshua.core.repository.remote.android.prepareKjv
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
    fun testLoadTranslationListWithError() {
        TranslationsActivityRobot(activityRule)
                .launchWithError()
                .hasDialogWithText(R.string.dialog_load_translation_list_error)
                .clickDialogPositive() // retry
                .hasDialogWithText(R.string.dialog_load_translation_list_error)
                .clickDialogNegative() // no longer retry, will close the activity
        assertNoCurrentTranslation()
    }

    @Test
    fun testDownloadAndRemoveTranslation() {
        val robot = TranslationsActivityRobot(activityRule)
                .launch()
                .verifyNoneDownloaded()
                .refresh()
                .verifyNoneDownloaded()
        assertNoCurrentTranslation()

        // try to download, but cancel before starting
        robot.tryDownloadKjvButCancel().verifyNoneDownloaded()
        assertNoCurrentTranslation()

        // try to download, but with error
        robot.tryDownloadKjvWithError()
                .clickDialogPositive() // retry
                .clickDialogNegative() // no longer retry
                .verifyNoneDownloaded()
        assertNoCurrentTranslation()

        // try to download, but then cancel after started
        robot.downloadKjv().pressBack().verifyNoneDownloaded()
        assertNoCurrentTranslation()

        // try a successful download
        robot.downloadKjv().verifyKjvDownloaded()
        assertCurrentTranslation("KJV")

        // try to remove KJV (current translation), nothing should happen
        robot.tryRemoveKjv().verifyKjvDownloaded()
        assertCurrentTranslation("KJV")

        // download another one
        robot.downloadCuv().verifyKjvAndCuvDownloaded()
        assertCurrentTranslation("KJV")

        // try to remove KJV (current translation), nothing should happen
        robot.tryRemoveKjv().verifyKjvAndCuvDownloaded()
        assertCurrentTranslation("KJV")

        // try to remove CUV, but cancel before starting
        robot.tryRemoveCuvButCancel().verifyKjvAndCuvDownloaded()
        assertCurrentTranslation("KJV")

        // try a successful removal
        robot.removeCuv().verifyKjvDownloaded()
        assertCurrentTranslation("KJV")

        // download CUV again
        robot.downloadCuv().verifyKjvAndCuvDownloaded()
        assertCurrentTranslation("KJV")

        // select CUV
        robot.selectCuv()
        assertCurrentTranslation("中文和合本")
    }
}

private class TranslationsActivityRobot(activityRule: ActivityTestRule<TranslationsActivity>)
    : BaseRobot<TranslationsActivity, TranslationsActivityRobot>(activityRule) {
    override fun launch(): TranslationsActivityRobot {
        prepareTranslationList()
        return super.launch()
    }

    fun launchWithError(): TranslationsActivityRobot {
        return super.launch()
    }

    fun refresh(): TranslationsActivityRobot {
        onView(withId(R.id.translation_list)).perform(swipeDown())
        return self()
    }

    fun tryDownloadKjvWithError(): TranslationsActivityRobot {
        clickText("King James Version")
        return self()
    }

    fun tryDownloadKjvButCancel(): TranslationsActivityRobot {
        prepareKjv()
        clickText("King James Version")
        clickDialogNegative()
        return self()
    }

    fun downloadKjv(): TranslationsActivityRobot {
        prepareKjv()
        clickText("King James Version")
        clickDialogPositive()
        return self()
    }

    fun downloadCuv(): TranslationsActivityRobot {
        prepareCuv()
        clickText("中文和合本（简体）")
        clickDialogPositive()
        return self()
    }

    fun tryRemoveKjv(): TranslationsActivityRobot {
        longClickText("King James Version")
        return self()
    }

    fun tryRemoveCuvButCancel(): TranslationsActivityRobot {
        longClickText("中文和合本（简体）")
        clickDialogNegative()
        return self()
    }

    fun removeCuv(): TranslationsActivityRobot {
        longClickText("中文和合本（简体）")
        clickDialogPositive()
        return self()
    }

    fun selectCuv(): TranslationsActivityRobot {
        clickText("中文和合本（简体）")
        return self()
    }

    fun verifyNoneDownloaded(): TranslationsActivityRobot {
        hasText(R.string.header_available_translations)
                .isTextDisplayedBelow(Locale("en").displayLanguage, withText(R.string.header_available_translations))
                .isTextDisplayedBelow("King James Version", withText(Locale("en").displayLanguage))
                .isTextDisplayedBelow(Locale("zh").displayLanguage, withText("King James Version"))
                .isTextDisplayedBelow("中文和合本（简体）", withText(Locale("zh").displayLanguage))
        return self()
    }

    fun verifyKjvDownloaded(): TranslationsActivityRobot {
        val dialog = activityRule.activity.translationListPresenter.downloadTranslationDialog
        val downloading = dialog?.let { ProgressDialogIdlingResource(it) }
        downloading?.let { IdlingRegistry.getInstance().register(it) }

        hasText(Locale("en").displayLanguage)
                .isTextDisplayedBelow("King James Version", withText(Locale("en").displayLanguage))
                .isTextDisplayedBelow(R.string.header_available_translations, withText("King James Version"))
                .isTextDisplayedBelow(Locale("zh").displayLanguage, withText(R.string.header_available_translations))
                .isTextDisplayedBelow("中文和合本（简体）", withText(Locale("zh").displayLanguage))

        downloading?.let { IdlingRegistry.getInstance().unregister(it) }
        return self()
    }

    fun verifyKjvAndCuvDownloaded(): TranslationsActivityRobot {
        val dialog = activityRule.activity.translationListPresenter.downloadTranslationDialog
        val downloading = dialog?.let { ProgressDialogIdlingResource(it) }
        downloading?.let { IdlingRegistry.getInstance().register(it) }

        hasText(Locale("en").displayLanguage)
                .isTextDisplayedBelow("King James Version", withText(Locale("en").displayLanguage))
                .isTextDisplayedBelow(Locale("zh").displayLanguage, withText("King James Version"))
                .isTextDisplayedBelow("中文和合本（简体）", withText(Locale("zh").displayLanguage))

        downloading?.let { IdlingRegistry.getInstance().unregister(it) }
        return self()
    }
}
