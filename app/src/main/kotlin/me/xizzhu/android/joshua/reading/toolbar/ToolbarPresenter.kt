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
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.logger.Log

class ToolbarPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<ToolbarView>() {
    private val translationComparator = TranslationInfoComparator(
            TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)

    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            readingInteractor.observeCurrentTranslation().filter { it.isNotEmpty() }
                    .consumeEach {
                        view?.onCurrentTranslationUpdated(it)
                        view?.onBookShortNamesUpdated(readingInteractor.readBookShortNames(it))
                    }
        }
        coroutineScope.launch(Dispatchers.Main) {
            readingInteractor.observeCurrentVerseIndex().filter { it.isValid() }
                    .consumeEach { view?.onCurrentVerseIndexUpdated(it) }
        }
        coroutineScope.launch(Dispatchers.Main) {
            readingInteractor.observeDownloadedTranslations().consumeEach {
                if (it.isEmpty()) {
                    view?.onNoTranslationsDownloaded()
                } else {
                    view?.onDownloadedTranslationsLoaded(it.sortedWith(translationComparator))
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            readingInteractor.observeParallelTranslations().consumeEach { view?.onParallelTranslationsUpdated(it) }
        }
    }

    fun updateCurrentTranslation(translationShortName: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentTranslation(translationShortName)
                try {
                    readingInteractor.clearParallelTranslation()
                } catch (e: Exception) {
                    Log.w(tag, "Failed to clear parallel translation", e)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to update current translation", e)
                view?.onCurrentTranslationUpdateFailed(translationShortName)
            }
        }
    }

    fun requestParallelTranslation(translationShortName: String) {
        coroutineScope.launch(Dispatchers.Main) { readingInteractor.requestParallelTranslation(translationShortName) }
    }

    fun removeParallelTranslation(translationShortName: String) {
        coroutineScope.launch(Dispatchers.Main) { readingInteractor.removeParallelTranslation(translationShortName) }
    }

    fun openTranslationManagement() {
        try {
            readingInteractor.openTranslationManagement()
        } catch (e: Exception) {
            Log.e(tag, "Failed to open translation management activity", e)
            view?.onFailedToNavigateToTranslationManagement()
        }
    }

    fun openReadingProgress() {
        try {
            readingInteractor.openReadingProgress()
        } catch (e: Exception) {
            Log.e(tag, "Failed to open reading progress activity", e)
            view?.onFailedToNavigateToReadingProgress()
        }
    }

    fun openBookmarks() {
        try {
            readingInteractor.openBookmarks()
        } catch (e: Exception) {
            Log.e(tag, "Failed to open bookmarks activity", e)
            view?.onFailedToNavigateToBookmarks()
        }
    }

    fun openNotes() {
        try {
            readingInteractor.openNotes()
        } catch (e: Exception) {
            Log.e(tag, "Failed to open notes activity", e)
            view?.onFailedToNavigateToNotes()
        }
    }

    fun openSettings() {
        try {
            readingInteractor.openSettings()
        } catch (e: Exception) {
            Log.e(tag, "Failed to open settings activity", e)
            view?.onFailedToNavigateToSettings()
        }
    }

    fun finish() {
        readingInteractor.finish()
    }
}
