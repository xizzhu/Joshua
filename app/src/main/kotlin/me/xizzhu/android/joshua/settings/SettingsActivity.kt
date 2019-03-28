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

package me.xizzhu.android.joshua.settings

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.SwitchCompat
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.settings.widgets.SettingButton
import me.xizzhu.android.joshua.settings.widgets.SettingSectionHeader
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.MVPView
import javax.inject.Inject
import kotlin.math.roundToInt

interface SettingsView : MVPView {
    fun onVersionLoaded(version: String)

    fun onSettingsUpdated(settings: Settings)

    fun onSettingsUpdateFailed(settingsToUpdate: Settings)
}

class SettingsActivity : BaseActivity(), SettingsView {
    private val fontSizeTexts: Array<CharSequence> = arrayOf(".5x", "1x", "1.5x", "2x", "2.5x", "3x")

    @Inject
    lateinit var presenter: SettingsPresenter

    private lateinit var display: SettingSectionHeader
    private lateinit var fontSize: SettingButton
    private lateinit var keepScreenOn: SwitchCompat
    private lateinit var nightModeOn: SwitchCompat
    private lateinit var about: SettingSectionHeader
    private lateinit var version: SettingButton

    private var shouldAnimateFontSize = false
    private var shouldAnimateColor = false
    private var originalSettings: Settings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        display = findViewById(R.id.display)

        fontSize = findViewById(R.id.font_size)
        fontSize.setOnClickListener {
            DialogHelper.showDialog(this@SettingsActivity, R.string.settings_title_font_size,
                    fontSizeTexts, presenter.getSettings().fontSizeScale - 1,
                    DialogInterface.OnClickListener { dialog, which ->
                        originalSettings = presenter.getSettings()
                        shouldAnimateFontSize = true
                        presenter.setFontSizeScale(which + 1)

                        dialog.dismiss()
                    })
        }

        keepScreenOn = findViewById(R.id.keep_screen_on)
        keepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            presenter.setKeepScreenOn(isChecked)
        }

        nightModeOn = findViewById(R.id.night_mode_on)
        nightModeOn.setOnCheckedChangeListener { _, isChecked ->
            originalSettings = presenter.getSettings()
            shouldAnimateColor = true
            presenter.setNightModeOn(isChecked)
        }

        about = findViewById(R.id.about)
        version = findViewById(R.id.version)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView()
        super.onStop()
    }

    override fun onVersionLoaded(version: String) {
        this.version.setDescription(version)
    }

    override fun onSettingsUpdated(settings: Settings) {
        window.decorView.keepScreenOn = settings.keepScreenOn

        val resources = resources
        if (shouldAnimateFontSize) {
            shouldAnimateFontSize = false

            animateTextSize(originalSettings!!.getBodyTextSize(resources), settings.getBodyTextSize(resources),
                    originalSettings!!.getCaptionTextSize(resources), settings.getCaptionTextSize(resources))
        } else {
            updateTextSize(settings.getBodyTextSize(resources), settings.getCaptionTextSize(resources))
        }

        if (shouldAnimateColor) {
            shouldAnimateColor = false

            animateColor(originalSettings!!.getBackgroundColor(), settings.getBackgroundColor(),
                    originalSettings!!.getPrimaryTextColor(resources), settings.getPrimaryTextColor(resources),
                    originalSettings!!.getSecondaryTextColor(resources), settings.getSecondaryTextColor(resources))
        } else {
            updateColor(settings.getBackgroundColor(), settings.getPrimaryTextColor(resources),
                    settings.getSecondaryTextColor(resources))
        }

        fontSize.setDescription(fontSizeTexts[settings.fontSizeScale - 1])
        keepScreenOn.isChecked = settings.keepScreenOn
        nightModeOn.isChecked = settings.nightModeOn
    }

    private fun animateColor(@ColorInt fromBackgroundColor: Int, @ColorInt toBackgroundColor: Int,
                             @ColorInt fromPrimaryTextColor: Int, @ColorInt toPrimaryTextColor: Int,
                             @ColorInt fromSecondaryTextColor: Int, @ColorInt toSecondaryTextColor: Int) {
        val argbEvaluator = ArgbEvaluator()
        val colorAnimator = ValueAnimator.ofFloat(0.0F, 1.0F)
        colorAnimator.addUpdateListener { animator ->
            val fraction = animator.animatedValue as Float
            val backgroundColor = argbEvaluator.evaluate(fraction, fromBackgroundColor, toBackgroundColor) as Int
            val primaryTextColor = argbEvaluator.evaluate(fraction, fromPrimaryTextColor, toPrimaryTextColor) as Int
            val secondaryTextColor = argbEvaluator.evaluate(fraction, fromSecondaryTextColor, toSecondaryTextColor) as Int
            updateColor(backgroundColor, primaryTextColor, secondaryTextColor)
        }
        colorAnimator.start()
    }

    private fun updateColor(@ColorInt backgroundColor: Int, @ColorInt primaryTextColor: Int,
                            @ColorInt secondaryTextColor: Int) {
        window.decorView.setBackgroundColor(backgroundColor)

        fontSize.setTextColor(primaryTextColor, secondaryTextColor)
        keepScreenOn.setTextColor(primaryTextColor)
        nightModeOn.setTextColor(primaryTextColor)
        version.setTextColor(primaryTextColor, secondaryTextColor)
    }

    private fun animateTextSize(@Px fromBodyTextSize: Int, @Px toBodyTextSize: Int,
                                @Px fromCaptionTextSize: Int, @Px toCaptionTextSize: Int) {
        val argbEvaluator = ArgbEvaluator()
        val textSizeAnimator = ValueAnimator.ofFloat(0.0F, 1.0F)
        textSizeAnimator.addUpdateListener { animator ->
            val fraction = animator.animatedValue as Float
            val bodyTextSize = fromBodyTextSize + fraction * (toBodyTextSize - fromBodyTextSize)
            val captionTextSize = fromCaptionTextSize + fraction * (toCaptionTextSize - fromCaptionTextSize)
            updateTextSize(bodyTextSize.roundToInt(), captionTextSize.roundToInt())
        }
        textSizeAnimator.start()
    }

    private fun updateTextSize(@Px bodyTextSize: Int, @Px captionTextSize: Int) {
        display.setTextSize(bodyTextSize)
        fontSize.setTextSize(bodyTextSize, captionTextSize)
        keepScreenOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize.toFloat())
        nightModeOn.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize.toFloat())
        about.setTextSize(bodyTextSize)
        version.setTextSize(bodyTextSize, captionTextSize)
    }

    override fun onSettingsUpdateFailed(settingsToUpdate: Settings) {
        DialogHelper.showDialog(this, true, R.string.dialog_update_settings_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.saveSettings(settingsToUpdate)
                })
    }
}
