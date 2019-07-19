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

package me.xizzhu.android.joshua.ui

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseRecyclerView
import me.xizzhu.android.joshua.utils.activities.BaseSettingsPresenter
import me.xizzhu.android.joshua.utils.activities.BaseSettingsView
import me.xizzhu.android.logger.Log

interface AnnotatedVersesView : BaseSettingsView {
    fun onLoadingStarted()

    fun onLoadingCompleted()

    fun onItemsLoaded(items: List<BaseItem>)

    fun onLoadingFailed(@Constants.SortOrder sortOrder: Int)

    fun onVerseSelectionFailed(verseToSelect: VerseIndex)
}

class AnnotatedVerseListView : BaseRecyclerView, AnnotatedVersesView {
    private lateinit var presenter: AnnotatedVersePresenter

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setPresenter(presenter: AnnotatedVersePresenter) {
        this.presenter = presenter
    }

    override fun onLoadingStarted() {
        visibility = GONE
    }

    override fun onLoadingCompleted() {
        fadeIn()
    }

    override fun onItemsLoaded(items: List<BaseItem>) {
        setItems(items)
    }

    override fun onLoadingFailed(@Constants.SortOrder sortOrder: Int) {
        DialogHelper.showDialog(context, true, R.string.dialog_load_annotated_verses_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.load(sortOrder)
                })
    }

    override fun onVerseSelectionFailed(verseToSelect: VerseIndex) {
        DialogHelper.showDialog(context, true, R.string.dialog_verse_selection_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.openVerse(verseToSelect)
                })
    }
}

abstract class AnnotatedVersePresenter(private val baseAnnotatedVerseInteractor: BaseAnnotatedVerseInteractor)
    : BaseSettingsPresenter<AnnotatedVersesView>(baseAnnotatedVerseInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            baseAnnotatedVerseInteractor.observeSortOrder().consumeEach { load(it) }
        }
    }

    abstract fun load(@Constants.SortOrder sortOrder: Int)

    fun openVerse(verseToSelect: VerseIndex) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                baseAnnotatedVerseInteractor.openVerse(verseToSelect)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                view?.onVerseSelectionFailed(verseToSelect)
            }
        }
    }
}

abstract class BaseAnnotatedVerseInteractor(settingsManager: SettingsManager,
                                            @Companion.LoadingState initialLoadingState: Int)
    : BaseLoadingAwareInteractor(settingsManager, initialLoadingState) {
    abstract suspend fun observeSortOrder(): ReceiveChannel<Int>

    abstract suspend fun openVerse(verseIndex: VerseIndex)
}
