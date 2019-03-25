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

import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.ui.SwipeRefresher
import me.xizzhu.android.joshua.ui.SwipeRefresherPresenter
import me.xizzhu.android.joshua.utils.BaseSettingsActivity
import javax.inject.Inject

class TranslationManagementActivity : BaseSettingsActivity() {
    @Inject
    lateinit var translationInteractor: TranslationInteractor

    @Inject
    lateinit var swipeRefresherPresenter: SwipeRefresherPresenter

    @Inject
    lateinit var translationPresenter: TranslationPresenter

    private lateinit var swipeRefresher: SwipeRefresher
    private lateinit var translationListView: TranslationListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_translation_management)

        swipeRefresher = findViewById(R.id.swipe_refresher)
        swipeRefresher.setPresenter(swipeRefresherPresenter)

        translationListView = findViewById(R.id.translation_list)
        translationListView.setPresenter(translationPresenter)

        observeSettings(translationInteractor)
    }

    override fun onStart() {
        super.onStart()

        swipeRefresherPresenter.attachView(swipeRefresher)
        translationPresenter.attachView(translationListView)
    }

    override fun onStop() {
        swipeRefresherPresenter.detachView()
        translationPresenter.detachView()

        super.onStop()
    }
}
