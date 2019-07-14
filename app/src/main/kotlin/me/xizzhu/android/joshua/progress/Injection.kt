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

package me.xizzhu.android.joshua.progress

import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.ui.LoadingSpinnerPresenter

@Module
class ReadingProgressModule {
    @Provides
    @ActivityScope
    fun provideReadingProgressInteractor(readingProgressActivity: ReadingProgressActivity,
                                         readingProgressManager: ReadingProgressManager,
                                         bibleReadingManager: BibleReadingManager,
                                         navigator: Navigator,
                                         settingsManager: SettingsManager): ReadingProgressInteractor =
            ReadingProgressInteractor(readingProgressActivity, readingProgressManager, bibleReadingManager, navigator, settingsManager)

    @Provides
    fun provideLoadingSpinnerPresenter(readingProgressInteractor: ReadingProgressInteractor): LoadingSpinnerPresenter =
            LoadingSpinnerPresenter(readingProgressInteractor.observeLoadingState())

    @Provides
    fun provideReadingProgressPresenter(readingProgressInteractor: ReadingProgressInteractor): ReadingProgressPresenter =
            ReadingProgressPresenter(readingProgressInteractor)
}
