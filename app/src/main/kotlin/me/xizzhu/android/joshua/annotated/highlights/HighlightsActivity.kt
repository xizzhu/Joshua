/*
 * Copyright (C) 2020 Xizhi Zhu
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

import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesViewModel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager

class HighlightsViewModel(bibleReadingManager: BibleReadingManager,
                          highlightsManager: VerseAnnotationManager<Highlight>,
                          settingsManager: SettingsManager)
    : BaseAnnotatedVersesViewModel<Highlight>(bibleReadingManager, highlightsManager, settingsManager)

class HighlightsActivity : BaseAnnotatedVersesActivity<Highlight, HighlightsActivity>()
