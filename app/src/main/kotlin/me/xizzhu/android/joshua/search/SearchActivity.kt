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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.search.toolbar.SearchToolbar
import me.xizzhu.android.joshua.search.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.search.verse.VerseListView
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.fadeOut
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.onEach
import javax.inject.Inject

class SearchActivity : BaseActivity() {
    companion object {
        fun newStartIntent(context: Context) = Intent(context, SearchActivity::class.java)
    }

    @Inject
    lateinit var searchManager: SearchManager

    @Inject
    lateinit var toolbarPresenter: ToolbarPresenter

    private lateinit var toolbar: SearchToolbar
    private lateinit var verseList: VerseListView
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setPresenter(toolbarPresenter)

        verseList = findViewById(R.id.verse_list)

        loadingSpinner = findViewById(R.id.loading_spinner)
    }

    override fun onStart() {
        super.onStart()

        toolbarPresenter.attachView(toolbar)

        launch(Dispatchers.Main) {
            receiveChannels.add(searchManager.observeSearchState()
                    .onEach { searching ->
                        if (searching) {
                            loadingSpinner.visibility = View.VISIBLE
                            verseList.visibility = View.GONE
                        } else {
                            loadingSpinner.fadeOut()
                            verseList.fadeIn()
                        }
                    })
        }
    }

    override fun onStop() {
        toolbarPresenter.detachView()

        super.onStop()
    }
}
