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

package me.xizzhu.android.joshua.reading.toolbar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.utils.MVPPresenter

class ToolbarPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<ToolbarView>() {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            val currentTranslation = readingInteractor.observeCurrentTranslation()
            receiveChannels.add(currentTranslation)
            currentTranslation.filter { it.isNotEmpty() }
                    .consumeEach {
                        view?.onCurrentTranslationUpdated(it)
                        view?.onBookShortNamesUpdated(readingInteractor.readBookShortNames(it))
                    }
        }
        launch(Dispatchers.Main) {
            val currentVerseIndex = readingInteractor.observeCurrentVerseIndex()
            receiveChannels.add(currentVerseIndex)
            currentVerseIndex.filter { it.isValid() }
                    .consumeEach { view?.onCurrentVerseIndexUpdated(it) }
        }
        launch(Dispatchers.Main) {
            val downloadedTranslations = readingInteractor.observeDownloadedTranslations()
            receiveChannels.add(downloadedTranslations)
            downloadedTranslations.consumeEach {
                if (it.isEmpty()) {
                    view?.onNoTranslationsDownloaded()
                } else {
                    view?.onDownloadedTranslationsLoaded(it.sortedBy { t -> t.language })
                }
            }
        }
        launch(Dispatchers.Main) {
            val parallelTranslations = readingInteractor.observeParallelTranslations()
            receiveChannels.add(parallelTranslations)
            parallelTranslations.consumeEach { view?.onParallelTranslationsUpdated(it) }
        }
    }

    fun updateCurrentTranslation(translationShortName: String) {
        launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentTranslation(translationShortName)
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to update current translation")
                view?.onCurrentTranslationUpdateFailed(translationShortName)
            }
        }
    }

    fun requestParallelTranslation(translationShortName: String) {
        launch(Dispatchers.Main) { readingInteractor.requestParallelTranslation(translationShortName) }
    }

    fun removeParallelTranslation(translationShortName: String) {
        launch(Dispatchers.Main) { readingInteractor.removeParallelTranslation(translationShortName) }
    }

    fun openTranslationManagement() {
        try {
            readingInteractor.openTranslationManagement()
        } catch (e: Exception) {
            Log.e(tag, e, "Failed to open translation management activity")
            view?.onFailedToNavigateToTranslationManagement()
        }
    }

    fun openReadingProgress() {
        try {
            readingInteractor.openReadingProgress()
        } catch (e: Exception) {
            Log.e(tag, e, "Failed to open reading progress activity")
            view?.onFailedToNavigateToReadingProgress()
        }
    }

    fun openBookmarks() {
        try {
            readingInteractor.openBookmarks()
        } catch (e: Exception) {
            Log.e(tag, e, "Failed to open bookmarks activity")
            view?.onFailedToNavigateToBookmarks()
        }
    }

    fun openSettings() {
        try {
            readingInteractor.openSettings()
        } catch (e: Exception) {
            Log.e(tag, e, "Failed to open settings activity")
            view?.onFailedToNavigateToSettings()
        }
    }

    fun finish() {
        readingInteractor.finish()
    }
}
