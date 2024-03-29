/*
 * Copyright (C) 2023 Xizhi Zhu
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
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.databinding.WidgetRecyclerViewBinding
import me.xizzhu.android.joshua.databinding.WidgetSeekBarBinding
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import kotlin.math.roundToInt
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerView

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
}

fun Activity.dialog(
    cancelable: Boolean,
    @StringRes title: Int,
    @StringRes message: Int,
    onPositive: DialogInterface.OnClickListener,
    onNegative: DialogInterface.OnClickListener? = null,
    onDismiss: DialogInterface.OnDismissListener? = null
) {
    if (isDestroyed) return

    MaterialAlertDialogBuilder(this)
        .setCancelable(cancelable)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, onPositive)
        .setNegativeButton(android.R.string.cancel, onNegative)
        .setOnDismissListener(onDismiss)
        .show()
}

fun Activity.dialog(cancelable: Boolean, title: CharSequence, @StringRes message: Int,
                    onPositive: DialogInterface.OnClickListener, onNegative: DialogInterface.OnClickListener? = null) {
    if (isDestroyed) return

    MaterialAlertDialogBuilder(this)
        .setCancelable(cancelable)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, onPositive)
        .setNegativeButton(android.R.string.cancel, onNegative)
        .show()
}

fun Activity.progressDialog(@StringRes title: Int, maxProgress: Int, onCancel: () -> Unit): ProgressDialog? {
    if (isDestroyed) return null

    val progressBar = (View.inflate(this, R.layout.widget_progress_bar, null) as ProgressBar)
        .apply { max = maxProgress }
    return ProgressDialog(
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(progressBar)
            .setCancelable(true)
            .setOnCancelListener { onCancel() }
            .setNegativeButton(android.R.string.cancel) { _, _ -> onCancel() }
            .show(),
        progressBar)
}

fun Activity.indeterminateProgressDialog(@StringRes title: Int): AlertDialog? {
    if (isDestroyed) return null

    return MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setTitle(title)
        .setView(View.inflate(this, R.layout.widget_indeterminate_progress_bar, null))
        .show()
}

fun Activity.seekBarDialog(
    @StringRes title: Int,
    initialValue: Float,
    minValue: Float,
    maxValue: Float,
    onValueChanged: (Float) -> Unit,
    onPositive: ((Float) -> Unit)? = null,
    onNegative: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
): AlertDialog? {
    if (isDestroyed) return null

    val viewBinding = WidgetSeekBarBinding.inflate(LayoutInflater.from(this))
    viewBinding.seekBar.setProgress(initialValue, minValue, maxValue)
    viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) onValueChanged(viewBinding.seekBar.calculateValue(minValue, maxValue))
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })
    return MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setTitle(title)
        .setView(viewBinding.root)
        .setPositiveButton(android.R.string.ok, onPositive?.let { { _, _ -> it(viewBinding.seekBar.calculateValue(minValue, maxValue)) } })
        .setNegativeButton(android.R.string.cancel, onNegative?.let { { _, _ -> it() } })
        .setOnDismissListener { onDismiss?.invoke() }
        .show()
}

private fun SeekBar.setProgress(value: Float, minValue: Float, maxValue: Float) {
    progress = (max * (value - minValue) / (maxValue - minValue)).roundToInt()
}

private fun SeekBar.calculateValue(minValue: Float, maxValue: Float): Float =
    minValue + (maxValue - minValue) * progress.toFloat() / max.toFloat()

fun Activity.listDialog(
    title: CharSequence,
    adapter: RecyclerView.Adapter<*>,
    scrollToPosition: Int,
    onDismiss: (() -> Unit)? = null,
): AlertDialog? {
    if (isDestroyed) return null

    val recyclerView = VerticalRecyclerView(this).apply {
        this.adapter = adapter
        if (scrollToPosition != RecyclerView.NO_POSITION) {
            scrollToPosition(scrollToPosition)
        }
    }
    val builder = MaterialAlertDialogBuilder(this)
        .setCancelable(true)
        .setTitle(title)
        .setView(recyclerView)
    onDismiss?.let { builder.setOnDismissListener { onDismiss() } }
    return builder.show()
}

fun Activity.listDialog(
    title: CharSequence,
    settings: Settings,
    items: List<BaseItem>,
    selected: Int,
    onDismiss: DialogInterface.OnDismissListener? = null
): AlertDialog? {
    if (isDestroyed) return null

    val viewBinding = WidgetRecyclerViewBinding.inflate(LayoutInflater.from(this))
    viewBinding.recyclerView.setSettings(settings)
    viewBinding.recyclerView.setItems(items)
    viewBinding.recyclerView.scrollToPosition(selected)
    return MaterialAlertDialogBuilder(this)
        .setCancelable(true)
        .setTitle(title)
        .setView(viewBinding.root)
        .setOnDismissListener(onDismiss)
        .show()
}

fun Activity.listDialog(
    @StringRes title: Int,
    items: Array<String>,
    selected: Int,
    onSelected: (which: Int) -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    if (isDestroyed) return

    val builder = MaterialAlertDialogBuilder(this)
        .setCancelable(true)
        .setSingleChoiceItems(items, selected) { dialog, which ->
            dialog.dismiss()
            onSelected(which)
        }
        .setTitle(title)
    onDismiss?.let { builder.setOnDismissListener { onDismiss() } }
    builder.show()
}
