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
import me.xizzhu.android.joshua.search.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.search.result.SearchResultPresenter
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.ui.LoadingAwarePresenter

@Module
class SearchModule {
    @ActivityScope
    @Provides
    fun provideSearchViewController(searchActivity: SearchActivity,
                                    navigator: Navigator,
                                    bibleReadingManager: BibleReadingManager,
                                    settingsManager: SettingsManager): SearchInteractor =
            SearchInteractor(searchActivity, navigator, bibleReadingManager, settingsManager)

    @Provides
    fun provideToolbarPresenter(searchInteractor: SearchInteractor): ToolbarPresenter =
            ToolbarPresenter(searchInteractor)

    @Provides
    fun provideLoadingAwarePresenter(searchInteractor: SearchInteractor): LoadingAwarePresenter =
            LoadingAwarePresenter(searchInteractor.observeLoadingState())

    @Provides
    fun provideSearchResultPresenter(searchInteractor: SearchInteractor): SearchResultPresenter =
            SearchResultPresenter(searchInteractor)
}
