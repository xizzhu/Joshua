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

package me.xizzhu.android.joshua.progress

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.utils.MVPPresenter

class ReadingProgressPresenter(private val readingProgressInteractor: ReadingProgressInteractor)
    : MVPPresenter<ReadingProgressView>() {
    companion object {
        private val TAG: String = ReadingProgressPresenter::class.java.simpleName
    }

    override fun onViewAttached() {
        super.onViewAttached()
        loadReadingProgress()
    }

    fun loadReadingProgress() {
        launch(Dispatchers.Main) {
            try {
                val currentTranslation = readingProgressInteractor.readCurrentTranslation()
                val bookNames = readingProgressInteractor.readBookNames(currentTranslation)
                val readingProgress = readingProgressInteractor.readReadingProgress()
                        .toReadingProgressForDisplay(bookNames)
                view?.onReadingProgressLoaded(readingProgress)

                readingProgressInteractor.notifyLoadingFinished()
            } catch (e: Exception) {
                Log.e(TAG, e, "Failed to load reading progress")
                view?.onReadingProgressLoadFailed()
            }
        }
    }
}
