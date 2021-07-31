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
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

class ProgressDialog(private val dialog: AlertDialog, private val progressBar: ProgressBar) {
    fun setTitle(@StringRes title: Int) {}

    fun setIsIndeterminate(indeterminate: Boolean) {}

    fun setProgress(progress: Int) {}

    fun dismiss() {}

    fun isShowing(): Boolean = false
}

fun Activity.dialog(@StringRes title: Int, items: Array<String>, selected: Int, onClicked: DialogInterface.OnClickListener) {}

fun Activity.dialog(cancelable: Boolean, @StringRes message: Int, onPositive: DialogInterface.OnClickListener, onNegative: DialogInterface.OnClickListener? = null) {}

fun Activity.dialog(cancelable: Boolean, message: CharSequence, onPositive: DialogInterface.OnClickListener, onNegative: DialogInterface.OnClickListener? = null) {}

fun Activity.progressDialog(@StringRes title: Int, maxProgress: Int, onCancel: () -> Unit): ProgressDialog? = null

fun Activity.indeterminateProgressDialog(@StringRes title: Int): AlertDialog? = null
