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

package me.xizzhu.android.joshua.utils.activities

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.ui.LoadingSpinner
import me.xizzhu.android.joshua.ui.LoadingAwarePresenter
import javax.inject.Inject

abstract class BaseLoadingSpinnerActivity : BaseSettingsActivity() {
    @Inject
    lateinit var loadingAwarePresenter: LoadingAwarePresenter

    private val loadingSpinner: LoadingSpinner by bindView(R.id.loading_spinner)

    override fun onStart() {
        super.onStart()

        loadingAwarePresenter.attachView(loadingSpinner)
    }

    override fun onStop() {
        loadingAwarePresenter.detachView()

        super.onStop()
    }
}
