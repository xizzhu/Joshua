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

import me.xizzhu.android.joshua.annotated.AnnotatedVersePresenter
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.highlights.list.HighlightsPresenter
import me.xizzhu.android.joshua.utils.activities.BaseSettingsInteractor
import javax.inject.Inject

class HighlightsActivity : BaseAnnotatedVersesActivity() {
    @Inject
    lateinit var highlightInteractor: HighlightsInteractor

    @Inject
    lateinit var highlightsPresenter: HighlightsPresenter

    override fun getBaseSettingsInteractor(): BaseSettingsInteractor = highlightInteractor

    override fun getAnnotatedVersesPresenter(): AnnotatedVersePresenter = highlightsPresenter
}
