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

package me.xizzhu.android.joshua.annotated.highlights

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.channels.first
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.core.HighlightManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.ui.LoadingAwarePresenter
import me.xizzhu.android.joshua.annotated.SortOrderToolbarPresenter

@Module
class HighlightsModule {
    @Provides
    @ActivityScope
    fun provideHighlightsInteractor(highlightsActivity: HighlightsActivity,
                                    highlightsManager: HighlightManager,
                                    settingsManager: SettingsManager): HighlightsInteractor =
            HighlightsInteractor(highlightsActivity, highlightsManager, settingsManager)

    @Provides
    fun provideLoadingAwarePresenter(highlightsInteractor: HighlightsInteractor): LoadingAwarePresenter =
            LoadingAwarePresenter(highlightsInteractor.observeLoadingState())

    @Provides
    fun provideSortOrderToolbarPresenter(highlightsInteractor: HighlightsInteractor): SortOrderToolbarPresenter =
            SortOrderToolbarPresenter({ highlightsInteractor.observeSortOrder().first() },
                    highlightsInteractor::saveSortOrder)
}
