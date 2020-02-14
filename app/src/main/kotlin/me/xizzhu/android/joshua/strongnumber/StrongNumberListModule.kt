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

package me.xizzhu.android.joshua.strongnumber

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.ActivityScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumberManager

@Module
object StrongNumberListModule {
    @ActivityScope
    @Provides
    fun provideStrongNumberListPresenter(strongNumberListActivity: StrongNumberListActivity,
                                         navigator: Navigator,
                                         strongNumberListViewModel: StrongNumberListViewModel): StrongNumberListPresenter =
            StrongNumberListPresenter(strongNumberListActivity, navigator, strongNumberListViewModel, strongNumberListActivity.lifecycleScope)

    @ActivityScope
    @Provides
    fun provideStrongNumberViewModel(strongNumberListActivity: StrongNumberListActivity,
                                     bibleReadingManager: BibleReadingManager,
                                     strongNumberManager: StrongNumberManager,
                                     settingsManager: SettingsManager): StrongNumberListViewModel {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StrongNumberListViewModel::class.java)) {
                    return StrongNumberListViewModel(bibleReadingManager, strongNumberManager, settingsManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(strongNumberListActivity, factory).get(StrongNumberListViewModel::class.java)
    }
}
