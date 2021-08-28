/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.translations

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.SettingsManager

@Module
@InstallIn(ActivityComponent::class)
object TranslationsModule {
    @Provides
    fun provideTranslationsActivity(activity: Activity): TranslationsActivity = activity as TranslationsActivity

    @ActivityScoped
    @Provides
    fun provideTranslationsInteractor(
            bibleReadingManager: BibleReadingManager, translationManager: TranslationManager, settingsManager: SettingsManager
    ): TranslationsInteractor = TranslationsInteractor(bibleReadingManager, translationManager, settingsManager)

    @ActivityScoped
    @Provides
    fun provideTranslationsViewModel(
            navigator: Navigator, translationsInteractor: TranslationsInteractor, translationsActivity: TranslationsActivity
    ): TranslationsViewModel = TranslationsViewModel(navigator, translationsInteractor, translationsActivity)
}
