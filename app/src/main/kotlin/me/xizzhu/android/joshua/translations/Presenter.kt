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
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.model.BibleReadingManager
import me.xizzhu.android.joshua.model.TranslationInfo
import me.xizzhu.android.joshua.model.TranslationManager
import me.xizzhu.android.joshua.utils.MVPPresenter
import java.lang.Exception
import java.util.*
import kotlin.Comparator

class TranslationManagementPresenter(
        private val bibleReadingManager: BibleReadingManager, private val translationManager: TranslationManager)
    : MVPPresenter<TranslationManagementView>() {
    fun loadTranslations(forceRefresh: Boolean) {
        launch(Dispatchers.Main) {
            val translations = async(Dispatchers.IO) {
                val translations = translationManager.loadTranslations(forceRefresh).groupBy { it.downloaded }
                translations.forEach {
                    it.value.sortedWith(object : Comparator<TranslationInfo> {
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
                    })
                }
                Pair(translations.getOrElse(true) { emptyList() },
                        translations.getOrElse(false) { emptyList() })
            }
            val currentTranslation = async(Dispatchers.IO) { bibleReadingManager.currentTranslation }
            try {
                val (downloaded, available) = translations.await()
                view?.onTranslationsLoaded(downloaded, available, currentTranslation.await())
            } catch (e: Exception) {
                view?.onTranslationsLoadFailed()
            }
        }
    }

    fun downloadTranslation(translationInfo: TranslationInfo) {
        view?.onTranslationDownloadStarted()

        launch(Dispatchers.Main) {
            try {
                produce<Int>(Dispatchers.IO) {
                    translationManager.downloadTranslation(this, translationInfo)
                }.consumeEach {
                    view?.onTranslationDownloadProgressed(it)
                }
                if (bibleReadingManager.currentTranslation.isEmpty()) {
                    bibleReadingManager.currentTranslation = translationInfo.shortName
                }
                view?.onTranslationDownloaded()
            } catch (e: Exception) {
                e.printStackTrace()
                view?.onTranslationDownloadFailed()
            }
        }
    }

    fun setCurrentTranslation(currentTranslation: TranslationInfo) {
        bibleReadingManager.currentTranslation = currentTranslation.shortName
        view?.onTranslationSelected()
    }
}
