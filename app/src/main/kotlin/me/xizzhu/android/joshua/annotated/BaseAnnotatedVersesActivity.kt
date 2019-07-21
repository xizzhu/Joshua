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
import androidx.annotation.StringRes
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.ui.bindView
import me.xizzhu.android.joshua.utils.activities.BaseLoadingSpinnerActivity
import javax.inject.Inject

abstract class BaseAnnotatedVersesActivity(@StringRes private val title: Int) : BaseLoadingSpinnerActivity() {
    @Inject
    lateinit var toolbarPresenter: AnnotatedVersesToolbarPresenter

    private val toolbar: AnnotatedVersesToolbar by bindView(R.id.toolbar)
    private val verseListView: AnnotatedVerseListView by bindView(R.id.verse_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_annotated)
        verseListView.setPresenter(getAnnotatedVersesPresenter())
        toolbar.setPresenter(toolbarPresenter)
        toolbar.setTitle(title)
    }

    override fun onStart() {
        super.onStart()

        toolbarPresenter.attachView(toolbar)
        getAnnotatedVersesPresenter().attachView(verseListView)
    }

    override fun onStop() {
        toolbarPresenter.detachView()
        getAnnotatedVersesPresenter().detachView()

        super.onStop()
    }

    abstract fun getAnnotatedVersesPresenter(): AnnotatedVersePresenter
}
