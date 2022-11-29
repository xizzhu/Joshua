/*
 * Copyright (C) 2022 Xizhi Zhu
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

import android.app.Application
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVerseItem
import me.xizzhu.android.joshua.annotated.AnnotatedVerseActivity
import me.xizzhu.android.joshua.annotated.AnnotatedVerseViewModel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.provider.TimeProvider
import javax.inject.Inject

@HiltViewModel
class HighlightsViewModel @Inject constructor(
    bibleReadingManager: BibleReadingManager,
    highlightsManager: VerseAnnotationManager<Highlight>,
    settingsManager: SettingsManager,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    timeProvider: TimeProvider,
    application: Application
) : AnnotatedVerseViewModel<Highlight>(
    bibleReadingManager = bibleReadingManager,
    verseAnnotationManager = highlightsManager,
    noItemText = R.string.text_no_highlights,
    settingsManager = settingsManager,
    coroutineDispatcherProvider = coroutineDispatcherProvider,
    timeProvider = timeProvider,
    application = application
) {
    override fun buildAnnotatedVerseItem(
        settings: Settings,
        verseAnnotation: Highlight,
        bookName: String,
        bookShortName: String,
        verseText: String,
        @Constants.SortOrder sortOrder: Int
    ): AnnotatedVerseItem = AnnotatedVerseItem.Highlight(
        settings = settings,
        verseIndex = verseAnnotation.verseIndex,
        bookName = bookName,
        bookShortName = bookShortName,
        verseText = verseText,
        highlightColor = verseAnnotation.color,
        sortOrder = sortOrder
    )
}

@AndroidEntryPoint
class HighlightsActivity : AnnotatedVerseActivity<Highlight, HighlightsViewModel>(R.string.title_highlights) {
    override val viewModel: HighlightsViewModel by viewModels()
}
