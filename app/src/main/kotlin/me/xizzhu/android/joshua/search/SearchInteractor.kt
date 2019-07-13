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

package me.xizzhu.android.joshua.search

import kotlinx.coroutines.channels.*
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.LoadingSpinnerPresenter
import me.xizzhu.android.joshua.utils.activities.BaseSettingsInteractor

class SearchInteractor(private val searchActivity: SearchActivity,
                       private val navigator: Navigator,
                       private val bibleReadingManager: BibleReadingManager,
                       settingsManager: SettingsManager) : BaseSettingsInteractor(settingsManager) {
    private val searchState: BroadcastChannel<Int> = ConflatedBroadcastChannel(LoadingSpinnerPresenter.NOT_LOADING)
    private val searchResult: BroadcastChannel<Pair<String, List<Verse>>> = ConflatedBroadcastChannel(Pair("", emptyList()))

    fun observeSearchState(): ReceiveChannel<Int> = searchState.openSubscription()

    fun observeSearchResult(): ReceiveChannel<Pair<String, List<Verse>>> = searchResult.openSubscription()

    suspend fun selectVerse(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun search(query: String) {
        searchState.send(LoadingSpinnerPresenter.IS_LOADING)

        try {
            searchResult.send(Pair(query, bibleReadingManager.search(readCurrentTranslation(), query)))
        } finally {
            searchState.send(LoadingSpinnerPresenter.NOT_LOADING)
        }
    }

    suspend fun readCurrentTranslation(): String = bibleReadingManager.observeCurrentTranslation().first()

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookNames(translationShortName)

    fun openReading() {
        navigator.navigate(searchActivity, Navigator.SCREEN_READING)
    }
}
