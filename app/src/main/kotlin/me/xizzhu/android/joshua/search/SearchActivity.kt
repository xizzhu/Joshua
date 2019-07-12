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
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.search.toolbar.SearchToolbar
import me.xizzhu.android.joshua.search.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.search.result.SearchResultPresenter
import me.xizzhu.android.joshua.search.result.SearchResultListView
import me.xizzhu.android.joshua.ui.LoadingSpinner
import me.xizzhu.android.joshua.ui.LoadingSpinnerPresenter
import me.xizzhu.android.joshua.ui.bindView
import me.xizzhu.android.joshua.utils.BaseSettingsActivity
import me.xizzhu.android.joshua.utils.BaseSettingsInteractor
import javax.inject.Inject

class SearchActivity : BaseSettingsActivity() {
    @Inject
    lateinit var searchInteractor: SearchInteractor

    @Inject
    lateinit var toolbarPresenter: ToolbarPresenter

    @Inject
    lateinit var loadingSpinnerPresenter: LoadingSpinnerPresenter

    @Inject
    lateinit var searchResultPresenter: SearchResultPresenter

    private val toolbar: SearchToolbar by bindView(R.id.toolbar)
    private val searchResultList: SearchResultListView by bindView(R.id.search_result)
    private val loadingSpinner: LoadingSpinner by bindView(R.id.loading_spinner)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)
        toolbar.setPresenter(toolbarPresenter)
        searchResultList.setPresenter(searchResultPresenter)
    }

    override fun onStart() {
        super.onStart()

        toolbarPresenter.attachView(toolbar)
        loadingSpinnerPresenter.attachView(loadingSpinner)
        searchResultPresenter.attachView(searchResultList)
    }

    override fun onStop() {
        toolbarPresenter.detachView()
        loadingSpinnerPresenter.detachView()
        searchResultPresenter.detachView()

        super.onStop()
    }

    override fun getBaseSettingsInteractor(): BaseSettingsInteractor = searchInteractor
}
