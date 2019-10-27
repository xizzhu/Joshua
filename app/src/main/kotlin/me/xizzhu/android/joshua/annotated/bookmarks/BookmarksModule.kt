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

package me.xizzhu.android.joshua.annotated.bookmarks

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.list.BookmarksListInteractor
import me.xizzhu.android.joshua.annotated.bookmarks.list.BookmarksListPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarInteractor
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.BookmarkManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerInteractor
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerPresenter

@Module
object BookmarksModule {
    @ActivityScope
    @Provides
    fun provideAnnotatedVersesToolbarInteractor(bookmarkManager: BookmarkManager): AnnotatedVersesToolbarInteractor =
            AnnotatedVersesToolbarInteractor({ bookmarkManager.observeSortOrder().first() }, bookmarkManager::saveSortOrder)

    @ActivityScope
    @Provides
    fun provideSortOrderToolbarPresenter(annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor): AnnotatedVersesToolbarPresenter =
            AnnotatedVersesToolbarPresenter(R.string.title_bookmarks, annotatedVersesToolbarInteractor)

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerInteractor(): LoadingSpinnerInteractor = LoadingSpinnerInteractor()

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerPresenter(loadingSpinnerInteractor: LoadingSpinnerInteractor): LoadingSpinnerPresenter =
            LoadingSpinnerPresenter(loadingSpinnerInteractor)

    @ActivityScope
    @Provides
    fun provideBookmarksListInteractor(bookmarkManager: BookmarkManager,
                                       bibleReadingManager: BibleReadingManager,
                                       settingsManager: SettingsManager): BookmarksListInteractor =
            BookmarksListInteractor(bookmarkManager, bibleReadingManager, settingsManager)

    @ActivityScope
    @Provides
    fun provideBookmarksListPresenter(bookmarksActivity: BookmarksActivity,
                                      navigator: Navigator,
                                      bookmarksListInteractor: BookmarksListInteractor): BookmarksListPresenter =
            BookmarksListPresenter(bookmarksActivity, navigator, bookmarksListInteractor)

    @ActivityScope
    @Provides
    fun provideBookmarksViewModel(settingsManager: SettingsManager,
                                  annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor,
                                  loadingSpinnerInteractor: LoadingSpinnerInteractor,
                                  bookmarksListInteractor: BookmarksListInteractor): BookmarksViewModel =
            BookmarksViewModel(settingsManager, annotatedVersesToolbarInteractor, loadingSpinnerInteractor, bookmarksListInteractor)
}
