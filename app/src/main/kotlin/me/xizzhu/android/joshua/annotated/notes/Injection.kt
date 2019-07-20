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
import kotlinx.coroutines.channels.first
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.NoteManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.annotated.notes.list.NotesPresenter
import me.xizzhu.android.joshua.ui.LoadingAwarePresenter
import me.xizzhu.android.joshua.annotated.AnnotatedVersesToolbarPresenter

@Module
class NotesModule {
    @Provides
    @ActivityScope
    fun provideNotesInteractor(notesActivity: NotesActivity,
                               bibleReadingManager: BibleReadingManager,
                               noteManager: NoteManager,
                               navigator: Navigator,
                               settingsManager: SettingsManager): NotesInteractor =
            NotesInteractor(notesActivity, bibleReadingManager, noteManager, navigator, settingsManager)

    @Provides
    fun provideLoadingAwarePresenter(notesInteractor: NotesInteractor): LoadingAwarePresenter =
            LoadingAwarePresenter(notesInteractor.observeLoadingState())

    @Provides
    fun provideSortOrderToolbarPresenter(notesInteractor: NotesInteractor): AnnotatedVersesToolbarPresenter =
            AnnotatedVersesToolbarPresenter({ notesInteractor.observeSortOrder().first() },
                    notesInteractor::saveNotesSortOrder)

    @Provides
    fun provideNotesPresenter(notesActivity: NotesActivity,
                              notesInteractor: NotesInteractor): NotesPresenter =
            NotesPresenter(notesInteractor, notesActivity.resources)
}
