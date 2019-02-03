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

package me.xizzhu.android.joshua.translations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.model.TranslationInfo
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.MVPView
import me.xizzhu.android.joshua.utils.fadeIn
import me.xizzhu.android.joshua.utils.fadeOut
import javax.inject.Inject

interface TranslationManagementView : MVPView {
    fun onTranslationsLoaded(translations: List<TranslationInfo>, currentTranslation: String)

    fun onTranslationsLoadFailed()

    fun onTranslationDownloadProgressed(progress: Int)

    fun onTranslationDownloaded()

    fun onTranslationDownloadFailed()
}

class TranslationManagementActivity : BaseActivity(), TranslationManagementView {
    companion object {
        fun newStartIntent(context: Context) = Intent(context, TranslationManagementActivity::class.java)
    }

    @Inject
    lateinit var presenter: TranslationManagementPresenter

    private lateinit var loadingSpinner: ProgressBar
    private lateinit var translationListView: RecyclerView

    private lateinit var adapter: TranslationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_translation_management)
        loadingSpinner = findViewById(R.id.loading_spinner)

        translationListView = findViewById(R.id.translation_list)
        translationListView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = TranslationListAdapter(this, presenter)
        translationListView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.takeView(this)
        loadTranslations(false)
    }

    private fun loadTranslations(forceRefresh: Boolean) {
        loadingSpinner.visibility = View.VISIBLE
        translationListView.visibility = View.GONE

        presenter.loadTranslations(forceRefresh)
    }

    override fun onStop() {
        presenter.dropView()
        super.onStop()
    }

    override fun onTranslationsLoaded(translations: List<TranslationInfo>, currentTranslation: String) {
        adapter.setTranslations(translations, currentTranslation)

        loadingSpinner.fadeOut()
        translationListView.fadeIn()
    }

    override fun onTranslationsLoadFailed() {
        // TODO
    }

    override fun onTranslationDownloadProgressed(progress: Int) {
        // TODO
    }

    override fun onTranslationDownloaded() {
        loadTranslations(false)
    }

    override fun onTranslationDownloadFailed() {
        // TODO
    }
}
