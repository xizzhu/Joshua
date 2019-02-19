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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.utils.MVPPresenter
import java.lang.Exception
import java.util.*
import kotlin.Comparator

class TranslationPresenter(private val bibleReadingManager: BibleReadingManager,
                           private val translationManager: TranslationManager) : MVPPresenter<TranslationView>() {
    private val translationComparator = object : Comparator<TranslationInfo> {
        override fun compare(t1: TranslationInfo, t2: TranslationInfo): Int {
            val userLocale = Locale.getDefault()
            val userLanguage = userLocale.language.toLowerCase(userLocale)

            val language1 = t1.language.split(("_"))[0]
            val language2 = t2.language.split(("_"))[0]
            val score1 = if (userLanguage == language1) 1 else 0
            val score2 = if (userLanguage == language2) 1 else 0
            var r = score2 - score1
            if (r == 0) {
                r = t1.language.compareTo(t2.language)
            }
            if (r == 0) {
                r = t1.name.compareTo(t2.name)
            }
            return r
        }
    }

    override fun onViewTaken() {
        super.onViewTaken()

        launch(Dispatchers.Main) {
            val currentTranslation = bibleReadingManager.observeCurrentTranslation()
            receiveChannels.add(currentTranslation)
            currentTranslation.consumeEach { view?.onCurrentTranslationUpdated(it) }
        }
        launch(Dispatchers.Main) {
            val availableTranslations = translationManager.observeAvailableTranslations()
            receiveChannels.add(availableTranslations)
            availableTranslations.consumeEach {
                view?.onAvailableTranslationsUpdated(it.sortedWith(translationComparator))
            }
        }
        launch(Dispatchers.Main) {
            val downloadedTranslations = translationManager.observeDownloadedTranslations()
            receiveChannels.add(downloadedTranslations)
            downloadedTranslations.consumeEach {
                view?.onDownloadedTranslationsUpdated(it.sortedWith(translationComparator))
            }
        }
        launch(Dispatchers.Main) { translationManager.reload(false) }
    }

    fun downloadTranslation(translationInfo: TranslationInfo) {
        view?.onTranslationDownloadStarted()

        launch(Dispatchers.Main) {
            try {
                val downloadProgressChannel = Channel<Int>()
                launch(Dispatchers.IO) {
                    translationManager.downloadTranslation(downloadProgressChannel, translationInfo)
                }
                downloadProgressChannel.consumeEach { progress ->
                    view?.onTranslationDownloadProgressed(progress)
                }

                if (bibleReadingManager.observeCurrentTranslation().receive().isEmpty()) {
                    bibleReadingManager.updateCurrentTranslation(translationInfo.shortName)
                }
                view?.onTranslationDownloaded()
            } catch (e: Exception) {
                view?.onError(e)
            }
        }
    }

    fun updateCurrentTranslation(currentTranslation: TranslationInfo) {
        launch(Dispatchers.Main) {
            bibleReadingManager.updateCurrentTranslation(currentTranslation.shortName)
            view?.onTranslationSelected()
        }
    }
}
