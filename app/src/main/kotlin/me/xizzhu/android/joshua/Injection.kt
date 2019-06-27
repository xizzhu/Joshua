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

import com.squareup.moshi.Moshi
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import me.xizzhu.android.joshua.bookmarks.BookmarksActivity
import me.xizzhu.android.joshua.bookmarks.BookmarksModule
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.core.repository.*
import me.xizzhu.android.joshua.core.repository.local.*
import me.xizzhu.android.joshua.core.repository.local.android.*
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.core.repository.remote.retrofit.RetrofitTranslationService
import me.xizzhu.android.joshua.notes.NotesActivity
import me.xizzhu.android.joshua.notes.NotesModule
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
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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

    @Provides
    @Singleton
    fun provideNavigator(): Navigator = Navigator()

    @Provides
    @Singleton
    fun provideBibleReadingManager(bibleReadingRepository: BibleReadingRepository): BibleReadingManager =
            BibleReadingManager(bibleReadingRepository)

    @Provides
    @Singleton
    fun provideBookmarkManager(bookmarkRepository: BookmarkRepository): BookmarkManager =
            BookmarkManager(bookmarkRepository)

    @Provides
    @Singleton
    fun provideHighlightManager(highlightRepository: HighlightRepository): HighlightManager =
            HighlightManager(highlightRepository)

    @Provides
    @Singleton
    fun provideNoteManager(noteRepository: NoteRepository): NoteManager =
            NoteManager(noteRepository)

    @Provides
    @Singleton
    fun provideReadingProgressManager(bibleReadingManager: BibleReadingManager,
                                      readingProgressRepository: ReadingProgressRepository): ReadingProgressManager =
            ReadingProgressManager(bibleReadingManager, readingProgressRepository)

    @Provides
    @Singleton
    fun provideSettingsManager(settingsRepository: SettingsRepository): SettingsManager =
            SettingsManager(settingsRepository)

    @Provides
    @Singleton
    fun provideTranslationManager(translationRepository: TranslationRepository): TranslationManager =
            TranslationManager(translationRepository)
}

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideAndroidDatabase(app: App): AndroidDatabase = AndroidDatabase(app)

    @Provides
    @Singleton
    fun provideLocalBookmarkStorage(androidDatabase: AndroidDatabase): LocalBookmarkStorage =
            AndroidBookmarkStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideLocalHighlightStorage(androidDatabase: AndroidDatabase): LocalHighlightStorage =
            AndroidHighlightStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideLocalNoteStorage(androidDatabase: AndroidDatabase): LocalNoteStorage =
            AndroidNoteStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideLocalReadingProgressStorage(androidDatabase: AndroidDatabase): LocalReadingProgressStorage =
            AndroidReadingProgressStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideLocalReadingStorage(androidDatabase: AndroidDatabase): LocalReadingStorage =
            AndroidReadingStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideLocalSettingsStorage(androidDatabase: AndroidDatabase): LocalSettingsStorage =
            AndroidSettingsStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideLocalTranslationStorage(androidDatabase: AndroidDatabase): LocalTranslationStorage =
            AndroidTranslationStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                    .connectTimeout(RetrofitTranslationService.OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(RetrofitTranslationService.OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(RetrofitTranslationService.OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .build()

    @Provides
    @Singleton
    fun provideRemoteTranslationService(moshi: Moshi, okHttpClient: OkHttpClient): RemoteTranslationService =
            RetrofitTranslationService(moshi, okHttpClient)

    @Provides
    @Singleton
    fun provideBibleReadingRepository(localReadingStorage: LocalReadingStorage): BibleReadingRepository =
            BibleReadingRepository(localReadingStorage)

    @Provides
    @Singleton
    fun provideBookmarkRepository(localBookmarkStorage: LocalBookmarkStorage): BookmarkRepository =
            BookmarkRepository(localBookmarkStorage)

    @Provides
    @Singleton
    fun provideHighlightRepository(localHighlightStorage: LocalHighlightStorage): HighlightRepository =
            HighlightRepository(localHighlightStorage)

    @Provides
    @Singleton
    fun provideNoteRepository(localNoteStorage: LocalNoteStorage): NoteRepository =
            NoteRepository(localNoteStorage)

    @Provides
    @Singleton
    fun provideReadingProgressRepository(localReadingProgressStorage: LocalReadingProgressStorage): ReadingProgressRepository =
            ReadingProgressRepository(localReadingProgressStorage)

    @Provides
    @Singleton
    fun provideSettingsRepository(localSettingsStorage: LocalSettingsStorage): SettingsRepository =
            SettingsRepository(localSettingsStorage)

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
