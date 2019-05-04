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

package me.xizzhu.android.joshua.search.result

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.search.SearchInteractor
import me.xizzhu.android.joshua.ui.LoadingSpinnerState
import me.xizzhu.android.joshua.ui.recyclerview.toSearchItems
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter

class SearchResultPresenter(private val searchInteractor: SearchInteractor)
    : BaseSettingsPresenter<SearchResultView>(searchInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            searchInteractor.observeSearchResult().consumeEach { (query, verses) ->
                view?.onSearchResultUpdated(verses.toSearchItems(query))
            }
        }
        launch(Dispatchers.Main) {
            searchInteractor.observeSearchState().consumeEach { loadingState ->
                when (loadingState) {
                    LoadingSpinnerState.IS_LOADING -> view?.onSearchStarted()
                    LoadingSpinnerState.NOT_LOADING -> view?.onSearchCompleted()
                }
            }
        }
    }

    fun selectVerse(verseToSelect: VerseIndex) {
        launch(Dispatchers.Main) {
            try {
                searchInteractor.selectVerse(verseToSelect)
                searchInteractor.openReading()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to select verse and open reading activity")
                view?.onVerseSelectionFailed(verseToSelect)
            }
        }
    }
}
