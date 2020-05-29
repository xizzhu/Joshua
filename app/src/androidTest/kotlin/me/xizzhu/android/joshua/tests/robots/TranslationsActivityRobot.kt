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

package me.xizzhu.android.joshua.tests.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.repository.remote.android.clearPrepared
import me.xizzhu.android.joshua.core.repository.remote.android.prepareCuv
import me.xizzhu.android.joshua.core.repository.remote.android.prepareKjv
import me.xizzhu.android.joshua.tests.waitUntilDismissed
import me.xizzhu.android.joshua.translations.TranslationsActivity
import java.util.*

class TranslationsActivityRobot(activity: TranslationsActivity, prev: ReadingActivityRobot? = null)
    : BaseRobot<TranslationsActivity, ReadingActivityRobot?, TranslationsActivityRobot>(activity, prev) {
    fun refresh(): TranslationsActivityRobot {
        onView(withId(R.id.translation_list)).perform(swipeDown())
        return self()
    }

    fun confirmLoadTranslationListRetryRequest(): TranslationsActivityRobot =
            isTranslationListLoadingErrorDialogDisplayed()
                    .clickDialogPositiveButton()

    fun cancelLoadTranslationListRetryRequest(): TranslationsActivityRobot =
            isTranslationListLoadingErrorDialogDisplayed()
                    .clickDialogNegativeButton()

    fun isTranslationListLoadingErrorDialogDisplayed(): TranslationsActivityRobot =
            isDialogDisplayed(R.string.dialog_load_translation_list_error)

    fun tryDownloadCuv(): TranslationsActivityRobot {
        prepareCuv()
        return clickText("中文和合本（简体）")
    }

    fun tryDownloadKjv(): TranslationsActivityRobot {
        prepareKjv()
        return clickText("King James Version")
    }

    fun tryDownloadKjvWithError(): TranslationsActivityRobot {
        clearPrepared()
        return clickText("King James Version")
    }

    fun confirmDownloadRequest(): TranslationsActivityRobot =
            isDownloadRequestDialogDisplayed()
                    .clickDialogPositiveButton()

    fun cancelDownloadRequest(): TranslationsActivityRobot =
            isDownloadRequestDialogDisplayed()
                    .clickDialogNegativeButton()

    fun isDownloadRequestDialogDisplayed(): TranslationsActivityRobot =
            isDialogDisplayed(R.string.dialog_download_translation_confirmation)

    fun confirmDownloadRetryRequest(): TranslationsActivityRobot =
            isDownloadRetryRequestDialogDisplayed()
                    .clickDialogPositiveButton()

    fun cancelDownloadRetryRequest(): TranslationsActivityRobot =
            isDownloadRetryRequestDialogDisplayed()
                    .clickDialogNegativeButton()

    fun isDownloadRetryRequestDialogDisplayed(): TranslationsActivityRobot =
            isDialogDisplayed(R.string.dialog_download_error)

    fun cancelDownload(): TranslationsActivityRobot = pressBack()

    fun waitUntilDownloadFinish(): TranslationsActivityRobot {
        activity.translationListPresenter.downloadTranslationDialog.waitUntilDismissed()
        return self()
    }

    fun hasNoTranslationDownloaded(): TranslationsActivityRobot =
            isTextDisplayed(R.string.header_available_translations)
                    .isTextDisplayedBelow(Locale("en").displayLanguage, withText(R.string.header_available_translations))
                    .isTextDisplayedBelow("King James Version", withText(Locale("en").displayLanguage))
                    .isTextDisplayedBelow(Locale("zh").displayLanguage, withText("King James Version"))
                    .isTextDisplayedBelow("中文和合本（简体）", withText(Locale("zh").displayLanguage))

    fun hasKjvDownloaded(): TranslationsActivityRobot =
            isTextDisplayed(Locale("en").displayLanguage)
                    .isTextDisplayedBelow("King James Version", withText(Locale("en").displayLanguage))
                    .isTextDisplayedBelow(R.string.header_available_translations, withText("King James Version"))
                    .isTextDisplayedBelow(Locale("zh").displayLanguage, withText(R.string.header_available_translations))
                    .isTextDisplayedBelow("中文和合本（简体）", withText(Locale("zh").displayLanguage))

    fun hasKjvAndCuvDownloaded(): TranslationsActivityRobot =
            isTextDisplayed(Locale("en").displayLanguage)
                    .isTextDisplayedBelow("King James Version", withText(Locale("en").displayLanguage))
                    .isTextDisplayedBelow(Locale("zh").displayLanguage, withText("King James Version"))
                    .isTextDisplayedBelow("中文和合本（简体）", withText(Locale("zh").displayLanguage))

    fun tryRemoveKjv(): TranslationsActivityRobot = longClickText("King James Version")

    fun tryRemoveCuv(): TranslationsActivityRobot = longClickText("中文和合本（简体）")

    fun confirmRemoveRequest(): TranslationsActivityRobot =
            isRemoveRequestDialogDisplayed()
                    .clickDialogPositiveButton()

    fun cancelRemoveRequest(): TranslationsActivityRobot =
            isRemoveRequestDialogDisplayed()
                    .clickDialogNegativeButton()

    fun isRemoveRequestDialogDisplayed(): TranslationsActivityRobot =
            isDialogDisplayed(R.string.dialog_delete_translation_confirmation)

    fun removeRequestDialogNotDisplayed(): TranslationsActivityRobot =
            dialogNotExist(R.string.dialog_delete_translation_confirmation)

    fun selectCuv(): TranslationsActivityRobot {
        clickText("中文和合本（简体）")
        return self()
    }
}
