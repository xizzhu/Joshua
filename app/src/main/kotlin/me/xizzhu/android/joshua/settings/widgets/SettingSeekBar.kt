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
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatSeekBar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.ui.setBackground
import me.xizzhu.android.joshua.ui.setText

class SettingSeekBar : LinearLayout {
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
    private lateinit var value: TextView
    private lateinit var seekBar: AppCompatSeekBar

    private fun init(context: Context, attrs: AttributeSet?) {
        setBackground(android.R.attr.selectableItemBackground)
        minimumHeight = context.resources.getDimensionPixelSize(R.dimen.item_height)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        inflate(context, R.layout.inner_setting_seek_bar, this)
        title = findViewById(R.id.title)
        value = findViewById(R.id.value)
        seekBar = findViewById(R.id.seek_bar)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.SettingSeekBar)
            title.setText(a, R.styleable.SettingSeekBar_settingSeekBarTitle)
            a.recycle()
        }
    }

    fun setListener(listener: SeekBar.OnSeekBarChangeListener) {
        seekBar.setOnSeekBarChangeListener(listener)
    }

    fun setTextColor(@ColorInt titleColor: Int, @ColorInt valueColor: Int) {
        title.setTextColor(titleColor)
        value.setTextColor(valueColor)
    }

    fun setTextSize(@Px titleSize: Int, @Px valueSize: Int) {
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
        value.setTextSize(TypedValue.COMPLEX_UNIT_PX, valueSize.toFloat())
    }

    fun setMax(max: Int) {
        seekBar.max = max
    }

    fun setValue(valueInt: Int, valueText: String) {
        seekBar.progress = valueInt
        value.text = valueText
    }
}
