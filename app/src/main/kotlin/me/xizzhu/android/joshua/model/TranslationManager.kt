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

package me.xizzhu.android.joshua.model

import android.content.SharedPreferences
import android.text.TextUtils
import io.reactivex.Observable
import io.reactivex.Single
import me.xizzhu.android.joshua.SHARED_PREFERENCES_KEY_LAST_TRANSLATION
import javax.inject.Inject
import javax.inject.Singleton

data class TranslationInfo(val shortName: String, val name: String, val language: String, val size: Long)

@Singleton
class TranslationManager @Inject constructor(private val sharedPreferences: SharedPreferences,
                                             private val backendService: BackendService,
                                             private val localStorage: LocalStorage) {
    fun hasTranslationsInstalled() = !TextUtils.isEmpty(sharedPreferences.getString(SHARED_PREFERENCES_KEY_LAST_TRANSLATION, null))

    fun loadTranslations(): Observable<List<TranslationInfo>> {
        return fetchTranslations().toObservable()
    }

    private fun fetchTranslations(): Single<List<TranslationInfo>> =
            backendService.translationService().fetchTranslationList().map {
                val translations = ArrayList<TranslationInfo>(it.translations.size)
                for (t in it.translations) {
                    translations.add(t.toTranslationInfo())
                }
                translations as List<TranslationInfo>
            }.doOnSuccess {
                val localTranslations = ArrayList<LocalTranslationInfo>(it.size)
                for (t in it) {
                    localTranslations.add(LocalTranslationInfo.fromTranslationInfo(t))
                }
                localStorage.localTranslationInfoDao().save(localTranslations)
            }
}
