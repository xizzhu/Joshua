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

package me.xizzhu.android.joshua.annotated.toolbar

import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesViewModel
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.onEachSuccess

data class AnnotatedVersesToolbarViewHolder(val toolbar: AnnotatedVersesToolbar) : ViewHolder

class AnnotatedVersesToolbarPresenter<V : VerseAnnotation, A : BaseAnnotatedVersesActivity<V, A>>(
        @StringRes private val title: Int, annotatedVersesViewModel: BaseAnnotatedVersesViewModel<V>,
        annotatedVersesActivity: A, coroutineScope: CoroutineScope = annotatedVersesActivity.lifecycleScope
) : BaseSettingsPresenter<AnnotatedVersesToolbarViewHolder, BaseAnnotatedVersesViewModel<V>, A>(annotatedVersesViewModel, annotatedVersesActivity, coroutineScope) {
    @UiThread
    override fun onBind() {
        super.onBind()

        viewHolder.toolbar.setTitle(title)
        viewHolder.toolbar.sortOrderUpdated = { sortOrder -> coroutineScope.launch { viewModel.saveSortOrder(sortOrder) } }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun observeSortOrder() {
        viewModel.sortOrder().onEachSuccess { viewHolder.toolbar.setSortOrder(it) }.launchIn(coroutineScope)
    }
}
