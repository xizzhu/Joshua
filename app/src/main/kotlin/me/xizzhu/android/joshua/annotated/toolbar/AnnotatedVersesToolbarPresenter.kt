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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter

data class AnnotatedVersesToolbarViewHolder(val toolbar: AnnotatedVersesToolbar) : ViewHolder

class AnnotatedVersesToolbarPresenter<V : VerseAnnotation>(@StringRes private val title: Int,
                                                           annotatedVersesToolbarInteractor: AnnotatedVersesToolbarInteractor<V>,
                                                           dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<AnnotatedVersesToolbarViewHolder, AnnotatedVersesToolbarInteractor<V>>(annotatedVersesToolbarInteractor, dispatcher) {
    @UiThread
    override fun onCreate(viewHolder: AnnotatedVersesToolbarViewHolder) {
        super.onCreate(viewHolder)

        viewHolder.toolbar.setTitle(title)
        viewHolder.toolbar.sortOrderUpdated = { sortOrder -> coroutineScope.launch { interactor.saveSortOrder(sortOrder) } }
    }

    @UiThread
    override fun onStart() {
        super.onStart()

        coroutineScope.launch { viewHolder?.toolbar?.setSortOrder(interactor.readSortOrder()) }
    }
}
