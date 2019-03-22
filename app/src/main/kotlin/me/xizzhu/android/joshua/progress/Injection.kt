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
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.ui.LoadingSpinnerPresenter

@Module
class ReadingProgressModule {
    @Provides
    @ActivityScope
    fun provideReadingProgressInteractor(readingProgressManager: ReadingProgressManager,
                                         bibleReadingManager: BibleReadingManager): ReadingProgressInteractor =
            ReadingProgressInteractor(readingProgressManager, bibleReadingManager)

    @Provides
    fun provideLoadingSpinnerPresenter(readingProgressInteractor: ReadingProgressInteractor): LoadingSpinnerPresenter =
            LoadingSpinnerPresenter(readingProgressInteractor.observeReadingProgressLoadingState())

    @Provides
    fun provideReadingProgressPresenter(readingProgressInteractor: ReadingProgressInteractor): ReadingProgressPresenter =
            ReadingProgressPresenter(readingProgressInteractor)
}
