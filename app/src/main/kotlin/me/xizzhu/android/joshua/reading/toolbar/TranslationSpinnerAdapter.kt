/*
 * Copyright (C) 2022 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading.toolbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.databinding.SpinnerDropDownBinding
import me.xizzhu.android.joshua.databinding.SpinnerSelectedBinding
import me.xizzhu.android.joshua.ui.setOnCheckedChangeByUserListener

class TranslationSpinnerAdapter(
    context: Context,
    private val requestParallelTranslation: (translationShortName: String) -> Unit,
    private val removeParallelTranslation: (translationShortName: String) -> Unit,
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    private val items: ArrayList<TranslationItem> = arrayListOf()

    fun setItems(items: List<TranslationItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): TranslationItem = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = convertView?.let { SpinnerSelectedBinding.bind(it) }
            ?: SpinnerSelectedBinding.inflate(inflater, parent, false)
        binding.root.text = (getItem(position) as? TranslationItem.Translation)?.translationShortName
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = getDropDownViewBinding(convertView, parent)
        when (val item = getItem(position)) {
            is TranslationItem.Translation -> binding.bind(item)
            is TranslationItem.More -> binding.bind(item)
        }
        return binding.root
    }

    private fun getDropDownViewBinding(convertView: View?, parent: ViewGroup): SpinnerDropDownBinding {
        if (convertView != null) {
            return SpinnerDropDownBinding.bind(convertView)
        }

        val binding = SpinnerDropDownBinding.inflate(inflater, parent, false)
        binding.checkbox.setOnCheckedChangeByUserListener { isChecked ->
            when (val item = binding.root.tag as TranslationItem) {
                is TranslationItem.Translation -> {
                    if (isChecked) {
                        requestParallelTranslation(item.translationShortName)
                    } else {
                        removeParallelTranslation(item.translationShortName)
                    }
                }
                is TranslationItem.More -> {
                    // Do nothing
                }
            }
        }
        return binding
    }

    private fun SpinnerDropDownBinding.bind(item: TranslationItem.Translation) {
        root.tag = item

        title.text = item.translationShortName
        checkbox.isVisible = true
        checkbox.isEnabled = !item.isCurrentTranslation
        checkbox.isChecked = item.isCurrentTranslation or item.isParallelTranslation
    }

    private fun SpinnerDropDownBinding.bind(item: TranslationItem.More) {
        root.tag = item

        title.setText(R.string.action_more_translation)
        checkbox.isInvisible = true
        checkbox.isEnabled = false
        checkbox.isChecked = false
    }
}

sealed class TranslationItem {
    data class Translation(val translationShortName: String, val isCurrentTranslation: Boolean, val isParallelTranslation: Boolean) : TranslationItem()
    object More : TranslationItem()
}
