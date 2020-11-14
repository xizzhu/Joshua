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

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.search.result.SearchResultListPresenter
import me.xizzhu.android.joshua.search.toolbar.SearchToolbarPresenter

@Module
@InstallIn(ActivityComponent::class)
object SearchModule {
    @Provides
    fun provideSearchActivity(activity: Activity): SearchActivity = activity as SearchActivity

    @ActivityScoped
    @Provides
    fun provideSearchToolbarPresenter(searchViewModel: SearchViewModel,
                                      searchActivity: SearchActivity): SearchToolbarPresenter =
            SearchToolbarPresenter(searchViewModel, searchActivity)

    @ActivityScoped
    @Provides
    fun provideSearchResultListPresenter(navigator: Navigator, searchViewModel: SearchViewModel,
                                         searchActivity: SearchActivity): SearchResultListPresenter =
            SearchResultListPresenter(navigator, searchViewModel, searchActivity)

    @ActivityScoped
    @Provides
    fun provideSearchViewModel(searchActivity: SearchActivity,
                               bibleReadingManager: BibleReadingManager,
                               bookmarkManager: VerseAnnotationManager<Bookmark>,
                               highlightManager: VerseAnnotationManager<Highlight>,
                               noteManager: VerseAnnotationManager<Note>,
                               settingsManager: SettingsManager): SearchViewModel {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                    return SearchViewModel(bibleReadingManager, bookmarkManager, highlightManager, noteManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(searchActivity, factory).get(SearchViewModel::class.java)
    }
}
