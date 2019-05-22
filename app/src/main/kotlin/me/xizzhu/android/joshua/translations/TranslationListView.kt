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
import android.view.View
import android.widget.Toast
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.*
import me.xizzhu.android.joshua.utils.BaseSettingsView

interface TranslationView : BaseSettingsView {
    fun onCurrentTranslationUpdateFailed(translationShortName: String)

    fun onTranslationsLoadingStarted()

    fun onTranslationsLoadingCompleted()

    fun onTranslationsLoadingFailed(forceRefresh: Boolean)

    fun onTranslationsUpdated(translations: List<BaseItem>)

    fun onTranslationDownloadStarted()

    fun onTranslationDownloadProgressed(progress: Int)

    fun onTranslationDownloaded()

    fun onTranslationDownloadFailed(translationToDownload: TranslationInfo)

    fun onTranslationDeleteStarted()

    fun onTranslationDeleted()

    fun onTranslationDeleteFailed(translationToDelete: TranslationInfo)
}

class TranslationListView : BaseRecyclerView, TranslationView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var presenter: TranslationPresenter

    private var downloadProgressDialog: ProgressDialog? = null
    private var deleteProgressDialog: ProgressDialog? = null
    private val onClickListener = OnClickListener { view ->
        ((getChildViewHolder(view) as TranslationItemViewHolder).item)?.let { translationItem ->
            if (translationItem.translationInfo.downloaded) {
                presenter.updateCurrentTranslation(translationItem.translationInfo.shortName)
            } else {
                presenter.downloadTranslation(translationItem.translationInfo)
            }
        }
    }
    private val onLongClickListener = OnLongClickListener { view ->
        ((getChildViewHolder(view) as TranslationItemViewHolder).item)?.let { translationItem ->
            if (translationItem.translationInfo.downloaded) {
                if (!translationItem.isCurrentTranslation) {
                    DialogHelper.showDialog(context, true, R.string.dialog_delete_translation_confirmation,
                            DialogInterface.OnClickListener { _, _ ->
                                presenter.removeTranslation(translationItem.translationInfo)
                            })
                }
            } else {
                presenter.downloadTranslation(translationItem.translationInfo)
            }
        }
        return@OnLongClickListener true
    }

    fun setPresenter(presenter: TranslationPresenter) {
        this.presenter = presenter
    }

    override fun onChildAttachedToWindow(child: View) {
        super.onChildAttachedToWindow(child)
        if (getChildViewHolder(child) is TranslationItemViewHolder) {
            child.setOnClickListener(onClickListener)
            child.setOnLongClickListener(onLongClickListener)
        }
    }

    override fun onChildDetachedFromWindow(child: View) {
        super.onChildDetachedFromWindow(child)
        child.setOnClickListener(null)
        child.setOnLongClickListener(null)
    }

    override fun onCurrentTranslationUpdateFailed(translationShortName: String) {
        DialogHelper.showDialog(context, true, R.string.dialog_update_translation_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.updateCurrentTranslation(translationShortName)
                })
    }

    override fun onTranslationsLoadingStarted() {
        visibility = GONE
    }

    override fun onTranslationsLoadingCompleted() {
        fadeIn()
    }

    override fun onTranslationsLoadingFailed(forceRefresh: Boolean) {
        DialogHelper.showDialog(context, true, R.string.dialog_load_translation_list_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.loadTranslationList(forceRefresh)
                })
    }

    override fun onTranslationsUpdated(translations: List<BaseItem>) {
        setItems(translations)
    }

    override fun onTranslationDownloadStarted() {
        downloadProgressDialog = ProgressDialog.showProgressDialog(context, R.string.dialog_downloading_translation, 100)
    }

    override fun onTranslationDownloadProgressed(progress: Int) {
        if (progress < 100) {
            downloadProgressDialog?.setProgress(progress)
        } else {
            downloadProgressDialog?.setTitle(R.string.dialog_installing_translation)
            downloadProgressDialog?.setIsIndeterminate(true)
        }
    }

    override fun onTranslationDownloaded() {
        dismissDownloadProgressDialog()
        Toast.makeText(context, R.string.toast_translation_downloaded, Toast.LENGTH_SHORT).show()
    }

    private fun dismissDownloadProgressDialog() {
        downloadProgressDialog?.dismiss()
        downloadProgressDialog = null
    }

    override fun onTranslationDownloadFailed(translationToDownload: TranslationInfo) {
        dismissDownloadProgressDialog()

        DialogHelper.showDialog(context, true, R.string.dialog_download_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.downloadTranslation(translationToDownload)
                })
    }

    override fun onTranslationDeleteStarted() {
        deleteProgressDialog = ProgressDialog.showIndeterminateProgressDialog(context, R.string.dialog_deleting_translation)
    }

    override fun onTranslationDeleted() {
        dismissDeleteProgressDialog()
        Toast.makeText(context, R.string.toast_translation_deleted, Toast.LENGTH_SHORT).show()
    }

    private fun dismissDeleteProgressDialog() {
        deleteProgressDialog?.dismiss()
        deleteProgressDialog = null
    }

    override fun onTranslationDeleteFailed(translationToDelete: TranslationInfo) {
        dismissDeleteProgressDialog()

        DialogHelper.showDialog(context, true, R.string.dialog_delete_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.removeTranslation(translationToDelete)
                })
    }
}
