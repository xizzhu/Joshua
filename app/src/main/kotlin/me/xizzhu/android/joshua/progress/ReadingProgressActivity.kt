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

package me.xizzhu.android.joshua.progress

import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.utils.activities.BaseLoadingSpinnerActivity
import me.xizzhu.android.joshua.utils.activities.BaseSettingsInteractor
import javax.inject.Inject

class ReadingProgressActivity : BaseLoadingSpinnerActivity() {
    @Inject
    lateinit var readingProgressInteractor: ReadingProgressInteractor

    @Inject
    lateinit var readingProgressPresenter: ReadingProgressPresenter

    private val readingProgressListView: ReadingProgressListView by bindView(R.id.reading_progress_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reading_progress)
        readingProgressListView.setPresenter(readingProgressPresenter)
    }

    override fun onStart() {
        super.onStart()

        readingProgressPresenter.attachView(readingProgressListView)
    }

    override fun onStop() {
        readingProgressPresenter.detachView()

        super.onStop()
    }

    override fun getBaseSettingsInteractor(): BaseSettingsInteractor = readingProgressInteractor
}
