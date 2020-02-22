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

import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.reading.chapter.ChapterListPresenter
import me.xizzhu.android.joshua.reading.verse.VersePresenter
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.reading.chapter.ChapterListInteractor
import me.xizzhu.android.joshua.reading.detail.VerseDetailInteractor
import me.xizzhu.android.joshua.reading.detail.VerseDetailPresenter
import me.xizzhu.android.joshua.reading.search.SearchButtonInteractor
import me.xizzhu.android.joshua.reading.search.SearchButtonPresenter
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbarInteractor
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbarPresenter
import me.xizzhu.android.joshua.reading.verse.VerseInteractor

@Module
object ReadingModule {
    @ActivityScope
    @Provides
    fun provideReadingToolbarInteractor(bibleReadingManager: BibleReadingManager,
                                        translationManager: TranslationManager): ReadingToolbarInteractor =
            ReadingToolbarInteractor(bibleReadingManager, translationManager)

    @ActivityScope
    @Provides
    fun provideReadingToolbarPresenter(readingActivity: ReadingActivity,
                                       navigator: Navigator,
                                       readingViewModel: ReadingViewModel,
                                       readingToolbarInteractor: ReadingToolbarInteractor): ReadingToolbarPresenter =
            ReadingToolbarPresenter(readingActivity, navigator, readingViewModel, readingToolbarInteractor)

    @ActivityScope
    @Provides
    fun provideChapterListInteractor(bibleReadingManager: BibleReadingManager): ChapterListInteractor =
            ChapterListInteractor(bibleReadingManager)

    @ActivityScope
    @Provides
    fun provideChapterListPresenter(readingActivity: ReadingActivity,
                                    readingViewModel: ReadingViewModel,
                                    chapterListInteractor: ChapterListInteractor): ChapterListPresenter =
            ChapterListPresenter(readingActivity, readingViewModel, chapterListInteractor)

    @ActivityScope
    @Provides
    fun provideSearchButtonInteractor(settingsManager: SettingsManager): SearchButtonInteractor =
            SearchButtonInteractor(settingsManager)

    @ActivityScope
    @Provides
    fun provideSearchButtonPresenter(readingActivity: ReadingActivity, navigator: Navigator,
                                     readingViewModel: ReadingViewModel,
                                     searchButtonInteractor: SearchButtonInteractor): SearchButtonPresenter =
            SearchButtonPresenter(readingActivity, navigator, readingViewModel, searchButtonInteractor)

    @ActivityScope
    @Provides
    fun provideVerseInteractor(bibleReadingManager: BibleReadingManager,
                               bookmarkManager: VerseAnnotationManager<Bookmark>,
                               highlightManager: VerseAnnotationManager<Highlight>,
                               noteManager: VerseAnnotationManager<Note>,
                               settingsManager: SettingsManager): VerseInteractor =
            VerseInteractor(bibleReadingManager, bookmarkManager, highlightManager, noteManager, settingsManager)

    @ActivityScope
    @Provides
    fun provideVersePresenter(readingActivity: ReadingActivity,
                              readingViewModel: ReadingViewModel,
                              verseInteractor: VerseInteractor): VersePresenter =
            VersePresenter(readingActivity, readingViewModel, verseInteractor)

    @ActivityScope
    @Provides
    fun provideVerseDetailInteractor(translationManager: TranslationManager,
                                     bibleReadingManager: BibleReadingManager,
                                     bookmarkManager: VerseAnnotationManager<Bookmark>,
                                     highlightManager: VerseAnnotationManager<Highlight>,
                                     noteManager: VerseAnnotationManager<Note>,
                                     strongNumberManager: StrongNumberManager,
                                     settingsManager: SettingsManager): VerseDetailInteractor =
            VerseDetailInteractor(translationManager, bibleReadingManager, bookmarkManager,
                    highlightManager, noteManager, strongNumberManager, settingsManager)

    @ActivityScope
    @Provides
    fun provideVerseDetailPresenter(readingActivity: ReadingActivity,
                                    navigator: Navigator,
                                    readingViewModel: ReadingViewModel,
                                    verseDetailInteractor: VerseDetailInteractor): VerseDetailPresenter =
            VerseDetailPresenter(readingActivity, navigator, readingViewModel, verseDetailInteractor)

    @ActivityScope
    @Provides
    fun provideReadingViewModel(bibleReadingManager: BibleReadingManager,
                                readingProgressManager: ReadingProgressManager,
                                translationManager: TranslationManager,
                                bookmarkManager: VerseAnnotationManager<Bookmark>,
                                highlightManager: VerseAnnotationManager<Highlight>,
                                noteManager: VerseAnnotationManager<Note>,
                                strongNumberManager: StrongNumberManager,
                                settingsManager: SettingsManager): ReadingViewModel =
            ReadingViewModel(bibleReadingManager, readingProgressManager, translationManager,
                    bookmarkManager, highlightManager, noteManager, strongNumberManager, settingsManager)
}
