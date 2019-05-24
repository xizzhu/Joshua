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

package me.xizzhu.android.joshua.bookmarks

import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.bookmarks.list.BookmarksListView
import me.xizzhu.android.joshua.bookmarks.list.BookmarksPresenter
import me.xizzhu.android.joshua.ui.LoadingSpinner
import me.xizzhu.android.joshua.ui.LoadingSpinnerPresenter
import me.xizzhu.android.joshua.ui.bindView
import me.xizzhu.android.joshua.utils.BaseSettingsActivity
import javax.inject.Inject

class BookmarksActivity : BaseSettingsActivity() {
    @Inject
    lateinit var bookmarksInteractor: BookmarksInteractor

    @Inject
    lateinit var loadingSpinnerPresenter: LoadingSpinnerPresenter

    @Inject
    lateinit var bookmarksPresenter: BookmarksPresenter

    private val loadingSpinner: LoadingSpinner by bindView(R.id.loading_spinner)
    private val bookmarksListView: BookmarksListView by bindView(R.id.bookmarks)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bookmarks)
        bookmarksListView.setPresenter(bookmarksPresenter)

        observeSettings(bookmarksInteractor)
    }

    override fun onStart() {
        super.onStart()

        loadingSpinnerPresenter.attachView(loadingSpinner)
        bookmarksPresenter.attachView(bookmarksListView)
    }

    override fun onStop() {
        loadingSpinnerPresenter.detachView()
        bookmarksPresenter.detachView()

        super.onStop()
    }
}
