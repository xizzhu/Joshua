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

class TranslationSpinnerAdapter(context: Context, private val listener: Listener,
                                private val currentTranslation: String,
                                private val translationShortNames: ArrayList<String>) : BaseAdapter() {
    interface Listener {
        fun onParallelTranslationRequested(translationShortName: String)

        fun onParallelTranslationRemoved(translationShortName: String)
    }

    private val inflater = LayoutInflater.from(context)

    private val checkBoxListener = CompoundButton.OnCheckedChangeListener { checkBox, isChecked ->
        val translationShortName = checkBox.tag as String
        if (isChecked) {
            listener.onParallelTranslationRequested(translationShortName)
        } else {
            listener.onParallelTranslationRemoved(translationShortName)
        }
    }

    override fun getCount(): Int = translationShortNames.size

    override fun getItem(position: Int): String = translationShortNames[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = (convertView
                ?: inflater.inflate(R.layout.spinner_selected, parent, false)) as TextView
        textView.text = getItem(position)
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder = if (convertView == null) {
            val holder = DropDownViewHolder(inflater.inflate(R.layout.spinner_drop_down, parent, false))
            holder.rootView.tag = holder
            holder
        } else {
            convertView.tag as DropDownViewHolder
        }

        val translationShortName = translationShortNames[position]
        viewHolder.title.text = translationShortName

        viewHolder.checkBox.setOnCheckedChangeListener(null)
        if (position < count - 1) {
            if (currentTranslation == translationShortName) {
                viewHolder.checkBox.isEnabled = false
                viewHolder.checkBox.isChecked = true
            } else {
                viewHolder.checkBox.isEnabled = true
                viewHolder.checkBox.isChecked = false
                viewHolder.checkBox.tag = translationShortName

                // Sets the listener after isChecked is updated, to avoid unwanted callback.
                viewHolder.checkBox.setOnCheckedChangeListener(checkBoxListener)
            }

            viewHolder.checkBox.visibility = View.VISIBLE
        } else {
            // Hides the check box for last item ("More")
            viewHolder.checkBox.visibility = View.INVISIBLE
        }

        return viewHolder.rootView
    }
}

private class DropDownViewHolder(val rootView: View) {
    val title: TextView = rootView.findViewById(R.id.title)
    val checkBox: CheckBox = rootView.findViewById(R.id.checkbox)
}
