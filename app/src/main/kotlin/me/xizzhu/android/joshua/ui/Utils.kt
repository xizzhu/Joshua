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

package me.xizzhu.android.joshua.ui

import android.content.res.TypedArray
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.StyleableRes

fun TextView.setText(a: TypedArray, @StyleableRes index: Int) {
    val resourceId = a.getResourceId(index, -1)
    if (resourceId != -1) {
        setText(resourceId)
    } else {
        val text = a.getString(index)
        if (!TextUtils.isEmpty(text)) {
            setText(text)
        }
    }
}

fun View.setBackground(resId: Int) {
    val out = TypedValue()
    context.theme.resolveAttribute(resId, out, true)
    setBackgroundResource(out.resourceId)
}