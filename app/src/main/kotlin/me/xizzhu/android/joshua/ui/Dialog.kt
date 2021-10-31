/*
 * Copyright (C) 2021 Xizhi Zhu
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

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import me.xizzhu.android.joshua.R

class ProgressDialog(private val dialog: AlertDialog, private val progressBar: ProgressBar) {
    fun setTitle(@StringRes title: Int) {
        dialog.setTitle(title)
    }

    fun setIsIndeterminate(indeterminate: Boolean) {
        progressBar.isIndeterminate = indeterminate
    }

    fun setProgress(progress: Int) {
        progressBar.progress = progress
    }

    fun dismiss() {
        try {
            dialog.dismiss()
        } catch (_: Exception) {
            // We don't care if it fails.
        }
    }

    fun isShowing(): Boolean = dialog.isShowing
}

fun Activity.dialog(@StringRes title: Int, items: Array<String>, selected: Int,
                    onClicked: DialogInterface.OnClickListener) {
    if (isDestroyed) return

    AlertDialog.Builder(this)
            .setCancelable(true)
            .setSingleChoiceItems(items, selected, onClicked)
            .setTitle(title)
            .show()
}

fun Activity.dialog(cancelable: Boolean, @StringRes message: Int,
                    onPositive: DialogInterface.OnClickListener, onNegative: DialogInterface.OnClickListener? = null) {
    if (isDestroyed) return

    AlertDialog.Builder(this)
            .setCancelable(cancelable)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, onPositive)
            .setNegativeButton(android.R.string.cancel, onNegative)
            .show()
}

fun Activity.dialog(cancelable: Boolean, message: CharSequence,
                    onPositive: DialogInterface.OnClickListener, onNegative: DialogInterface.OnClickListener? = null) {
    if (isDestroyed) return

    AlertDialog.Builder(this)
            .setCancelable(cancelable)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, onPositive)
            .setNegativeButton(android.R.string.cancel, onNegative)
            .show()
}

fun Activity.progressDialog(@StringRes title: Int, maxProgress: Int, onCancel: () -> Unit): ProgressDialog? {
    val progressBar = (View.inflate(this, R.layout.widget_progress_bar, null) as ProgressBar)
            .apply { max = maxProgress }
    return ProgressDialog(
            AlertDialog.Builder(this)
                    .setTitle(title)
                    .setView(progressBar)
                    .setCancelable(true)
                    .setOnCancelListener { onCancel() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> onCancel() }
                    .create()
                    .apply { show() },
            progressBar)
}

fun Activity.indeterminateProgressDialog(@StringRes title: Int): AlertDialog? {
    if (isDestroyed) return null

    return AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(title)
            .setView(View.inflate(this, R.layout.widget_indeterminate_progress_bar, null))
            .create()
            .apply { show() }
}
