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

package me.xizzhu.android.joshua

import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarksActivity
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarksModule
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.core.repository.*
import me.xizzhu.android.joshua.core.repository.local.android.*
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.annotated.highlights.HighlightsModule
import me.xizzhu.android.joshua.annotated.notes.NotesActivity
import me.xizzhu.android.joshua.annotated.notes.NotesModule
import me.xizzhu.android.joshua.core.repository.remote.android.HttpTranslationService
import me.xizzhu.android.joshua.core.serializer.android.BackupJsonSerializer
import me.xizzhu.android.joshua.progress.ReadingProgressActivity
import me.xizzhu.android.joshua.progress.ReadingProgressModule
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingModule
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchModule
import me.xizzhu.android.joshua.settings.SettingsActivity
import me.xizzhu.android.joshua.settings.SettingsModule
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.translations.TranslationManagementModule
import javax.inject.Scope
import javax.inject.Singleton

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Module
class AppModule(private val app: App) {
    @Provides
    @Singleton
    fun provideApp(): App = app

    @Module
    companion object {
        // TODO @JvmStatic is still needed in companion objects https://github.com/google/dagger/issues/1646

        @JvmStatic
        @Provides
        @Singleton
        fun provideNavigator(): Navigator = Navigator()

        @JvmStatic
        @Provides
        @Singleton
        fun provideBackupManager(bookmarkRepository: VerseAnnotationRepository<Bookmark>,
                                 highlightRepository: VerseAnnotationRepository<Highlight>,
                                 noteRepository: VerseAnnotationRepository<Note>,
                                 readingProgressRepository: ReadingProgressRepository): BackupManager =
                BackupManager(BackupJsonSerializer(), bookmarkRepository, highlightRepository, noteRepository, readingProgressRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideBibleReadingManager(bibleReadingRepository: BibleReadingRepository,
                                       translationRepository: TranslationRepository): BibleReadingManager =
                BibleReadingManager(bibleReadingRepository, translationRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideBookmarkManager(bookmarkRepository: VerseAnnotationRepository<Bookmark>): VerseAnnotationManager<Bookmark> =
                VerseAnnotationManager(bookmarkRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideHighlightManager(highlightRepository: VerseAnnotationRepository<Highlight>): VerseAnnotationManager<Highlight> =
                VerseAnnotationManager(highlightRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideNoteManager(noteRepository: VerseAnnotationRepository<Note>): VerseAnnotationManager<Note> =
                VerseAnnotationManager(noteRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideReadingProgressManager(bibleReadingRepository: BibleReadingRepository,
                                          readingProgressRepository: ReadingProgressRepository): ReadingProgressManager =
                ReadingProgressManager(bibleReadingRepository, readingProgressRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideSettingsManager(settingsRepository: SettingsRepository): SettingsManager =
                SettingsManager(settingsRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideTranslationManager(translationRepository: TranslationRepository): TranslationManager =
                TranslationManager(translationRepository)
    }
}

@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAndroidDatabase(app: App): AndroidDatabase = AndroidDatabase(app)

    @Provides
    @Singleton
    fun provideBibleReadingRepository(androidDatabase: AndroidDatabase): BibleReadingRepository =
            BibleReadingRepository(AndroidReadingStorage(androidDatabase))

    @Provides
    @Singleton
    fun provideBookmarkRepository(androidDatabase: AndroidDatabase): VerseAnnotationRepository<Bookmark> =
            VerseAnnotationRepository(AndroidBookmarkStorage(androidDatabase))

    @Provides
    @Singleton
    fun provideHighlightRepository(androidDatabase: AndroidDatabase): VerseAnnotationRepository<Highlight> =
            VerseAnnotationRepository(AndroidHighlightStorage(androidDatabase))

    @Provides
    @Singleton
    fun provideNoteRepository(androidDatabase: AndroidDatabase): VerseAnnotationRepository<Note> =
            VerseAnnotationRepository(AndroidNoteStorage(androidDatabase))

    @Provides
    @Singleton
    fun provideReadingProgressRepository(androidDatabase: AndroidDatabase): ReadingProgressRepository =
            ReadingProgressRepository(AndroidReadingProgressStorage(androidDatabase))

    @Provides
    @Singleton
    fun provideSettingsRepository(androidDatabase: AndroidDatabase): SettingsRepository =
            SettingsRepository(AndroidSettingsStorage(androidDatabase))

    @Provides
    @Singleton
    fun provideTranslationRepository(androidDatabase: AndroidDatabase): TranslationRepository =
            TranslationRepository(AndroidTranslationStorage(androidDatabase), HttpTranslationService())
}

@Module
abstract class ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [(BookmarksModule::class)])
    abstract fun contributeBookmarksActivity(): BookmarksActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(HighlightsModule::class)])
    abstract fun contributeHighlightsActivity(): HighlightsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(NotesModule::class)])
    abstract fun contributeNotesActivity(): NotesActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(ReadingProgressModule::class)])
    abstract fun contributeReadingProgressActivity(): ReadingProgressActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(ReadingModule::class)])
    abstract fun contributeReadingActivity(): ReadingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(SearchModule::class)])
    abstract fun contributeSearchActivity(): SearchActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(SettingsModule::class)])
    abstract fun contributeSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(TranslationManagementModule::class)])
    abstract fun contributeTranslationManagementActivity(): TranslationManagementActivity
}

@Singleton
@Component(modules = [(AppModule::class), (RepositoryModule::class), (ActivityModule::class),
    (AndroidInjectionModule::class), (AndroidSupportInjectionModule::class)])
interface AppComponent {
    fun inject(app: App)
}
