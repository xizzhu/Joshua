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
import me.xizzhu.android.joshua.model.BibleReadingManager
import me.xizzhu.android.joshua.model.TranslationInfo
import me.xizzhu.android.joshua.model.TranslationManager
import me.xizzhu.android.joshua.utils.MVPPresenter
import java.lang.Exception

class TranslationManagementPresenter(
        private val bibleReadingManager: BibleReadingManager, private val translationManager: TranslationManager)
    : MVPPresenter<TranslationManagementView>() {
    fun loadTranslations(forceRefresh: Boolean) {
        launch(Dispatchers.Main) {
            val translations = async(Dispatchers.IO) { translationManager.loadTranslations(forceRefresh) }
            val currentTranslation = async(Dispatchers.IO) { bibleReadingManager.loadCurrentTranslation() }
            try {
                view?.onTranslationsLoaded(translations.await(), currentTranslation.await())
            } catch (e: Exception) {
                view?.onTranslationsLoadFailed()
            }
        }
    }

    fun downloadTranslation(translationInfo: TranslationInfo) {
        launch(Dispatchers.Main) {
            try {
                produce<Int>(Dispatchers.IO) {
                    translationManager.downloadTranslation(this, translationInfo)
                }.consumeEach {
                    view?.onTranslationDownloadProgressed(it)
                }
                view?.onTranslationDownloaded()
            } catch (e: Exception) {
                e.printStackTrace()
                view?.onTranslationDownloadFailed()
            }
        }
    }
}
