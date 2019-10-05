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

import android.content.DialogInterface
import android.view.View
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.logger.Log

data class AnnotatedVersesViewHolder(val annotatedVerseListView: AnnotatedVerseListView) : ViewHolder

abstract class BaseAnnotatedVersesPresenter
<VerseAnnotation, Interactor : BaseAnnotatedVersesInteractor<VerseAnnotation>>
(private val activity: BaseAnnotatedVersesActivity<VerseAnnotation>, private val navigator: Navigator,
 @StringRes private val noItemText: Int, interactor: Interactor, dispatcher: CoroutineDispatcher)
    : BaseSettingsAwarePresenter<AnnotatedVersesViewHolder, Interactor>(interactor, dispatcher) {
    @UiThread
    override fun onStart() {
        super.onStart()

        coroutineScope.launch {
            interactor.settings().collect {
                if (it.status == ViewData.STATUS_SUCCESS) {
                    viewHolder?.annotatedVerseListView?.onSettingsUpdated(it.data)
                }
            }
        }

        coroutineScope.launch { interactor.sortOrder().collect { load(it) } }
    }

    private fun load(@Constants.SortOrder sortOrder: Int) {
        coroutineScope.launch {
            try {
                interactor.updateLoadingState(ViewData.loading(Unit))
                viewHolder?.annotatedVerseListView?.visibility = View.GONE
                viewHolder?.annotatedVerseListView?.onItemsLoaded(prepareItems(sortOrder))
                viewHolder?.annotatedVerseListView?.fadeIn()
                interactor.updateLoadingState(ViewData.success(Unit))
            } catch (e: Exception) {
                Log.e(tag, "Failed to load annotated verses", e)
                interactor.updateLoadingState(ViewData.error(Unit, e))
                DialogHelper.showDialog(activity, true, R.string.dialog_load_annotated_verses_error,
                        DialogInterface.OnClickListener { _, _ -> load(sortOrder) })
            }
        }
    }

    @VisibleForTesting
    suspend fun prepareItems(@Constants.SortOrder sortOrder: Int): List<BaseItem> {
        val verseAnnotations = interactor.readVerseAnnotations(sortOrder)
        return if (verseAnnotations.isEmpty()) {
            listOf(TextItem(activity.getString(noItemText)))
        } else {
            when (sortOrder) {
                Constants.SORT_BY_DATE -> verseAnnotations.toBaseItemsByDate()
                Constants.SORT_BY_BOOK -> verseAnnotations.toBaseItemsByBook()
                else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
            }
        }
    }

    @VisibleForTesting
    abstract suspend fun List<VerseAnnotation>.toBaseItemsByDate(): List<BaseItem>

    @VisibleForTesting
    abstract suspend fun List<VerseAnnotation>.toBaseItemsByBook(): List<BaseItem>

    @VisibleForTesting
    fun openVerse(verseToSelect: VerseIndex) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(verseToSelect)
                navigator.navigate(activity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                DialogHelper.showDialog(activity, true, R.string.dialog_verse_selection_error,
                        DialogInterface.OnClickListener { _, _ -> openVerse(verseToSelect) })
            }
        }
    }
}
