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
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SwitchCompat
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.settings.widgets.SettingButton
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.getBackgroundColor
import me.xizzhu.android.joshua.ui.getPrimaryTextColor
import me.xizzhu.android.joshua.ui.getSecondaryTextColor
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.MVPView
import javax.inject.Inject

interface SettingsView : MVPView {
    fun onVersionLoaded(version: String)

    fun onSettingsUpdated(settings: Settings)

    fun onSettingsUpdateFailed(settingsToUpdate: Settings)
}

class SettingsActivity : BaseActivity(), SettingsView {
    @Inject
    lateinit var presenter: SettingsPresenter

    private lateinit var keepScreenOn: SwitchCompat
    private lateinit var nightModeOn: SwitchCompat
    private lateinit var version: SettingButton

    private var shouldAnimateColor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        version = findViewById(R.id.version)
        keepScreenOn = findViewById(R.id.keep_screen_on)
        keepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            presenter.setKeepScreenOn(isChecked)
        }
        nightModeOn = findViewById(R.id.night_mode_on)
        nightModeOn.setOnCheckedChangeListener { _, isChecked ->
            shouldAnimateColor = true
            presenter.setNightModeOn(isChecked)
        }
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
        if (shouldAnimateColor) {
            val fromBackgroundColor: Int
            val toBackgroundColor: Int
            val fromPrimaryTextColor: Int
            val toPrimaryTextColor: Int
            val fromSecondaryTextColor: Int
            val toSecondaryTextColor: Int
            if (settings.nightModeOn) {
                fromBackgroundColor = Color.WHITE
                toBackgroundColor = Color.BLACK

                val resources = resources
                fromPrimaryTextColor = resources.getColor(R.color.text_dark_primary)
                toPrimaryTextColor = resources.getColor(R.color.text_light_primary)
                fromSecondaryTextColor = resources.getColor(R.color.text_dark_secondary)
                toSecondaryTextColor = resources.getColor(R.color.text_light_secondary)
            } else {
                fromBackgroundColor = Color.BLACK
                toBackgroundColor = Color.WHITE

                val resources = resources
                fromPrimaryTextColor = resources.getColor(R.color.text_light_primary)
                toPrimaryTextColor = resources.getColor(R.color.text_dark_primary)
                fromSecondaryTextColor = resources.getColor(R.color.text_light_secondary)
                toSecondaryTextColor = resources.getColor(R.color.text_dark_secondary)
            }
            animateColor(fromBackgroundColor, toBackgroundColor, fromPrimaryTextColor, toPrimaryTextColor,
                    fromSecondaryTextColor, toSecondaryTextColor)
        } else {
            val resources = resources
            updateColor(settings.getBackgroundColor(), settings.getPrimaryTextColor(resources),
                    settings.getSecondaryTextColor(resources))
        }

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

        keepScreenOn.setTextColor(primaryTextColor)
        nightModeOn.setTextColor(primaryTextColor)
        version.setTextColor(primaryTextColor, secondaryTextColor)
    }

    override fun onSettingsUpdateFailed(settingsToUpdate: Settings) {
        DialogHelper.showDialog(this, true, R.string.dialog_update_settings_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.saveSettings(settingsToUpdate)
                })
    }
}
