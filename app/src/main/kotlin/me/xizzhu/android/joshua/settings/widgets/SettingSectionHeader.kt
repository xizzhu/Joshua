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

package me.xizzhu.android.joshua.settings.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import me.xizzhu.android.joshua.R

class SettingSectionHeader : FrameLayout {
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private lateinit var title: TextView

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.inner_setting_section_header, this)
        title = findViewById(R.id.title)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.SettingSectionHeader)
            val titleResourceId = a.getResourceId(R.styleable.SettingSectionHeader_settingSectionHeaderTitle, -1)
            if (titleResourceId != -1) {
                title.setText(titleResourceId)
            } else {
                val titleText = a.getString(R.styleable.SettingSectionHeader_settingSectionHeaderTitle)
                if (!TextUtils.isEmpty(titleText)) {
                    title.text = titleText
                }
            }
            a.recycle()
        }
    }
}
