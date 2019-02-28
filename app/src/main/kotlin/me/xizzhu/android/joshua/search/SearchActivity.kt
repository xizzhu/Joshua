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

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.search.toolbar.SearchToolbar
import me.xizzhu.android.joshua.search.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.search.result.SearchResultPresenter
import me.xizzhu.android.joshua.search.result.SearchResultListView
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.fadeOut
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.onEach
import javax.inject.Inject

class SearchActivity : BaseActivity() {
    @Inject
    lateinit var searchViewController: SearchViewController

    @Inject
    lateinit var toolbarPresenter: ToolbarPresenter

    @Inject
    lateinit var searchResultPresenter: SearchResultPresenter

    private lateinit var toolbar: SearchToolbar
    private lateinit var searchResultList: SearchResultListView
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setPresenter(toolbarPresenter)

        searchResultList = findViewById(R.id.search_result)
        searchResultList.setPresenter(searchResultPresenter)

        loadingSpinner = findViewById(R.id.loading_spinner)
    }

    override fun onStart() {
        super.onStart()

        toolbarPresenter.attachView(toolbar)
        searchResultPresenter.attachView(searchResultList)

        launch(Dispatchers.Main) {
            receiveChannels.add(searchViewController.observeSearchState()
                    .onEach { searching ->
                        if (searching) {
                            loadingSpinner.visibility = View.VISIBLE
                            searchResultList.visibility = View.GONE
                        } else {
                            loadingSpinner.fadeOut()
                            searchResultList.fadeIn()
                        }
                    })
        }
    }

    override fun onStop() {
        toolbarPresenter.detachView()
        searchResultPresenter.detachView()

        super.onStop()
    }
}
