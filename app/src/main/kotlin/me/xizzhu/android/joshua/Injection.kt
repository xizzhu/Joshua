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
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.LocalStorage
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.joshua.core.repository.android.LocalStorageImpl
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.local.android.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.AndroidTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.core.repository.remote.retrofit.RetrofitTranslationService
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingModule
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchModule
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
    fun provideBibleReadingManager(bibleReadingRepository: BibleReadingRepository): BibleReadingManager =
            BibleReadingManager(bibleReadingRepository)

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
    fun provideLocalStorage(app: App): LocalStorage = LocalStorageImpl(app)

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
    fun provideLocalTranslationStorage(androidDatabase: AndroidDatabase): LocalTranslationStorage =
            AndroidTranslationStorage(androidDatabase)

    @Provides
    @Singleton
    fun provideRemoteTranslationService(moshi: Moshi, okHttpClient: OkHttpClient): RemoteTranslationService =
            RetrofitTranslationService(moshi, okHttpClient)

    @Provides
    @Singleton
    fun provideBibleReadingRepository(localStorage: LocalStorage): BibleReadingRepository =
            BibleReadingRepository(localStorage)

    @Provides
    @Singleton
    fun provideTranslationRepository(localAndroidStorage: LocalTranslationStorage,
                                     remoteTranslationService: RemoteTranslationService): TranslationRepository =
            TranslationRepository(localAndroidStorage, remoteTranslationService)
}

@Module
abstract class ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [(ReadingModule::class)])
    abstract fun contributeReadingActivity(): ReadingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(SearchModule::class)])
    abstract fun contributeSearchActivity(): SearchActivity

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
