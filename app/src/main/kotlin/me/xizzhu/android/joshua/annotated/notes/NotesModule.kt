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

package me.xizzhu.android.joshua.annotated.notes

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersesInteractor
import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewModel
import me.xizzhu.android.joshua.annotated.notes.list.NotesListPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarInteractor
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerInteractor
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerPresenter

@Module
object NotesModule {
    @ActivityScope
    @Provides
    fun provideAnnotatedVersesToolbarInteractor(noteManager: VerseAnnotationManager<Note>): AnnotatedVersesToolbarInteractor =
            AnnotatedVersesToolbarInteractor({ noteManager.observeSortOrder().first() }, noteManager::saveSortOrder)

    @ActivityScope
    @Provides
    fun provideSortOrderToolbarPresenter(annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor): AnnotatedVersesToolbarPresenter =
            AnnotatedVersesToolbarPresenter(R.string.title_notes, annotatedVersesToolbarInteractor)

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerInteractor(): LoadingSpinnerInteractor = LoadingSpinnerInteractor()

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerPresenter(loadingSpinnerInteractor: LoadingSpinnerInteractor): LoadingSpinnerPresenter =
            LoadingSpinnerPresenter(loadingSpinnerInteractor)

    @ActivityScope
    @Provides
    fun provideNotesListInteractor(noteManager: VerseAnnotationManager<Note>,
                                   bibleReadingManager: BibleReadingManager,
                                   settingsManager: SettingsManager): AnnotatedVersesInteractor<Note> =
            AnnotatedVersesInteractor(noteManager, bibleReadingManager, settingsManager)

    @ActivityScope
    @Provides
    fun provideNotesListPresenter(notesActivity: NotesActivity,
                                  navigator: Navigator,
                                  notesListInteractor: AnnotatedVersesInteractor<Note>): NotesListPresenter =
            NotesListPresenter(notesActivity, navigator, notesListInteractor)

    @ActivityScope
    @Provides
    fun provideNotesViewModel(settingsManager: SettingsManager,
                              annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor,
                              loadingSpinnerInteractor: LoadingSpinnerInteractor,
                              notesListInteractor: AnnotatedVersesInteractor<Note>): AnnotatedVersesViewModel<Note> =
            AnnotatedVersesViewModel(settingsManager, annotatedVersesToolbarInteractor, loadingSpinnerInteractor, notesListInteractor)
}
