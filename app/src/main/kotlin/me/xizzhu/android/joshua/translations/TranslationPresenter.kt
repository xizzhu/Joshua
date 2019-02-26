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
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.onEach
import java.lang.Exception
import java.util.*
import kotlin.Comparator

class TranslationPresenter(private val translationViewController: TranslationViewController) : MVPPresenter<TranslationView>() {
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

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            receiveChannels.add(translationViewController.observeCurrentTranslation()
                    .onEach { view?.onCurrentTranslationUpdated(it) })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(translationViewController.observeAvailableTranslations()
                    .onEach {
                        view?.onAvailableTranslationsUpdated(it.sortedWith(translationComparator))
                    })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(translationViewController.observeDownloadedTranslations()
                    .onEach {
                        view?.onDownloadedTranslationsUpdated(it.sortedWith(translationComparator))
                    })
        }
        launch(Dispatchers.Main) {
            translationViewController.reload(false)
        }
    }

    fun downloadTranslation(translationInfo: TranslationInfo) {
        view?.onTranslationDownloadStarted()

        launch(Dispatchers.Main) {
            try {
                val downloadProgressChannel = Channel<Int>()
                launch(Dispatchers.IO) {
                    translationViewController.downloadTranslation(downloadProgressChannel, translationInfo)
                }
                downloadProgressChannel.onEach { progress ->
                    view?.onTranslationDownloadProgressed(progress)
                }

                if (translationViewController.observeCurrentTranslation().first().isEmpty()) {
                    translationViewController.saveCurrentTranslation(translationInfo.shortName, false)
                }
                view?.onTranslationDownloaded()
            } catch (e: Exception) {
                view?.onError(e)
            }
        }
    }

    fun updateCurrentTranslation(currentTranslation: TranslationInfo) {
        launch(Dispatchers.Main) {
            translationViewController.saveCurrentTranslation(currentTranslation.shortName, true)
        }
    }
}
