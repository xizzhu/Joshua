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
import android.widget.CompoundButton
import me.xizzhu.android.joshua.databinding.SpinnerDropDownBinding
import me.xizzhu.android.joshua.databinding.SpinnerSelectedBinding

class TranslationSpinnerAdapter(
        context: Context, requestParallelTranslation: (String) -> Unit, removeParallelTranslation: (String) -> Unit
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)

    private val checkBoxListener = CompoundButton.OnCheckedChangeListener { checkBox, isChecked ->
        val translationShortName = checkBox.tag as String
        if (isChecked) {
            requestParallelTranslation(translationShortName)
        } else {
            removeParallelTranslation(translationShortName)
        }
    }

    private var currentTranslation: String = ""
    private val parallelTranslations: MutableSet<String> = HashSet()
    private val downloadedTranslations: MutableList<String> = ArrayList()

    fun setData(currentTranslation: String, parallelTranslations: List<String>, downloadedTranslations: List<String>) {
        this.currentTranslation = currentTranslation

        this.parallelTranslations.clear()
        this.parallelTranslations.addAll(parallelTranslations)

        this.downloadedTranslations.clear()
        this.downloadedTranslations.addAll(downloadedTranslations)

        notifyDataSetChanged()
    }

    override fun getCount(): Int = downloadedTranslations.size

    override fun getItem(position: Int): String = downloadedTranslations[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (convertView?.let { SpinnerSelectedBinding.bind(it) } ?: SpinnerSelectedBinding.inflate(inflater, parent, false))
                    .apply { root.text = getItem(position) }
                    .root

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val spinner = convertView?.let { SpinnerDropDownBinding.bind(it) } ?: SpinnerDropDownBinding.inflate(inflater, parent, false)

        val translationShortName = downloadedTranslations[position]
        spinner.title.text = translationShortName

        // Nullify the listener to avoid unwanted callback when updating isChecked.
        spinner.checkbox.setOnCheckedChangeListener(null)

        if (position < count - 1) {
            if (currentTranslation == translationShortName) {
                spinner.checkbox.isEnabled = false
                spinner.checkbox.isChecked = true
            } else {
                spinner.checkbox.isEnabled = true
                spinner.checkbox.isChecked = parallelTranslations.contains(translationShortName)
                spinner.checkbox.tag = translationShortName

                // Sets the listener after isChecked is updated.
                spinner.checkbox.setOnCheckedChangeListener(checkBoxListener)
            }

            spinner.checkbox.visibility = View.VISIBLE
        } else {
            // It's the last item ("More"), hide the check box.
            spinner.checkbox.visibility = View.INVISIBLE
        }

        return spinner.root
    }
}
