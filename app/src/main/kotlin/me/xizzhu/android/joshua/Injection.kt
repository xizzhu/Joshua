/*
 * Copyright (C) 2022 Xizhi Zhu
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

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.provider.DefaultCoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.provider.DefaultTimeProvider
import me.xizzhu.android.joshua.core.provider.TimeProvider
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.remote.android.HttpCrossReferencesService
import me.xizzhu.android.joshua.core.repository.remote.android.HttpStrongNumberService
import me.xizzhu.android.joshua.core.repository.remote.android.HttpTranslationService
import me.xizzhu.android.joshua.core.serializer.android.BackupJsonSerializer
import javax.inject.Singleton
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.CrossReferencesManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SearchManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.CrossReferencesRepository
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.joshua.core.repository.SettingsRepository
import me.xizzhu.android.joshua.core.repository.StrongNumberRepository
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.joshua.core.repository.VerseAnnotationRepository
import me.xizzhu.android.joshua.core.repository.local.android.AndroidBookmarkStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidCrossReferencesStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidHighlightStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidNoteStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidReadingProgressStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidReadingStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidSettingsStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidTranslationStorage

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {
    @Provides
    @Singleton
    fun provideNavigator(): Navigator = Navigator()
}

@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope = appScope

    @Provides
    @Singleton
    fun provideCoroutineDispatcherProvider(): CoroutineDispatcherProvider = DefaultCoroutineDispatcherProvider()

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = DefaultTimeProvider()
}

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {
    @Provides
    @Singleton
    fun provideBackupManager(
        bookmarkRepository: VerseAnnotationRepository<Bookmark>,
        highlightRepository: VerseAnnotationRepository<Highlight>,
        noteRepository: VerseAnnotationRepository<Note>,
        readingProgressRepository: ReadingProgressRepository
    ): BackupManager = BackupManager(BackupJsonSerializer(), bookmarkRepository, highlightRepository, noteRepository, readingProgressRepository)

    @Provides
    @Singleton
    fun provideBibleReadingManager(
        bibleReadingRepository: BibleReadingRepository,
        translationRepository: TranslationRepository,
        appScope: CoroutineScope
    ): BibleReadingManager = BibleReadingManager(bibleReadingRepository, translationRepository, appScope)

    @Provides
    @Singleton
    fun provideBookmarkManager(bookmarkRepository: VerseAnnotationRepository<Bookmark>): VerseAnnotationManager<Bookmark> = VerseAnnotationManager(bookmarkRepository)

    @Provides
    @Singleton
    fun provideCrossReferencesManager(crossReferencesRepository: CrossReferencesRepository): CrossReferencesManager = CrossReferencesManager(crossReferencesRepository)

    @Provides
    @Singleton
    fun provideHighlightManager(highlightRepository: VerseAnnotationRepository<Highlight>): VerseAnnotationManager<Highlight> = VerseAnnotationManager(highlightRepository)

    @Provides
    @Singleton
    fun provideNoteManager(noteRepository: VerseAnnotationRepository<Note>): VerseAnnotationManager<Note> = VerseAnnotationManager(noteRepository)

    @Provides
    @Singleton
    fun provideReadingProgressManager(
        bibleReadingRepository: BibleReadingRepository,
        readingProgressRepository: ReadingProgressRepository,
        appScope: CoroutineScope
    ): ReadingProgressManager = ReadingProgressManager(bibleReadingRepository, readingProgressRepository, appScope)

    @Provides
    @Singleton
    fun provideSearchManager(
        bibleReadingRepository: BibleReadingRepository,
        bookmarkRepository: VerseAnnotationRepository<Bookmark>,
        highlightRepository: VerseAnnotationRepository<Highlight>,
        noteRepository: VerseAnnotationRepository<Note>
    ): SearchManager = SearchManager(bibleReadingRepository, bookmarkRepository, highlightRepository, noteRepository)

    @Provides
    @Singleton
    fun provideSettingsManager(settingsRepository: SettingsRepository): SettingsManager = SettingsManager(settingsRepository)

    @Provides
    @Singleton
    fun provideStrongNumberManager(strongNumberRepository: StrongNumberRepository): StrongNumberManager = StrongNumberManager(strongNumberRepository)

    @Provides
    @Singleton
    fun provideTranslationManager(translationRepository: TranslationRepository): TranslationManager = TranslationManager(translationRepository)
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAndroidDatabase(app: Application): AndroidDatabase = AndroidDatabase(app)

    @Provides
    @Singleton
    fun provideBibleReadingRepository(androidDatabase: AndroidDatabase, appScope: CoroutineScope): BibleReadingRepository =
        BibleReadingRepository(AndroidReadingStorage(androidDatabase), appScope)

    @Provides
    @Singleton
    fun provideBookmarkRepository(androidDatabase: AndroidDatabase, appScope: CoroutineScope): VerseAnnotationRepository<Bookmark> =
        VerseAnnotationRepository(AndroidBookmarkStorage(androidDatabase), appScope)

    @Provides
    @Singleton
    fun provideCrossReferencesRepository(app: Application, androidDatabase: AndroidDatabase): CrossReferencesRepository =
        CrossReferencesRepository(AndroidCrossReferencesStorage(androidDatabase), HttpCrossReferencesService(app))

    @Provides
    @Singleton
    fun provideHighlightRepository(androidDatabase: AndroidDatabase, appScope: CoroutineScope): VerseAnnotationRepository<Highlight> =
        VerseAnnotationRepository(AndroidHighlightStorage(androidDatabase), appScope)

    @Provides
    @Singleton
    fun provideNoteRepository(androidDatabase: AndroidDatabase, appScope: CoroutineScope): VerseAnnotationRepository<Note> =
        VerseAnnotationRepository(AndroidNoteStorage(androidDatabase), appScope)

    @Provides
    @Singleton
    fun provideReadingProgressRepository(androidDatabase: AndroidDatabase): ReadingProgressRepository =
        ReadingProgressRepository(AndroidReadingProgressStorage(androidDatabase))

    @Provides
    @Singleton
    fun provideSettingsRepository(androidDatabase: AndroidDatabase, appScope: CoroutineScope): SettingsRepository =
        SettingsRepository(AndroidSettingsStorage(androidDatabase), appScope)

    @Provides
    @Singleton
    fun provideStrongNumberRepository(app: Application, androidDatabase: AndroidDatabase): StrongNumberRepository =
        StrongNumberRepository(AndroidStrongNumberStorage(androidDatabase), HttpStrongNumberService(app))

    @Provides
    @Singleton
    fun provideTranslationRepository(app: Application, androidDatabase: AndroidDatabase, appScope: CoroutineScope): TranslationRepository =
        TranslationRepository(AndroidTranslationStorage(androidDatabase), HttpTranslationService(app), appScope)
}
