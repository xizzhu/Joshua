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

package me.xizzhu.android.joshua.translations

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.core.repository.remote.android.prepareTranslationList
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.assertions.ActivityAssertions.assertActivityDestroyed
import me.xizzhu.android.joshua.tests.assertions.Assertions.assertCurrentTranslation
import me.xizzhu.android.joshua.tests.assertions.Assertions.assertNoCurrentTranslation
import me.xizzhu.android.joshua.tests.robots.TranslationsActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
@LargeTest
class TranslationsActivityTest {
    @get:Rule
    val activityRule = object : EspressoTestRule<TranslationsActivity>(TranslationsActivity::class.java) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()

            prepareTranslationList()
        }
    }

    @Test
    fun testNoDownloadedTranslation() {
        val robot = TranslationsActivityRobot(activityRule.activity)
                .hasNoTranslationDownloaded()
        assertNoCurrentTranslation()

        robot.refresh()
                .hasNoTranslationDownloaded()
        assertNoCurrentTranslation()
    }

    @Test
    fun testTryDownloadTranslationButCancelRequest() {
        TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjv()
                .isDownloadRequestDialogDisplayed()
                .cancelDownloadRequest()
                .hasNoTranslationDownloaded()
        assertNoCurrentTranslation()
    }

    @Test
    fun testDownloadTranslationThenCancel() {
        TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjv()
                .isDownloadRequestDialogDisplayed()
                .confirmDownloadRequest()
                .cancelDownload()
                .hasNoTranslationDownloaded()
        assertNoCurrentTranslation()
    }

    @Test
    fun testDownloadTranslationWithError() {
        val robot = TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjvWithError()
                .isDownloadRequestDialogDisplayed()
                .confirmDownloadRequest()
                .isDownloadRetryRequestDialogDisplayed()
                .confirmDownloadRetryRequest()
        assertNoCurrentTranslation()

        robot.isDownloadRetryRequestDialogDisplayed()
                .cancelDownloadRetryRequest()
                .hasNoTranslationDownloaded()
        assertNoCurrentTranslation()
    }

    @Test
    fun testDownloadAndRemoveTranslation() {
        // download KJV
        val robot = TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjv()
                .isDownloadRequestDialogDisplayed()
                .confirmDownloadRequest()
                .waitUntilDownloadFinish()
                .hasKjvDownloaded()
        assertCurrentTranslation("KJV")

        // try to remove KJV (current translation), but nothing happens
        robot.tryRemoveKjv()
                .removeRequestDialogNotDisplayed()
                .hasKjvDownloaded()

        // download CUV
        robot.tryDownloadCuv()
                .isDownloadRequestDialogDisplayed()
                .confirmDownloadRequest()
                .waitUntilDownloadFinish()
                .hasKjvAndCuvDownloaded()

        // try to remove KJV (current translation) again, but nothing happens
        robot.tryRemoveKjv()
                .removeRequestDialogNotDisplayed()
                .hasKjvAndCuvDownloaded()

        // try to remove CUV, but cancel
        robot.tryRemoveCuv()
                .isRemoveRequestDialogDisplayed()
                .cancelRemoveRequest()
                .hasKjvAndCuvDownloaded()

        // remove CUV
        robot.tryRemoveCuv()
                .isRemoveRequestDialogDisplayed()
                .confirmRemoveRequest()
                .hasKjvDownloaded()

        // download CUV again
        robot.tryDownloadCuv()
                .isDownloadRequestDialogDisplayed()
                .confirmDownloadRequest()
                .waitUntilDownloadFinish()
                .hasKjvAndCuvDownloaded()

        // select CUV as current translation
        robot.selectCuv()
        assertActivityDestroyed(activityRule.activity)
        assertCurrentTranslation("中文和合本")
    }
}
