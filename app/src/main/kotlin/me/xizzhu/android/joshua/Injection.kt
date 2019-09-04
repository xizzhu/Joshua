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
import me.xizzhu.android.joshua.core.repository.local.*
import me.xizzhu.android.joshua.core.repository.local.android.*
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.annotated.highlights.HighlightsModule
import me.xizzhu.android.joshua.annotated.notes.NotesActivity
import me.xizzhu.android.joshua.annotated.notes.NotesModule
import me.xizzhu.android.joshua.core.repository.remote.http.HttpTranslationService
import me.xizzhu.android.joshua.core.serializer.android.BackupJsonWriterSerializer
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
import java.io.Writer
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
        @JvmStatic
        @Provides
        @Singleton
        fun provideNavigator(): Navigator = Navigator()

        @JvmStatic
        @Provides
        @Singleton
        fun provideBackupManager(bookmarkManager: BookmarkManager,
                                 highlightManager: HighlightManager,
                                 noteManager: NoteManager,
                                 readingProgressManager: ReadingProgressManager): BackupManager =
                BackupManager({ BackupJsonWriterSerializer() }, bookmarkManager, highlightManager, noteManager, readingProgressManager)

        @JvmStatic
        @Provides
        @Singleton
        fun provideBibleReadingManager(bibleReadingRepository: BibleReadingRepository,
                                       translationManager: TranslationManager): BibleReadingManager =
                BibleReadingManager(bibleReadingRepository, translationManager)

        @JvmStatic
        @Provides
        @Singleton
        fun provideBookmarkManager(bookmarkRepository: BookmarkRepository): BookmarkManager =
                BookmarkManager(bookmarkRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideHighlightManager(highlightRepository: HighlightRepository): HighlightManager =
                HighlightManager(highlightRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideNoteManager(noteRepository: NoteRepository): NoteManager =
                NoteManager(noteRepository)

        @JvmStatic
        @Provides
        @Singleton
        fun provideReadingProgressManager(bibleReadingManager: BibleReadingManager,
                                          readingProgressRepository: ReadingProgressRepository): ReadingProgressManager =
                ReadingProgressManager(bibleReadingManager, readingProgressRepository)

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
    @JvmStatic
    @Provides
    @Singleton
    fun provideAndroidDatabase(app: App): AndroidDatabase = AndroidDatabase(app)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalBookmarkStorage(androidDatabase: AndroidDatabase): LocalBookmarkStorage =
            AndroidBookmarkStorage(androidDatabase)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalHighlightStorage(androidDatabase: AndroidDatabase): LocalHighlightStorage =
            AndroidHighlightStorage(androidDatabase)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalNoteStorage(androidDatabase: AndroidDatabase): LocalNoteStorage =
            AndroidNoteStorage(androidDatabase)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalReadingProgressStorage(androidDatabase: AndroidDatabase): LocalReadingProgressStorage =
            AndroidReadingProgressStorage(androidDatabase)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalReadingStorage(androidDatabase: AndroidDatabase): LocalReadingStorage =
            AndroidReadingStorage(androidDatabase)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalSettingsStorage(androidDatabase: AndroidDatabase): LocalSettingsStorage =
            AndroidSettingsStorage(androidDatabase)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalTranslationStorage(androidDatabase: AndroidDatabase): LocalTranslationStorage =
            AndroidTranslationStorage(androidDatabase)

    @JvmStatic
    @Provides
    @Singleton
    fun provideRemoteTranslationService(): RemoteTranslationService = HttpTranslationService()

    @JvmStatic
    @Provides
    @Singleton
    fun provideBibleReadingRepository(localReadingStorage: LocalReadingStorage): BibleReadingRepository =
            BibleReadingRepository(localReadingStorage)

    @JvmStatic
    @Provides
    @Singleton
    fun provideBookmarkRepository(localBookmarkStorage: LocalBookmarkStorage): BookmarkRepository =
            BookmarkRepository(localBookmarkStorage)

    @JvmStatic
    @Provides
    @Singleton
    fun provideHighlightRepository(localHighlightStorage: LocalHighlightStorage): HighlightRepository =
            HighlightRepository(localHighlightStorage)

    @JvmStatic
    @Provides
    @Singleton
    fun provideNoteRepository(localNoteStorage: LocalNoteStorage): NoteRepository =
            NoteRepository(localNoteStorage)

    @JvmStatic
    @Provides
    @Singleton
    fun provideReadingProgressRepository(localReadingProgressStorage: LocalReadingProgressStorage): ReadingProgressRepository =
            ReadingProgressRepository(localReadingProgressStorage)

    @JvmStatic
    @Provides
    @Singleton
    fun provideSettingsRepository(localSettingsStorage: LocalSettingsStorage): SettingsRepository =
            SettingsRepository(localSettingsStorage)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTranslationRepository(localAndroidStorage: LocalTranslationStorage,
                                     remoteTranslationService: RemoteTranslationService): TranslationRepository =
            TranslationRepository(localAndroidStorage, remoteTranslationService)
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
