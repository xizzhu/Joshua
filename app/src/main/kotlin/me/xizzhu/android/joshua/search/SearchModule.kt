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

import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerInteractor
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerPresenter
import me.xizzhu.android.joshua.search.result.SearchResultInteractor
import me.xizzhu.android.joshua.search.result.SearchResultListPresenter
import me.xizzhu.android.joshua.search.toolbar.SearchToolbarInteractor
import me.xizzhu.android.joshua.search.toolbar.SearchToolbarPresenter

@Module
object SearchModule {
    @ActivityScope
    @Provides
    fun provideSearchToolbarInteractor(): SearchToolbarInteractor = SearchToolbarInteractor()

    @ActivityScope
    @Provides
    fun provideSearchToolbarPresenter(searchToolbarInteractor: SearchToolbarInteractor): SearchToolbarPresenter =
            SearchToolbarPresenter(searchToolbarInteractor)

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerInteractor(): LoadingSpinnerInteractor = LoadingSpinnerInteractor()

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerPresenter(loadingSpinnerInteractor: LoadingSpinnerInteractor): LoadingSpinnerPresenter =
            LoadingSpinnerPresenter(loadingSpinnerInteractor)

    @ActivityScope
    @Provides
    fun provideSearchResultInteractor(bibleReadingManager: BibleReadingManager,
                                      settingsManager: SettingsManager): SearchResultInteractor =
            SearchResultInteractor(bibleReadingManager, settingsManager)

    @ActivityScope
    @Provides
    fun provideSearchResultListPresenter(searchActivity: SearchActivity,
                                         navigator: Navigator,
                                         searchResultInteractor: SearchResultInteractor): SearchResultListPresenter =
            SearchResultListPresenter(searchActivity, navigator, searchResultInteractor)

    @ActivityScope
    @Provides
    fun provideSearchViewModel(settingsManager: SettingsManager,
                               searchToolbarInteractor: SearchToolbarInteractor,
                               loadingSpinnerInteractor: LoadingSpinnerInteractor,
                               searchResultInteractor: SearchResultInteractor): SearchViewModel =
            SearchViewModel(settingsManager, searchToolbarInteractor, loadingSpinnerInteractor, searchResultInteractor)
}
