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

package me.xizzhu.android.joshua.highlights

import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.ui.SortOrderToolbar
import me.xizzhu.android.joshua.ui.SortOrderToolbarPresenter
import me.xizzhu.android.joshua.ui.bindView
import me.xizzhu.android.joshua.utils.activities.BaseLoadingSpinnerActivity
import me.xizzhu.android.joshua.utils.activities.BaseSettingsInteractor
import javax.inject.Inject

class HighlightsActivity : BaseLoadingSpinnerActivity() {
    @Inject
    lateinit var highlightInteractor: HighlightsInteractor

    @Inject
    lateinit var toolbarPresenter: SortOrderToolbarPresenter

    private val toolbar: SortOrderToolbar by bindView(R.id.toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_highlights)
        toolbar.setPresenter(toolbarPresenter)
    }

    override fun onStart() {
        super.onStart()

        toolbarPresenter.attachView(toolbar)
    }

    override fun onStop() {
        toolbarPresenter.detachView()

        super.onStop()
    }

    override fun getBaseSettingsInteractor(): BaseSettingsInteractor = highlightInteractor
}
