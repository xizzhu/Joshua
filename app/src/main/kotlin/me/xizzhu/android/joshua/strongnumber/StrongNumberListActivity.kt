/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.strongnumber

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.activity.BaseSettingsActivity
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class StrongNumberListActivity : BaseSettingsActivity() {
    companion object {
        private const val KEY_STRONG_NUMBER = "me.xizzhu.android.joshua.KEY_STRONG_NUMBER"

        fun bundle(strongNumber: String): Bundle = Bundle().apply { putString(KEY_STRONG_NUMBER, strongNumber) }
    }

    @Inject
    lateinit var strongNumberListViewModel: StrongNumberListViewModel

    @Inject
    lateinit var strongNumberListPresenter: StrongNumberListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_strong_number)
        strongNumberListPresenter.bind(
                StrongNumberListViewHolder(findViewById(R.id.loading_spinner), findViewById(R.id.strong_number_list))
        )
    }

    override fun getBaseSettingsViewModel(): BaseSettingsViewModel = strongNumberListViewModel

    fun strongNumber(): String = intent.getStringExtra(KEY_STRONG_NUMBER) ?: ""
}
