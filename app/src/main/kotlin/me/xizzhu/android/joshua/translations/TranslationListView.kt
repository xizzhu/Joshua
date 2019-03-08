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
import android.content.DialogInterface
import android.util.AttributeSet
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.utils.MVPView
import java.lang.Exception

interface TranslationView : MVPView {
    fun onCurrentTranslationUpdated(currentTranslation: String)

    fun onTranslationsLoadingStarted()

    fun onTranslationsLoadingCompleted()

    fun onAvailableTranslationsUpdated(available: List<TranslationInfo>)

    fun onDownloadedTranslationsUpdated(downloaded: List<TranslationInfo>)

    fun onTranslationDownloadStarted()

    fun onTranslationDownloadProgressed(progress: Int)

    fun onTranslationDownloaded()

    fun onError(e: Exception)
}

class TranslationListView : RecyclerView, TranslationListAdapter.Listener, TranslationView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var presenter: TranslationPresenter

    private val adapter: TranslationListAdapter = TranslationListAdapter(context, this)

    private var currentTranslation: String? = null
    private var availableTranslations: List<TranslationInfo>? = null
    private var downloadedTranslations: List<TranslationInfo>? = null

    private var downloadProgressDialog: ProgressDialog? = null

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(adapter)
    }

    fun setPresenter(presenter: TranslationPresenter) {
        this.presenter = presenter
    }

    override fun onTranslationClicked(translationInfo: TranslationInfo) {
        if (translationInfo.downloaded) {
            presenter.updateCurrentTranslation(translationInfo)
        } else {
            presenter.downloadTranslation(translationInfo)
        }
    }

    override fun onTranslationLongClicked(translationInfo: TranslationInfo) {
        if (translationInfo.downloaded) {
            DialogHelper.showDialog(context, true, R.string.delete_translation_confirmation,
                    DialogInterface.OnClickListener { _, _ ->
                    }, null)
        }
    }

    override fun onCurrentTranslationUpdated(currentTranslation: String) {
        this.currentTranslation = currentTranslation
        updateTranslationList()
    }

    private fun updateTranslationList() {
        if (currentTranslation == null || availableTranslations == null || downloadedTranslations == null) {
            return
        }

        adapter.setTranslations(downloadedTranslations!!, availableTranslations!!, currentTranslation!!)
    }

    override fun onTranslationsLoadingStarted() {
        visibility = GONE
    }

    override fun onTranslationsLoadingCompleted() {
        fadeIn()
    }

    override fun onAvailableTranslationsUpdated(available: List<TranslationInfo>) {
        this.availableTranslations = available
        updateTranslationList()
    }

    override fun onDownloadedTranslationsUpdated(downloaded: List<TranslationInfo>) {
        this.downloadedTranslations = downloaded
        updateTranslationList()
    }

    override fun onTranslationDownloadStarted() {
        downloadProgressDialog = ProgressDialog.showProgressDialog(context, R.string.downloading_translation, 100)
    }

    override fun onTranslationDownloadProgressed(progress: Int) {
        downloadProgressDialog?.setProgress(progress)
    }

    override fun onTranslationDownloaded() {
        dismissDownloadProgressDialog()
        Toast.makeText(context, R.string.translation_downloaded, Toast.LENGTH_SHORT).show()
    }

    private fun dismissDownloadProgressDialog() {
        downloadProgressDialog?.dismiss()
        downloadProgressDialog = null
    }

    override fun onError(e: Exception) {
        dismissDownloadProgressDialog()

        // TODO
    }
}
