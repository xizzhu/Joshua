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

package me.xizzhu.android.joshua.translations

import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import me.xizzhu.android.joshua.model.TranslationInfo
import me.xizzhu.android.joshua.model.TranslationManager
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.applySchedulers

class TranslationManagementPresenter(private val translationManager: TranslationManager) : MVPPresenter<TranslationManagementView>() {
    private var loadTranslationsDisposable: Disposable? = null

    override fun onViewDropped() {
        disposeLoadTranslations()

        super.onViewDropped()
    }

    private fun disposeLoadTranslations() {
        loadTranslationsDisposable?.dispose()
        loadTranslationsDisposable = null
    }

    fun loadTranslations(forceRefresh: Boolean) {
        disposeLoadTranslations()
        loadTranslationsDisposable = translationManager.loadTranslations(forceRefresh).applySchedulers()
                .subscribeWith(object : DisposableSingleObserver<List<TranslationInfo>>() {
                    override fun onSuccess(translations: List<TranslationInfo>) {
                        loadTranslationsDisposable = null
                        view?.onTranslationsLoaded(translations)
                    }

                    override fun onError(e: Throwable) {
                        loadTranslationsDisposable = null
                        view?.onTranslationsLoadFailed()
                    }
                })
    }
}
