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

package me.xizzhu.android.joshua.reading.toolbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import me.xizzhu.android.joshua.R

class TranslationSpinnerAdapter(context: Context, onParallelTranslationRequested: (String) -> Unit,
                                onParallelTranslationRemoved: (String) -> Unit) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)

    private val checkBoxListener = CompoundButton.OnCheckedChangeListener { checkBox, isChecked ->
        val translationShortName = checkBox.tag as String
        if (isChecked) {
            onParallelTranslationRequested(translationShortName)
        } else {
            onParallelTranslationRemoved(translationShortName)
        }
    }

    private var currentTranslation: String = ""
    private val parallelTranslations: MutableSet<String> = mutableSetOf()
    private val translationShortNames: MutableList<String> = mutableListOf()

    fun setCurrentTranslation(currentTranslation: String) {
        this.currentTranslation = currentTranslation
        notifyDataSetChanged()
    }

    fun setTranslationShortNames(translationShortNames: List<String>) {
        this.translationShortNames.clear()
        this.translationShortNames.addAll(translationShortNames)
        notifyDataSetChanged()
    }

    fun setParallelTranslations(parallelTranslations: List<String>) {
        this.parallelTranslations.clear()
        this.parallelTranslations.addAll(parallelTranslations)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = translationShortNames.size

    override fun getItem(position: Int): String = translationShortNames[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (convertView ?: inflater.inflate(R.layout.spinner_selected, parent, false))
                    .apply { (this as TextView).text = getItem(position) }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
                ?: inflater.inflate(R.layout.spinner_drop_down, parent, false).apply { tag = DropDownViewHolder(this) }
        with(view.tag as DropDownViewHolder) {
            val translationShortName = translationShortNames[position]
            title.text = translationShortName

            // Nullify the listener to avoid unwanted callback when updating isChecked.
            checkBox.setOnCheckedChangeListener(null)

            if (position < count - 1) {
                if (currentTranslation == translationShortName) {
                    checkBox.isEnabled = false
                    checkBox.isChecked = true
                } else {
                    checkBox.isEnabled = true
                    checkBox.isChecked = parallelTranslations.contains(translationShortName)
                    checkBox.tag = translationShortName

                    // Sets the listener after isChecked is updated.
                    checkBox.setOnCheckedChangeListener(checkBoxListener)
                }

                checkBox.visibility = View.VISIBLE
            } else {
                // It's the last item ("More"), hide the check box.
                checkBox.visibility = View.INVISIBLE
            }
        }
        return view
    }
}

private class DropDownViewHolder(rootView: View) {
    val title: TextView = rootView.findViewById(R.id.title)
    val checkBox: CheckBox = rootView.findViewById(R.id.checkbox)
}
