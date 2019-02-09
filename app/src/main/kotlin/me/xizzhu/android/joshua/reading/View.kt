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

package me.xizzhu.android.joshua.reading

import android.os.Bundle
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.MVPView
import javax.inject.Inject

interface ReadingView : MVPView {
    fun onCurrentTranslationLoaded(currentTranslation: String)

    fun onNoCurrentTranslation()

    fun onCurrentTranslationLoadFailed()
}

class ReadingActivity : BaseActivity(), ReadingView {
    @Inject
    lateinit var presenter: ReadingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        presenter.takeView(this)
        presenter.loadCurrentTranslation()
    }

    override fun onStop() {
        presenter.dropView()
        super.onStop()
    }

    override fun onCurrentTranslationLoaded(currentTranslation: String) {
        // TODO
    }

    override fun onNoCurrentTranslation() {
        startActivity(TranslationManagementActivity.newStartIntent(this))
    }

    override fun onCurrentTranslationLoadFailed() {
        // TODO
    }
}
