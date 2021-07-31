/*
 * Copyright (C) 2021 Xizhi Zhu
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
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.activity.BaseSettingsActivity
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.search.result.SearchResultListPresenter
import me.xizzhu.android.joshua.search.result.SearchResultViewHolder
import me.xizzhu.android.joshua.search.toolbar.SearchToolbarPresenter
import me.xizzhu.android.joshua.search.toolbar.SearchToolbarViewHolder
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : BaseSettingsActivity() {
    @Inject
    lateinit var searchViewModel: SearchViewModel

    @Inject
    lateinit var searchToolbarPresenter: SearchToolbarPresenter

    @Inject
    lateinit var searchResultListPresenter: SearchResultListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)
        searchToolbarPresenter.bind(SearchToolbarViewHolder(findViewById(R.id.toolbar)))
        searchResultListPresenter.bind(
                SearchResultViewHolder(findViewById(R.id.loading_spinner), findViewById(R.id.search_result))
        )
    }

    override fun getBaseSettingsViewModel(): BaseSettingsViewModel = searchViewModel
}
