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
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.highlights.list.HighlightsListInteractor
import me.xizzhu.android.joshua.annotated.highlights.list.HighlightsListPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarInteractor
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerInteractor
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerPresenter

@Module
object HighlightsModule {
    @ActivityScope
    @Provides
    fun provideAnnotatedVersesToolbarInteractor(highlightManager: VerseAnnotationManager<Highlight>): AnnotatedVersesToolbarInteractor =
            AnnotatedVersesToolbarInteractor({ highlightManager.observeSortOrder().first() }, highlightManager::saveSortOrder)

    @ActivityScope
    @Provides
    fun provideSortOrderToolbarPresenter(annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor): AnnotatedVersesToolbarPresenter =
            AnnotatedVersesToolbarPresenter(R.string.title_highlights, annotatedVersesToolbarInteractor)

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerInteractor(): LoadingSpinnerInteractor = LoadingSpinnerInteractor()

    @ActivityScope
    @Provides
    fun provideLoadingSpinnerPresenter(loadingSpinnerInteractor: LoadingSpinnerInteractor): LoadingSpinnerPresenter =
            LoadingSpinnerPresenter(loadingSpinnerInteractor)

    @ActivityScope
    @Provides
    fun provideHighlightsListInteractor(highlightManager: VerseAnnotationManager<Highlight>,
                                        bibleReadingManager: BibleReadingManager,
                                        settingsManager: SettingsManager): HighlightsListInteractor =
            HighlightsListInteractor(highlightManager, bibleReadingManager, settingsManager)

    @ActivityScope
    @Provides
    fun provideHighlightsListPresenter(highlightsActivity: HighlightsActivity,
                                       navigator: Navigator,
                                       highlightsListInteractor: HighlightsListInteractor): HighlightsListPresenter =
            HighlightsListPresenter(highlightsActivity, navigator, highlightsListInteractor)

    @ActivityScope
    @Provides
    fun provideHighlightsViewModel(settingsManager: SettingsManager,
                                   annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor,
                                   loadingSpinnerInteractor: LoadingSpinnerInteractor,
                                   highlightsListInteractor: HighlightsListInteractor): HighlightsViewModel =
            HighlightsViewModel(settingsManager, annotatedVersesToolbarInteractor, loadingSpinnerInteractor, highlightsListInteractor)
}
