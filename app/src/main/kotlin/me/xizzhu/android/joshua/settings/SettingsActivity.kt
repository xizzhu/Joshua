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

import android.content.Intent
import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewModel
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.utils.activities.BaseActivity
import javax.inject.Inject

class SettingsActivity : BaseActivity() {
    @Inject
    lateinit var settingsViewModel: SettingsViewModel

    @Inject
    lateinit var settingsViewPresenter: SettingsViewPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        settingsViewPresenter.bind(SettingsViewHolder(
                findViewById(R.id.display), findViewById(R.id.font_size), findViewById(R.id.keep_screen_on),
                findViewById(R.id.night_mode_on), findViewById(R.id.reading), findViewById(R.id.simple_reading_mode),
                findViewById(R.id.backup_restore), findViewById(R.id.backup), findViewById(R.id.restore),
                findViewById(R.id.about), findViewById(R.id.rate), findViewById(R.id.version)
        ))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SettingsViewPresenter.CODE_GET_CONTENT_FOR_RESTORE -> settingsViewPresenter.onGetContentForRestore(resultCode, data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun getViewModel(): ViewModel = settingsViewModel

    override fun getViewPresenters(): List<ViewPresenter<out ViewHolder, out Interactor>> = listOf(settingsViewPresenter)
}
