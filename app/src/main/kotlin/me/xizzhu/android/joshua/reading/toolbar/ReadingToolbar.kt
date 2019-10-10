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
import android.util.AttributeSet
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import me.xizzhu.android.joshua.R

class ReadingToolbar : Toolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setTitle(R.string.app_name)
        inflateMenu(R.menu.menu_bible_reading)
    }

    fun initializeSpinner(adapter: TranslationSpinnerAdapter, listener: AdapterView.OnItemSelectedListener) {
        with(spinner()) {
            this.adapter = adapter
            this.onItemSelectedListener = listener
        }
    }

    private fun spinner(): Spinner = menu.findItem(R.id.action_translations).actionView as Spinner

    fun setSpinnerSelection(position: Int) {
        spinner().setSelection(position)
    }
}
