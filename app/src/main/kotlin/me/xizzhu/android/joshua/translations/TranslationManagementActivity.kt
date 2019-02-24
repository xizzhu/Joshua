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

package me.xizzhu.android.joshua.translations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.onEach
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.fadeOut
import javax.inject.Inject

class TranslationManagementActivity : BaseActivity() {
    companion object {
        fun newStartIntent(context: Context) = Intent(context, TranslationManagementActivity::class.java)
    }

    @Inject
    lateinit var translationManager: TranslationManager

    @Inject
    lateinit var translationPresenter: TranslationPresenter

    private lateinit var loadingSpinner: ProgressBar
    private lateinit var translationListView: TranslationListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_translation_management)
        loadingSpinner = findViewById(R.id.loading_spinner)

        translationListView = findViewById(R.id.translation_list)
        translationListView.setPresenter(translationPresenter)
    }

    override fun onStart() {
        super.onStart()

        translationPresenter.attachView(translationListView)

        launch(Dispatchers.Main) {
            receiveChannels.add(translationManager.observeTranslationsLoadingState()
                    .onEach { downloading ->
                        if (downloading) {
                            loadingSpinner.visibility = View.VISIBLE
                            translationListView.visibility = View.GONE
                        } else {
                            loadingSpinner.fadeOut()
                            translationListView.fadeIn()
                        }
                    })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(translationManager.observeTranslationSelection()
                    .onEach { finish() })
        }
    }

    override fun onStop() {
        translationPresenter.detachView()

        super.onStop()
    }
}
