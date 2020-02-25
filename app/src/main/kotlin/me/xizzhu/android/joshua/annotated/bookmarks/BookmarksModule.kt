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

package me.xizzhu.android.joshua.annotated.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesViewModel
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.bookmarks.list.BookmarksListPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager

@Module
object BookmarksModule {
    @ActivityScope
    @Provides
    fun provideToolbarPresenter(bookmarksViewModel: BaseAnnotatedVersesViewModel<Bookmark>,
                                bookmarksActivity: BookmarksActivity): AnnotatedVersesToolbarPresenter<Bookmark, BookmarksActivity> =
            AnnotatedVersesToolbarPresenter(R.string.title_bookmarks, bookmarksViewModel, bookmarksActivity)

    @ActivityScope
    @Provides
    fun provideBookmarksListPresenter(navigator: Navigator, bookmarksViewModel: BaseAnnotatedVersesViewModel<Bookmark>,
                                      bookmarksActivity: BookmarksActivity): BaseAnnotatedVersesPresenter<Bookmark, BookmarksActivity> =
            BookmarksListPresenter(navigator, bookmarksViewModel, bookmarksActivity)

    @ActivityScope
    @Provides
    fun provideBookmarksViewModel(bookmarksActivity: BookmarksActivity,
                                  bibleReadingManager: BibleReadingManager,
                                  bookmarksManager: VerseAnnotationManager<Bookmark>,
                                  settingsManager: SettingsManager): BaseAnnotatedVersesViewModel<Bookmark> {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
                    return BookmarksViewModel(bibleReadingManager, bookmarksManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(bookmarksActivity, factory).get(BookmarksViewModel::class.java)
    }
}
