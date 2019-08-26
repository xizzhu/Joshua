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

package me.xizzhu.android.joshua.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import me.xizzhu.android.joshua.R
import kotlin.math.min
import kotlin.math.roundToInt

class ReadingProgressBar : View {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val backGroundPaint: Paint = Paint()
    private val progressPaint: Paint = Paint()
    private val fullProgressPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val textPadding: Int

    var maxProgress: Int = 100
        set(value) {
            field = value
            postInvalidate()
        }
    var progress: Int = 0
        set(value) {
            field = value
            postInvalidate()
        }
    var text = ""
        set(value) {
            field = value
            postInvalidate()
        }

    init {
        val resources = context.resources
        backGroundPaint.color = Color.LTGRAY
        progressPaint.color = ContextCompat.getColor(context, R.color.dark_cyan)
        fullProgressPaint.color = ContextCompat.getColor(context, R.color.dark_lime)
        textPaint.isAntiAlias = true
        textPaint.color = Color.BLACK
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = resources.getDimension(R.dimen.text_body)
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPadding = (1.5 * resources.getDimensionPixelSize(R.dimen.padding_small)).roundToInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
                || (heightSpecMode != MeasureSpec.AT_MOST && heightSpecMode != MeasureSpec.UNSPECIFIED)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        // we only use match_parent for width, and wrap_content for height
        val height = textPaint.textSize.roundToInt() + 2 * textPadding
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                if (heightSpecMode == MeasureSpec.UNSPECIFIED) height else min(height, MeasureSpec.getSize(heightMeasureSpec)))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft.toFloat()
        val paddingRight = paddingRight.toFloat()
        val paddingTop = paddingTop.toFloat()
        val width = width.toFloat()
        val right = width - paddingRight
        val height = height.toFloat()
        val bottom = height - paddingBottom
        when {
            progress <= 0 -> canvas.drawRect(paddingLeft, paddingTop, right, bottom, backGroundPaint)
            progress >= maxProgress -> canvas.drawRect(paddingLeft, paddingTop, right, bottom, fullProgressPaint)
            else -> {
                val middle = progress * (width - paddingLeft - paddingRight) / maxProgress
                canvas.drawRect(paddingLeft, paddingTop, middle, bottom, progressPaint)
                canvas.drawRect(middle, paddingTop, right, bottom, backGroundPaint)
            }
        }

        canvas.drawText(text, width - textPadding, height - textPadding, textPaint)
    }
}
