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

package me.xizzhu.android.joshua.annotated.highlights.list

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersesInteractor
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem

class HighlightsListPresenter(highlightsActivity: HighlightsActivity,
                              navigator: Navigator,
                              highlightsListInteractor: AnnotatedVersesInteractor<Highlight>,
                              dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseAnnotatedVersesPresenter<Highlight, AnnotatedVersesInteractor<Highlight>>(
        highlightsActivity, navigator, R.string.text_no_highlights, highlightsListInteractor, dispatcher) {
    override fun Highlight.toBaseItem(bookName: String, bookShortName: String, verseText: String, @Constants.SortOrder sortOrder: Int): BaseItem =
            HighlightItem(verseIndex, bookName, bookShortName, verseText, color, sortOrder, ::openVerse)
}
