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
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.search.SearchViewController
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.onEach

class SearchResultPresenter(private val searchViewController: SearchViewController) : MVPPresenter<SearchResultView>() {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            receiveChannels.add(searchViewController.observeSearchResult()
                    .filter { it.isValid() }
                    .onEach { view?.onSearchResultUpdated(it) })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(searchViewController.observeSearchState()
                    .onEach { loading ->
                        if (loading) {
                            view?.onSearchStarted()
                        } else {
                            view?.onSearchCompleted()
                        }
                    })
        }
    }

    fun selectVerse(verseIndex: VerseIndex) {
        launch(Dispatchers.Main) {
            searchViewController.selectVerse(verseIndex)
            searchViewController.openReading()
        }
    }
}
