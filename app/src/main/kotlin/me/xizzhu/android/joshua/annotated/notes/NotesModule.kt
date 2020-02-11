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

package me.xizzhu.android.joshua.annotated.notes

import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersesInteractor
import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewModel
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.notes.list.NotesListPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarInteractor
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager

@Module
object NotesModule {
    @ActivityScope
    @Provides
    fun provideAnnotatedVersesToolbarInteractor(noteManager: VerseAnnotationManager<Note>): AnnotatedVersesToolbarInteractor<Note> =
            AnnotatedVersesToolbarInteractor(noteManager)

    @ActivityScope
    @Provides
    fun provideSortOrderToolbarPresenter(annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor<Note>): AnnotatedVersesToolbarPresenter<Note> =
            AnnotatedVersesToolbarPresenter(R.string.title_notes, annotatedVersesToolbarInteractor)

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
                                  notesListInteractor: AnnotatedVersesInteractor<Note>): BaseAnnotatedVersesPresenter<Note, AnnotatedVersesInteractor<Note>> =
            NotesListPresenter(notesActivity, navigator, notesListInteractor)

    @ActivityScope
    @Provides
    fun provideNotesViewModel(settingsManager: SettingsManager,
                              annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor<Note>,
                              notesListInteractor: AnnotatedVersesInteractor<Note>): AnnotatedVersesViewModel<Note> =
            AnnotatedVersesViewModel(settingsManager, annotatedVersesToolbarInteractor, notesListInteractor)
}
