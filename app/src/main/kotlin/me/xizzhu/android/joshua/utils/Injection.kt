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

package me.xizzhu.android.joshua.utils

import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import me.xizzhu.android.joshua.*
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.internal.repository.BackendService
import me.xizzhu.android.joshua.core.internal.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.internal.repository.LocalStorage
import me.xizzhu.android.joshua.core.internal.repository.TranslationRepository
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingModule
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

    @Provides
    @Singleton
    fun provideLocalStorage(app: App) = LocalStorage(app)

    @Provides
    @Singleton
    fun provideBackendService() = BackendService()

    @Provides
    @Singleton
    fun provideBibleReadingRepository(localStorage: LocalStorage) = BibleReadingRepository(localStorage)

    @Provides
    @Singleton
    fun provideTranslationRepository(localStorage: LocalStorage, backendService: BackendService) =
            TranslationRepository(localStorage, backendService)

    @Provides
    @Singleton
    fun provideBibleReadingManager(bibleReadingRepository: BibleReadingRepository) =
            BibleReadingManager(bibleReadingRepository)

    @Provides
    @Singleton
    fun provideTranslationManager(translationRepository: TranslationRepository) =
            TranslationManager(translationRepository)
}

@Module
abstract class ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [(ReadingModule::class)])
    abstract fun contributeReadingActivity(): ReadingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(TranslationManagementModule::class)])
    abstract fun contributeTranslationManagementActivity(): TranslationManagementActivity
}

@Singleton
@Component(modules = [(AppModule::class), (ActivityModule::class), (AndroidInjectionModule::class), (AndroidSupportInjectionModule::class)])
interface AppComponent {
    fun inject(app: App)
}
