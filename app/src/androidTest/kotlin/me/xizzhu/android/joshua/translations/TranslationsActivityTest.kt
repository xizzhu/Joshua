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

package me.xizzhu.android.joshua.translations

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.assertions.ActivityAssertions.assertActivityDestroyed
import me.xizzhu.android.joshua.tests.robots.TranslationsActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@LargeTest
class TranslationsActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(TranslationsActivity::class.java)

    @Test
    fun testLoadTranslationListWithError() {
        TranslationManager.throwErrorWhenLoadingTranslationList = true

        TranslationsActivityRobot(activityRule.activity)
                .refresh()
                .isTranslationListLoadingErrorDialogDisplayed()
                .confirmLoadTranslationListRetryRequest()
                .isTranslationListLoadingErrorDialogDisplayed()
                .cancelLoadTranslationListRetryRequest()

        assertActivityDestroyed(activityRule.activity)
        assertTrue(BibleReadingManager.currentTranslation.value!!.isEmpty())
    }

    @Test
    fun testNoDownloadedTranslation() {
        val robot = TranslationsActivityRobot(activityRule.activity)
                .hasNoTranslationDownloaded()
        assertTrue(BibleReadingManager.currentTranslation.value!!.isEmpty())

        robot.refresh()
                .hasNoTranslationDownloaded()
        assertTrue(BibleReadingManager.currentTranslation.value!!.isEmpty())
    }

    @Test
    fun testTryDownloadTranslationButCancelRequest() {
        TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjv()
                .cancelDownloadRequest()
                .hasNoTranslationDownloaded()
        assertTrue(BibleReadingManager.currentTranslation.value!!.isEmpty())
    }

    @Test
    fun testDownloadTranslationThenCancel() {
        TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjv()
                .confirmDownloadRequest()
                .cancelDownload()
                .hasNoTranslationDownloaded()
        assertTrue(BibleReadingManager.currentTranslation.value!!.isEmpty())
    }

    @Test
    fun testDownloadTranslationWithError() {
        TranslationManager.throwErrorWhenDownloadingTranslation = true

        val robot = TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjv()
                .confirmDownloadRequest()
                .waitUntilDownloadFinish()
                .isDownloadRetryRequestDialogDisplayed()
                .confirmDownloadRetryRequest()
                .waitUntilDownloadFinish()
        assertTrue(BibleReadingManager.currentTranslation.value!!.isEmpty())

        robot.isDownloadRetryRequestDialogDisplayed()
                .cancelDownloadRetryRequest()
                .hasNoTranslationDownloaded()
        assertTrue(BibleReadingManager.currentTranslation.value!!.isEmpty())
    }

    @Test
    fun testDownloadAndRemoveTranslation() {
        // download KJV
        val robot = TranslationsActivityRobot(activityRule.activity)
                .tryDownloadKjv()
                .confirmDownloadRequest()
                .waitUntilDownloadFinish()
                .hasKjvDownloaded()
        assertEquals(MockContents.kjvShortName, BibleReadingManager.currentTranslation.value)

        // try to remove KJV (current translation), but nothing happens
        robot.tryRemoveKjv()
                .removeRequestDialogNotDisplayed()
                .hasKjvDownloaded()

        // download CUV
        robot.tryDownloadCuv()
                .confirmDownloadRequest()
                .waitUntilDownloadFinish()
                .hasKjvAndCuvDownloaded()

        // try to remove KJV (current translation) again, but nothing happens
        robot.tryRemoveKjv()
                .removeRequestDialogNotDisplayed()
                .hasKjvAndCuvDownloaded()

        // try to remove CUV, but cancel
        robot.tryRemoveCuv()
                .cancelRemoveRequest()
                .hasKjvAndCuvDownloaded()

        // remove CUV
        robot.tryRemoveCuv()
                .confirmRemoveRequest()
                .hasKjvDownloaded()

        // download CUV again
        robot.tryDownloadCuv()
                .confirmDownloadRequest()
                .waitUntilDownloadFinish()
                .hasKjvAndCuvDownloaded()

        // select CUV as current translation
        robot.selectCuv()
        assertActivityDestroyed(activityRule.activity)
        assertEquals(MockContents.cuvShortName, BibleReadingManager.currentTranslation.value)
    }
}
