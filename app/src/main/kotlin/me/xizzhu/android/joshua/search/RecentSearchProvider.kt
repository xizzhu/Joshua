/*
 * Copyright (C) 2023 Xizhi Zhu
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
import android.content.SearchRecentSuggestionsProvider
import android.provider.SearchRecentSuggestions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.BuildConfig
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider

class RecentSearchProvider : SearchRecentSuggestionsProvider() {
    class SearchSuggestions(context: Context, private val scope: CoroutineScope, private val coroutineDispatcherProvider: CoroutineDispatcherProvider) {
        private val searchRecentSuggestions: SearchRecentSuggestions = SearchRecentSuggestions(context, AUTHORITY, MODE)

        fun saveRecentQuery(query: String) {
            if (query.isNotBlank()) {
                searchRecentSuggestions.saveRecentQuery(query, null)
            }
        }

        fun clearHistory() {
            scope.launch(coroutineDispatcherProvider.io) { searchRecentSuggestions.clearHistory() }
        }
    }

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.search.RecentSearchProvider"
        private const val MODE = DATABASE_MODE_QUERIES
    }

    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}
