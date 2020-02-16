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

package me.xizzhu.android.joshua.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager

@Module
object ReadingProgressModule {
    @ActivityScope
    @Provides
    fun provideReadingProgressPresenter(readingProgressActivity: ReadingProgressActivity, navigator: Navigator,
                                        readingProgressViewModel: ReadingProgressViewModel): ReadingProgressPresenter =
            ReadingProgressPresenter(readingProgressActivity, navigator, readingProgressViewModel, readingProgressActivity.lifecycle)

    @ActivityScope
    @Provides
    fun provideReadingProgressViewModel(readingProgressActivity: ReadingProgressActivity,
                                        bibleReadingManager: BibleReadingManager,
                                        readingProgressManager: ReadingProgressManager,
                                        settingsManager: SettingsManager): ReadingProgressViewModel {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReadingProgressViewModel::class.java)) {
                    return ReadingProgressViewModel(bibleReadingManager, readingProgressManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(readingProgressActivity, factory).get(ReadingProgressViewModel::class.java)
    }
}
