/*
 * Copyright (C) 2020 Xizhi Zhu
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.search.result.SearchResultListPresenter
import me.xizzhu.android.joshua.search.toolbar.SearchToolbarPresenter

@Module
object SearchModule {
    @ActivityScope
    @Provides
    fun provideSearchToolbarPresenter(searchActivity: SearchActivity,
                                      searchViewModel: SearchViewModel): SearchToolbarPresenter =
            SearchToolbarPresenter(searchActivity, searchViewModel, searchActivity.lifecycleScope)

    @ActivityScope
    @Provides
    fun provideSearchResultListPresenter(searchActivity: SearchActivity,
                                         navigator: Navigator,
                                         searchViewModel: SearchViewModel): SearchResultListPresenter =
            SearchResultListPresenter(searchActivity, navigator, searchViewModel, searchActivity.lifecycleScope)

    @ActivityScope
    @Provides
    fun provideSearchViewModel(searchActivity: SearchActivity,
                               bibleReadingManager: BibleReadingManager,
                               settingsManager: SettingsManager): SearchViewModel {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                    return SearchViewModel(bibleReadingManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(searchActivity, factory).get(SearchViewModel::class.java)
    }
}
