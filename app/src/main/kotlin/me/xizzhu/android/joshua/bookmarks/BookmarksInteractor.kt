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

package me.xizzhu.android.joshua.bookmarks

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.BookmarkManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.ui.LoadingSpinnerState
import me.xizzhu.android.joshua.utils.BaseSettingsInteractor

class BookmarksInteractor(private val bookmarksActivity: BookmarksActivity,
                          private val bibleReadingManager: BibleReadingManager,
                          private val bookmarkManager: BookmarkManager,
                          settingsManager: SettingsManager) : BaseSettingsInteractor(settingsManager) {
    private val bookmarksLoadingState: BroadcastChannel<LoadingSpinnerState> = ConflatedBroadcastChannel(LoadingSpinnerState.IS_LOADING)

    fun observeBookmarksLoadingState(): ReceiveChannel<LoadingSpinnerState> =
            bookmarksLoadingState.openSubscription()

    suspend fun notifyLoadingFinished() {
        bookmarksLoadingState.send(LoadingSpinnerState.NOT_LOADING)
    }
}
