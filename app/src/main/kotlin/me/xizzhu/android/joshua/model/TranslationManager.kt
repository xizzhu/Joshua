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
import com.squareup.moshi.Json
import io.reactivex.Observable
import io.reactivex.Single
import me.xizzhu.android.joshua.SHARED_PREFERENCES_KEY_LAST_TRANSLATION
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

data class TranslationInfo(val shortName: String, val name: String, val language: String, val size: Long)

@Singleton
class TranslationManager @Inject constructor(private val sharedPreferences: SharedPreferences, retrofit: Retrofit,
                                             private val localStorage: LocalStorage) {
    private val translationService = retrofit.create(TranslationService::class.java)

    fun hasTranslationsInstalled() = !TextUtils.isEmpty(sharedPreferences.getString(SHARED_PREFERENCES_KEY_LAST_TRANSLATION, null))

    fun loadTranslations(): Observable<List<TranslationInfo>> {
        return fetchTranslations().toObservable()
    }

    private fun fetchTranslations(): Single<List<TranslationInfo>> =
            translationService.fetchTranslationList().map {
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

private data class BackendTranslationInfo(@Json(name = "shortName") val shortName: String,
                                          @Json(name = "name") val name: String,
                                          @Json(name = "language") val language: String,
                                          @Json(name = "size") val size: Long) {
    fun toTranslationInfo() = TranslationInfo(shortName, name, language, size)
}

private data class BackendTranslationList(@Json(name = "translations") val translations: List<BackendTranslationInfo>)

private interface TranslationService {
    @GET("list.json")
    fun fetchTranslationList(): Single<BackendTranslationList>
}
