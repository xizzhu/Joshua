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

package me.xizzhu.android.joshua.translations

import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwareActivity
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwareViewModel
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import javax.inject.Inject

class TranslationManagementActivity : BaseSettingsAwareActivity() {
    @Inject
    lateinit var translationViewModel: TranslationsViewModel

    @Inject
    lateinit var translationListPresenter: TranslationListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_translation_management)
        translationListPresenter.create(TranslationListViewHolder(
                findViewById(R.id.swipe_refresher), findViewById(R.id.translation_list)))
    }

    override fun getViewPresenters(): List<ViewPresenter<out ViewHolder, out Interactor>> = listOf(translationListPresenter)

    override fun getBaseSettingsAwareViewModel(): BaseSettingsAwareViewModel = translationViewModel
}
