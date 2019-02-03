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

import androidx.room.Room
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import me.xizzhu.android.joshua.*
import me.xizzhu.android.joshua.model.LocalStorage
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.translations.TranslationManagementComponent
import javax.inject.Singleton

@Module
class AppModule(private val app: App) {
    @Provides
    @Singleton
    fun provideApp(): App = app

    @Provides
    @Singleton
    fun provideLocalStorage(app: App): LocalStorage =
            Room.databaseBuilder(app, LocalStorage::class.java, LocalStorage.DATABASE_NAME).build()
}

@Module(subcomponents = [(TranslationManagementComponent::class)])
abstract class ActivityModule {
    @Binds
    @IntoMap
    @ClassKey(TranslationManagementActivity::class)
    abstract fun bindTranslationManagementActivityInjectorFactory(builder: TranslationManagementComponent.Builder): AndroidInjector.Factory<*>
}

@Singleton
@Component(modules = [(AppModule::class), (ActivityModule::class), (AndroidInjectionModule::class),
    (AndroidSupportInjectionModule::class)])
interface AppComponent {
    fun inject(app: App)

    fun inject(launcherActivity: LauncherActivity)
}
