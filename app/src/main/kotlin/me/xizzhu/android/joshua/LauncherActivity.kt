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

package me.xizzhu.android.joshua

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.*
import me.xizzhu.android.joshua.model.TranslationManager
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LauncherActivity : Activity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var job: Job

    @Inject
    lateinit var translationManager: TranslationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        App.appComponent.inject(this)
        super.onCreate(savedInstanceState)

        job = Job()
        launch(Dispatchers.Main) {
            val hasTranslationsInstalled = withContext(Dispatchers.IO) {
                translationManager.hasTranslationsInstalled()
            }
            if (!hasTranslationsInstalled) {
                val startIntent = TranslationManagementActivity.newStartIntent(this@LauncherActivity)
                startActivity(startIntent)
            }
            finish()
        }
    }

    override fun onDestroy() {
        job.cancel()

        super.onDestroy()
    }
}
