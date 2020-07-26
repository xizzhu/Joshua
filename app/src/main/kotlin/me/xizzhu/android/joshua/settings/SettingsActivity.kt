/*
 * Copyright (C) 2020 Xizhi Zhu
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
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.activity.BaseActivity
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {
    @Inject
    lateinit var settingsViewModel: SettingsViewModel

    @Inject
    lateinit var settingsPresenter: SettingsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        settingsPresenter.bind(
                SettingsViewHolder(
                        findViewById(R.id.display), findViewById(R.id.font_size), findViewById(R.id.keep_screen_on),
                        findViewById(R.id.night_mode_on), findViewById(R.id.reading), findViewById(R.id.simple_reading_mode),
                        findViewById(R.id.hide_search_button), findViewById(R.id.consolidated_sharing),
                        findViewById(R.id.default_highlight_color), findViewById(R.id.backup_restore), findViewById(R.id.backup),
                        findViewById(R.id.restore), findViewById(R.id.about), findViewById(R.id.rate), findViewById(R.id.version)
                )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SettingsPresenter.CODE_CREATE_DOCUMENT_FOR_BACKUP -> settingsPresenter.onCreateDocumentForBackup(resultCode, data)
            SettingsPresenter.CODE_GET_CONTENT_FOR_RESTORE -> settingsPresenter.onGetContentForRestore(resultCode, data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
