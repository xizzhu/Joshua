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
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import me.xizzhu.android.joshua.R

class ProgressDialog private constructor(private val dialog: AlertDialog, private val progressBar: ProgressBar?) {
    companion object {
        fun showProgressDialog(context: Context, @StringRes title: Int, maxProgress: Int): ProgressDialog {
            val progressBar = View.inflate(context, R.layout.widget_progress_bar, null) as ProgressBar
            progressBar.max = maxProgress
            val dialog = AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle(title)
                    .setView(progressBar)
                    .create()
            dialog.show()
            return ProgressDialog(dialog, progressBar)
        }

        fun showIndeterminateProgressDialog(context: Context, @StringRes title: Int): ProgressDialog {
            val dialog = AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle(title)
                    .setView(View.inflate(context, R.layout.widget_indeterminate_progress_bar, null))
                    .create()
            dialog.show()
            return ProgressDialog(dialog, null)
        }
    }

    fun setTitle(@StringRes title: Int) {
        dialog.setTitle(title)
    }

    fun setIsIndeterminate(indeterminate: Boolean) {
        progressBar?.isIndeterminate = indeterminate
    }

    fun setProgress(progress: Int) {
        progressBar?.progress = progress
    }

    fun dismiss() {
        dialog.dismiss()
    }
}
