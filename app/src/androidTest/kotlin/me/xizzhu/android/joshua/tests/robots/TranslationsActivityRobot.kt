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

package me.xizzhu.android.joshua.tests.robots

import androidx.test.espresso.matcher.ViewMatchers.withText
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.action.*
import me.xizzhu.android.joshua.tests.assertions.doesNotExist
import me.xizzhu.android.joshua.tests.assertions.isDialogDisplayed
import me.xizzhu.android.joshua.tests.assertions.isDisplayed
import me.xizzhu.android.joshua.tests.assertions.isTextDisplayedBelow
import me.xizzhu.android.joshua.tests.waitUntilDismissed
import me.xizzhu.android.joshua.translations.TranslationsActivity
import java.util.*

class TranslationsActivityRobot(activity: TranslationsActivity)
    : BaseRobot<TranslationsActivity, TranslationsActivityRobot>(activity) {
    fun refresh(): TranslationsActivityRobot {
        swipeDown(R.id.translation_list)
        return self()
    }

    fun confirmLoadTranslationListRetryRequest(): TranslationsActivityRobot {
        isTranslationListLoadingErrorDialogDisplayed()
        clickDialogPositiveButton()
        return self()
    }

    fun cancelLoadTranslationListRetryRequest(): TranslationsActivityRobot {
        isTranslationListLoadingErrorDialogDisplayed()
        clickDialogNegativeButton()
        return self()
    }

    fun isTranslationListLoadingErrorDialogDisplayed(): TranslationsActivityRobot {
        isDialogDisplayed(R.string.dialog_load_translation_list_error)
        return self()
    }

    fun tryDownloadCuv(): TranslationsActivityRobot {
        clickText(MockContents.cuvTranslationInfo.name)
        return self()
    }

    fun tryDownloadKjv(): TranslationsActivityRobot {
        clickText(MockContents.kjvTranslationInfo.name)
        return self()
    }

    fun confirmDownloadRequest(): TranslationsActivityRobot {
        clickDialogPositiveButton()
        return self()
    }

    fun cancelDownloadRequest(): TranslationsActivityRobot {
        clickDialogNegativeButton()
        return self()
    }

    fun confirmDownloadRetryRequest(): TranslationsActivityRobot {
        isDownloadRetryRequestDialogDisplayed()
        clickDialogPositiveButton()
        return self()
    }

    fun cancelDownloadRetryRequest(): TranslationsActivityRobot {
        isDownloadRetryRequestDialogDisplayed()
        clickDialogNegativeButton()
        return self()
    }

    fun isDownloadRetryRequestDialogDisplayed(): TranslationsActivityRobot {
        isDialogDisplayed(R.string.dialog_download_error)
        return self()
    }

    fun cancelDownload(): TranslationsActivityRobot = pressBack()

    fun waitUntilDownloadFinish(): TranslationsActivityRobot {
        activity.translationListPresenter.downloadTranslationDialog.waitUntilDismissed()
        return self()
    }

    fun hasNoTranslationDownloaded(): TranslationsActivityRobot {
        isDisplayed(R.string.header_available_translations)
        isTextDisplayedBelow(Locale(MockContents.kjvTranslationInfo.language.split("_")[0]).displayLanguage, withText(R.string.header_available_translations))
        isTextDisplayedBelow(MockContents.kjvTranslationInfo.name, withText(Locale(MockContents.kjvTranslationInfo.language.split("_")[0]).displayLanguage))
        isTextDisplayedBelow(Locale(MockContents.cuvTranslationInfo.language.split("_")[0]).displayLanguage, withText(MockContents.kjvTranslationInfo.name))
        isTextDisplayedBelow(MockContents.cuvTranslationInfo.name, withText(Locale(MockContents.cuvTranslationInfo.language.split("_")[0]).displayLanguage))
        return self()
    }

    fun hasKjvDownloaded(): TranslationsActivityRobot {
        isDisplayed(Locale(MockContents.kjvTranslationInfo.language.split("_")[0]).displayLanguage)
        isTextDisplayedBelow(MockContents.kjvTranslationInfo.name, withText(Locale(MockContents.kjvTranslationInfo.language.split("_")[0]).displayLanguage))
        isTextDisplayedBelow(R.string.header_available_translations, withText(MockContents.kjvTranslationInfo.name))
        isTextDisplayedBelow(Locale(MockContents.cuvTranslationInfo.language.split("_")[0]).displayLanguage, withText(R.string.header_available_translations))
        isTextDisplayedBelow(MockContents.cuvTranslationInfo.name, withText(Locale(MockContents.cuvTranslationInfo.language.split("_")[0]).displayLanguage))
        return self()
    }

    fun hasKjvAndCuvDownloaded(): TranslationsActivityRobot {
        isDisplayed(Locale(MockContents.kjvTranslationInfo.language.split("_")[0]).displayLanguage)
        isTextDisplayedBelow(MockContents.kjvTranslationInfo.name, withText(Locale(MockContents.kjvTranslationInfo.language.split("_")[0]).displayLanguage))
        isTextDisplayedBelow(Locale(MockContents.cuvTranslationInfo.language.split("_")[0]).displayLanguage, withText(MockContents.kjvTranslationInfo.name))
        isTextDisplayedBelow(MockContents.cuvTranslationInfo.name, withText(Locale(MockContents.cuvTranslationInfo.language.split("_")[0]).displayLanguage))
        return self()
    }

    fun tryRemoveKjv(): TranslationsActivityRobot {
        longClickText(MockContents.kjvTranslationInfo.name)
        return self()
    }

    fun tryRemoveCuv(): TranslationsActivityRobot {
        longClickText(MockContents.cuvTranslationInfo.name)
        return self()
    }

    fun confirmRemoveRequest(): TranslationsActivityRobot {
        clickDialogPositiveButton()
        return self()
    }

    fun cancelRemoveRequest(): TranslationsActivityRobot {
        clickDialogNegativeButton()
        return self()
    }

    fun removeRequestDialogNotDisplayed(): TranslationsActivityRobot {
        doesNotExist(R.string.dialog_delete_translation_confirmation)
        return self()
    }

    fun selectCuv(): TranslationsActivityRobot {
        clickText(MockContents.cuvTranslationInfo.name)
        return self()
    }
}
