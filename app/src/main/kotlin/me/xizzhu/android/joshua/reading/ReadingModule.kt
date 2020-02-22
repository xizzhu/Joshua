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

package me.xizzhu.android.joshua.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.reading.chapter.ChapterListPresenter
import me.xizzhu.android.joshua.reading.verse.VersePresenter
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.reading.detail.VerseDetailPresenter
import me.xizzhu.android.joshua.reading.search.SearchButtonPresenter
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbarPresenter

@Module
object ReadingModule {
    @ActivityScope
    @Provides
    fun provideReadingToolbarPresenter(navigator: Navigator, readingViewModel: ReadingViewModel,
                                       readingActivity: ReadingActivity): ReadingToolbarPresenter =
            ReadingToolbarPresenter(navigator, readingViewModel, readingActivity)

    @ActivityScope
    @Provides
    fun provideChapterListPresenter(readingViewModel: ReadingViewModel, readingActivity: ReadingActivity): ChapterListPresenter =
            ChapterListPresenter(readingViewModel, readingActivity)

    @ActivityScope
    @Provides
    fun provideSearchButtonPresenter(navigator: Navigator, readingViewModel: ReadingViewModel,
                                     readingActivity: ReadingActivity): SearchButtonPresenter =
            SearchButtonPresenter(navigator, readingViewModel, readingActivity)

    @ActivityScope
    @Provides
    fun provideVersePresenter(readingViewModel: ReadingViewModel, readingActivity: ReadingActivity): VersePresenter =
            VersePresenter(readingViewModel, readingActivity)

    @ActivityScope
    @Provides
    fun provideVerseDetailPresenter(navigator: Navigator, readingViewModel: ReadingViewModel,
                                    readingActivity: ReadingActivity): VerseDetailPresenter =
            VerseDetailPresenter(navigator, readingViewModel, readingActivity)

    @ActivityScope
    @Provides
    fun provideReadingViewModel(readingActivity: ReadingActivity,
                                bibleReadingManager: BibleReadingManager,
                                readingProgressManager: ReadingProgressManager,
                                translationManager: TranslationManager,
                                bookmarkManager: VerseAnnotationManager<Bookmark>,
                                highlightManager: VerseAnnotationManager<Highlight>,
                                noteManager: VerseAnnotationManager<Note>,
                                strongNumberManager: StrongNumberManager,
                                settingsManager: SettingsManager): ReadingViewModel {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReadingViewModel::class.java)) {
                    return ReadingViewModel(bibleReadingManager, readingProgressManager, translationManager,
                            bookmarkManager, highlightManager, noteManager, strongNumberManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(readingActivity, factory).get(ReadingViewModel::class.java)
    }
}
