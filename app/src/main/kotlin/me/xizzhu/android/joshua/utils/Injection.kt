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

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
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
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.translations.TranslationManagementComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule(private val app: App) {
    @Provides
    @Singleton
    fun provideApp(): App = app

    @Provides
    @Singleton
    fun provideSharedPreferences(app: App): SharedPreferences = app.getSharedPreferences(SHARED_PREFERENCES_NAME, SHARED_PREFERENCES_MODE)

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .readTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(OKHTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
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
