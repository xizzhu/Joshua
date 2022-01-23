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

package me.xizzhu.android.joshua.ui

import android.content.res.Resources
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.*
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings

fun Settings.getPrimaryTextSize(resources: Resources): Float =
        getTextSize(resources, R.dimen.text_primary) * fontSizeScale

private val textSizes = mutableMapOf<Int, Float>()

private fun getTextSize(resources: Resources, @DimenRes fontSize: Int): Float {
    return textSizes.getOrPut(fontSize, { resources.getDimension(fontSize) })
}

fun Settings.getSecondaryTextSize(resources: Resources): Float =
        getTextSize(resources, R.dimen.text_secondary) * fontSizeScale

fun TextView.setPrimaryTextSize(settings: Settings) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getPrimaryTextSize(resources))
}

fun TextView.setSecondaryTextSize(settings: Settings) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getSecondaryTextSize(resources))
}
