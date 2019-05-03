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

package me.xizzhu.android.joshua.progress

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonAdapter
import me.xizzhu.android.joshua.utils.BaseSettingsView

interface ReadingProgressView : BaseSettingsView {
    fun onReadingProgressLoaded(readingProgressItems: List<BaseItem>)

    fun onReadingProgressLoadFailed()
}

class ReadingProgressListView : RecyclerView, ReadingProgressView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var presenter: ReadingProgressPresenter
    private val adapter: CommonAdapter = CommonAdapter(context)

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(adapter)
    }

    fun setPresenter(presenter: ReadingProgressPresenter) {
        this.presenter = presenter
    }

    override fun onSettingsUpdated(settings: Settings) {
        adapter.setSettings(settings)
    }

    override fun onReadingProgressLoaded(readingProgressItems: List<BaseItem>) {
        adapter.setItems(readingProgressItems)
    }

    override fun onReadingProgressLoadFailed() {
        DialogHelper.showDialog(context, true, R.string.dialog_load_reading_progress_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.loadReadingProgress()
                })
    }
}
