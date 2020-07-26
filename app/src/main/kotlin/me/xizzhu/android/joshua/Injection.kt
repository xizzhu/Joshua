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

package me.xizzhu.android.joshua

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.core.repository.*
import me.xizzhu.android.joshua.core.repository.local.android.*
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.remote.android.HttpStrongNumberService
import me.xizzhu.android.joshua.core.repository.remote.android.HttpTranslationService
import me.xizzhu.android.joshua.core.serializer.android.BackupJsonSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApp(application: Application): App = application as App

    @Provides
    @Singleton
    fun provideNavigator(): Navigator = Navigator()

    @Provides
    @Singleton
    fun provideBackupManager(bookmarkRepository: VerseAnnotationRepository<Bookmark>,
                             highlightRepository: VerseAnnotationRepository<Highlight>,
                             noteRepository: VerseAnnotationRepository<Note>,
                             readingProgressRepository: ReadingProgressRepository): BackupManager =
            BackupManager(BackupJsonSerializer(), bookmarkRepository, highlightRepository, noteRepository, readingProgressRepository)

    @Provides
    @Singleton
    fun provideBibleReadingManager(bibleReadingRepository: BibleReadingRepository,
                                   translationRepository: TranslationRepository): BibleReadingManager =
            BibleReadingManager(bibleReadingRepository, translationRepository)

    @Provides
    @Singleton
    fun provideBookmarkManager(bookmarkRepository: VerseAnnotationRepository<Bookmark>): VerseAnnotationManager<Bookmark> =
            VerseAnnotationManager(bookmarkRepository)

    @Provides
    @Singleton
    fun provideHighlightManager(highlightRepository: VerseAnnotationRepository<Highlight>): VerseAnnotationManager<Highlight> =
            VerseAnnotationManager(highlightRepository)

    @Provides
    @Singleton
    fun provideNoteManager(noteRepository: VerseAnnotationRepository<Note>): VerseAnnotationManager<Note> =
            VerseAnnotationManager(noteRepository)

    @Provides
    @Singleton
    fun provideReadingProgressManager(bibleReadingRepository: BibleReadingRepository,
                                      readingProgressRepository: ReadingProgressRepository): ReadingProgressManager =
            ReadingProgressManager(bibleReadingRepository, readingProgressRepository)

    @Provides
    @Singleton
    fun provideSettingsManager(settingsRepository: SettingsRepository): SettingsManager =
            SettingsManager(settingsRepository)

    @Provides
    @Singleton
    fun provideStrongNumberManager(strongNumberRepository: StrongNumberRepository): StrongNumberManager =
            StrongNumberManager(strongNumberRepository)

    @Provides
    @Singleton
    fun provideTranslationManager(translationRepository: TranslationRepository): TranslationManager =
            TranslationManager(translationRepository)
}

@Module
@InstallIn(SingletonComponent::class)
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
    fun provideStrongNumberRepository(androidDatabase: AndroidDatabase): StrongNumberRepository =
            StrongNumberRepository(AndroidStrongNumberStorage(androidDatabase), HttpStrongNumberService())

    @Provides
    @Singleton
    fun provideTranslationRepository(androidDatabase: AndroidDatabase): TranslationRepository =
            TranslationRepository(AndroidTranslationStorage(androidDatabase), HttpTranslationService())
}
