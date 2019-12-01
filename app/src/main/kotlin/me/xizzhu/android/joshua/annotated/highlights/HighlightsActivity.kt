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

import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewHolder
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.AnnotatedVersesInteractor
import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewModel
import me.xizzhu.android.joshua.annotated.highlights.list.HighlightsListPresenter
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwareViewModel
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import javax.inject.Inject

class HighlightsActivity : BaseAnnotatedVersesActivity<Highlight>() {
    @Inject
    lateinit var highlightsViewModel: AnnotatedVersesViewModel<Highlight>

    @Inject
    lateinit var highlightsListPresenter: HighlightsListPresenter

    override fun getBaseSettingsAwareViewModel(): BaseSettingsAwareViewModel = highlightsViewModel

    override fun listPresenter(): ViewPresenter<AnnotatedVersesViewHolder, AnnotatedVersesInteractor<Highlight>> = highlightsListPresenter
}
