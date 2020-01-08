/*
 * Copyright (C) 2020 Xizhi Zhu
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
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

class ProgressDialog private constructor(private val dialog: AlertDialog, private val progressBar: ProgressBar?) {
    companion object {
        fun showProgressDialog(context: Context, @StringRes title: Int, maxProgress: Int, onCancel: (() -> Unit)? = null): ProgressDialog? = null

        fun showIndeterminateProgressDialog(context: Context, @StringRes title: Int): ProgressDialog? = null
    }

    fun setTitle(@StringRes title: Int) {
    }

    fun setIsIndeterminate(indeterminate: Boolean) {
    }

    fun setProgress(progress: Int) {
    }

    fun dismiss() {
    }
}
