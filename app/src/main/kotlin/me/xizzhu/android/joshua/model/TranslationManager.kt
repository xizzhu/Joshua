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

import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

data class TranslationInfo(val shortName: String, val name: String, val language: String, val size: Long)

@Singleton
class TranslationManager @Inject constructor(
        private val backendService: BackendService, private val localStorage: LocalStorage) {
    fun hasTranslationsInstalled(): Single<Boolean> =
            localStorage.localMetadata().load(LocalMetadata.KEY_LAST_TRANSLATION)
                    .toSingle("").map { it.isNotEmpty() }

    fun loadTranslations(forceRefresh: Boolean): Single<List<TranslationInfo>> {
        return if (forceRefresh) {
            loadTranslationsFromBackend()
        } else {
            loadTranslationsFromLocal().switchIfEmpty(loadTranslationsFromBackend())
        }
    }

    private fun loadTranslationsFromBackend(): Single<List<TranslationInfo>> =
            backendService.translationService().fetchTranslationList().map {
                val translations = ArrayList<TranslationInfo>(it.translations.size)
                for (t in it.translations) {
                    translations.add(TranslationInfo(t.shortName, t.name, t.language, t.size))
                }
                translations as List<TranslationInfo>
            }.doOnSuccess {
                val localTranslations = ArrayList<LocalTranslationInfo>(it.size)
                for (t in it) {
                    localTranslations.add(LocalTranslationInfo(t.shortName, t.name, t.language, t.size))
                }
                localStorage.localTranslationInfoDao().save(localTranslations)
            }

    private fun loadTranslationsFromLocal(): Maybe<List<TranslationInfo>> =
            localStorage.localTranslationInfoDao().load().map {
                val translations = ArrayList<TranslationInfo>(it.size)
                for (t in it) {
                    translations.add(TranslationInfo(t.shortName, t.name, t.language, t.size))
                }
                translations as List<TranslationInfo>
            }
}
