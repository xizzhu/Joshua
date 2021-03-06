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

package me.xizzhu.android.joshua.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.SettingsManager

@Module
@InstallIn(ActivityComponent::class)
object SettingsModule {
    @Provides
    fun provideSettingsActivity(activity: Activity): SettingsActivity = activity as SettingsActivity

    @ActivityScoped
    @Provides
    fun provideSettingsViewPresenter(settingsViewModel: SettingsViewModel,
                                     settingsActivity: SettingsActivity): SettingsPresenter =
            SettingsPresenter(settingsViewModel, settingsActivity)

    @ActivityScoped
    @Provides
    fun provideSettingsViewModel(settingsActivity: SettingsActivity,
                                 settingsManager: SettingsManager,
                                 backupManager: BackupManager): SettingsViewModel {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    return SettingsViewModel(settingsManager, backupManager) as T
                }

                throw IllegalArgumentException("Unsupported model class - $modelClass")
            }
        }
        return ViewModelProvider(settingsActivity, factory).get(SettingsViewModel::class.java)
    }
}
