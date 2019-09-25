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

package me.xizzhu.android.joshua.translations

import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.core.SettingsManager

@Module
object TranslationManagementModule {
    @JvmStatic
    @ActivityScope
    @Provides
    fun provideSwipeRefreshInteractor(): SwipeRefreshInteractor = SwipeRefreshInteractor()

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideSwipeRefreshPresenter(swipeRefreshInteractor: SwipeRefreshInteractor): SwipeRefreshPresenter =
            SwipeRefreshPresenter(swipeRefreshInteractor)

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideTranslationListInteractor(bibleReadingManager: BibleReadingManager,
                                         translationManager: TranslationManager,
                                         settingsManager: SettingsManager): TranslationListInteractor =
            TranslationListInteractor(bibleReadingManager, translationManager, settingsManager)

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideTranslationListPresenter(translationManagementActivity: TranslationManagementActivity,
                                        translationListInteractor: TranslationListInteractor): TranslationListPresenter =
            TranslationListPresenter(translationManagementActivity, translationListInteractor)

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideTranslationsViewModel(settingsManager: SettingsManager,
                                     swipeRefreshInteractor: SwipeRefreshInteractor,
                                     translationListInteractor: TranslationListInteractor): TranslationsViewModel =
            TranslationsViewModel(settingsManager, swipeRefreshInteractor, translationListInteractor)
}
