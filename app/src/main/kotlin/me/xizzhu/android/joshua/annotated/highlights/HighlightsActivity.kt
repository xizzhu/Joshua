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
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewModel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import javax.inject.Inject

@HiltViewModel
class HighlightsViewModel @Inject constructor(
        bibleReadingManager: BibleReadingManager,
        highlightsManager: VerseAnnotationManager<Highlight>,
        settingsManager: SettingsManager,
        application: Application
) : AnnotatedVersesViewModel<Highlight>(bibleReadingManager, highlightsManager, R.string.text_no_highlights, settingsManager, application) {
    override fun buildBaseItem(annotatedVerse: Highlight, bookName: String, bookShortName: String, verseText: String, sortOrder: Int): BaseItem =
            HighlightItem(annotatedVerse.verseIndex, bookName, bookShortName, verseText, annotatedVerse.color, sortOrder)
}

@AndroidEntryPoint
class HighlightsActivity : AnnotatedVersesActivity<Highlight, HighlightsViewModel>(R.string.title_highlights)
