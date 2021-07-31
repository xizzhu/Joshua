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

package me.xizzhu.android.joshua.annotated.highlights

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesViewModel
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.highlights.list.HighlightsListPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager

@Module
@InstallIn(ActivityComponent::class)
object HighlightsModule {
    @Provides
    fun provideHighlightsActivity(activity: Activity): HighlightsActivity = activity as HighlightsActivity

    @Provides
    fun provideToolbarPresenter(highlightsViewModel: BaseAnnotatedVersesViewModel<Highlight>,
                                highlightsActivity: HighlightsActivity): AnnotatedVersesToolbarPresenter<Highlight, HighlightsActivity> =
            AnnotatedVersesToolbarPresenter(R.string.title_highlights, highlightsViewModel, highlightsActivity)

    @ActivityScoped
    @Provides
    fun provideHighlightsListPresenter(navigator: Navigator, highlightsViewModel: BaseAnnotatedVersesViewModel<Highlight>,
                                       highlightsActivity: HighlightsActivity): BaseAnnotatedVersesPresenter<Highlight, HighlightsActivity> =
            HighlightsListPresenter(navigator, highlightsViewModel, highlightsActivity)

    @ActivityScoped
    @Provides
    fun provideHighlightsViewModel(highlightsActivity: HighlightsActivity,
                                   bibleReadingManager: BibleReadingManager,
                                   highlightsManager: VerseAnnotationManager<Highlight>,
                                   settingsManager: SettingsManager): BaseAnnotatedVersesViewModel<Highlight> {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HighlightsViewModel::class.java)) {
                    return HighlightsViewModel(bibleReadingManager, highlightsManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(highlightsActivity, factory).get(HighlightsViewModel::class.java)
    }
}
