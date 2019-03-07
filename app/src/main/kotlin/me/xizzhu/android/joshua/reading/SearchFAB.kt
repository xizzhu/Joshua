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

package me.xizzhu.android.joshua.reading

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.MVPView

class SearchButtonPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<SearchButtonView>() {
    fun openSearch() {
        readingInteractor.openSearch()
    }
}

interface SearchButtonView : MVPView

class SearchFloatingActionButton : FloatingActionButton, SearchButtonView, View.OnClickListener {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setOnClickListener(this)
    }

    private lateinit var presenter: SearchButtonPresenter

    fun setPresenter(presenter: SearchButtonPresenter) {
        this.presenter = presenter
    }

    override fun onClick(v: View) {
        presenter.openSearch()
    }
}
