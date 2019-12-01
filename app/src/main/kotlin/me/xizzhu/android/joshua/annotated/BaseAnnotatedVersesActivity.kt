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

package me.xizzhu.android.joshua.annotated

import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarPresenter
import me.xizzhu.android.joshua.annotated.toolbar.AnnotatedVersesToolbarViewHolder
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwareActivity
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerPresenter
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerViewHolder
import javax.inject.Inject

abstract class BaseAnnotatedVersesActivity<V: VerseAnnotation> : BaseSettingsAwareActivity() {
    @Inject
    lateinit var toolbarPresenter: AnnotatedVersesToolbarPresenter
    @Inject
    lateinit var loadingSpinnerPresenter: LoadingSpinnerPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_annotated)
        toolbarPresenter.bind(AnnotatedVersesToolbarViewHolder(findViewById(R.id.toolbar)))
        loadingSpinnerPresenter.bind(LoadingSpinnerViewHolder(findViewById(R.id.loading_spinner)))
        listPresenter().bind(AnnotatedVersesViewHolder(findViewById(R.id.verse_list)))
    }

    override fun getViewPresenters(): List<ViewPresenter<out ViewHolder, out Interactor>> = listOf(toolbarPresenter, loadingSpinnerPresenter, listPresenter())

    protected abstract fun listPresenter(): ViewPresenter<AnnotatedVersesViewHolder, out AnnotatedVersesInteractor<V>>
}
