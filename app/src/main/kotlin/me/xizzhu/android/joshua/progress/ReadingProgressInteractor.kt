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

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.first
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.ui.LoadingSpinnerState
import me.xizzhu.android.joshua.utils.BaseSettingsInteractor

class ReadingProgressInteractor(private val readingProgressManager: ReadingProgressManager,
                                private val bibleReadingManager: BibleReadingManager,
                                settingsManager: SettingsManager) : BaseSettingsInteractor(settingsManager) {
    private val readingProgressLoadingState: BroadcastChannel<LoadingSpinnerState> = ConflatedBroadcastChannel(LoadingSpinnerState.IS_LOADING)

    fun observeReadingProgressLoadingState(): ReceiveChannel<LoadingSpinnerState> =
            readingProgressLoadingState.openSubscription()

    suspend fun notifyLoadingFinished() {
        readingProgressLoadingState.send(LoadingSpinnerState.NOT_LOADING)
    }

    suspend fun readCurrentTranslation(): String = bibleReadingManager.observeCurrentTranslation().first()

    suspend fun readBookNames(translationShortName: String): List<String> = bibleReadingManager.readBookNames(translationShortName)

    suspend fun readReadingProgress(): ReadingProgress = readingProgressManager.readReadingProgress()
}
