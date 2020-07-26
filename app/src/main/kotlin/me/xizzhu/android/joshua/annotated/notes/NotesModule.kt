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

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesViewModel
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.notes.list.NotesListPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager

@Module
@InstallIn(ActivityComponent::class)
object NotesModule {
    @Provides
    fun provideNotesActivity(activity: Activity): NotesActivity = activity as NotesActivity

    @ActivityScoped
    @Provides
    fun provideToolbarPresenter(notesViewModel: BaseAnnotatedVersesViewModel<Note>,
                                notesActivity: NotesActivity): AnnotatedVersesToolbarPresenter<Note, NotesActivity> =
            AnnotatedVersesToolbarPresenter(R.string.title_notes, notesViewModel, notesActivity)

    @ActivityScoped
    @Provides
    fun provideNotesListPresenter(navigator: Navigator, notesViewModel: BaseAnnotatedVersesViewModel<Note>,
                                  notesActivity: NotesActivity): BaseAnnotatedVersesPresenter<Note, NotesActivity> =
            NotesListPresenter(navigator, notesViewModel, notesActivity)

    @ActivityScoped
    @Provides
    fun provideNotesViewModel(notesActivity: NotesActivity,
                              bibleReadingManager: BibleReadingManager,
                              notesManager: VerseAnnotationManager<Note>,
                              settingsManager: SettingsManager): BaseAnnotatedVersesViewModel<Note> {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                    return NotesViewModel(bibleReadingManager, notesManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(notesActivity, factory).get(NotesViewModel::class.java)
    }
}
