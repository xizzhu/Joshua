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
import android.view.View
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.databinding.ActivityStrongNumberBinding
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.ui.fadeIn
import javax.inject.Inject

@AndroidEntryPoint
class StrongNumberActivity : BaseActivity<ActivityStrongNumberBinding>() {
    companion object {
        private const val KEY_STRONG_NUMBER = "me.xizzhu.android.joshua.KEY_STRONG_NUMBER"

        fun bundle(strongNumber: String): Bundle = Bundle().apply { putString(KEY_STRONG_NUMBER, strongNumber) }
    }

    @Inject
    lateinit var strongNumberViewModel: StrongNumberViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSettings()
        observeStrongNumber()
        strongNumberViewModel.loadStrongNumber(intent.getStringExtra(KEY_STRONG_NUMBER) ?: "")
    }

    private fun observeSettings() {
        strongNumberViewModel.settings()
                .onEach { viewBinding.strongNumberList.setSettings(it) }
                .launchIn(lifecycleScope)
    }

    private fun observeStrongNumber() {
        strongNumberViewModel.strongNumber()
                .onEach(
                        onLoading = {
                            with(viewBinding) {
                                loadingSpinner.fadeIn()
                                strongNumberList.visibility = View.GONE
                            }
                        },
                        onSuccess = {
                            println("--> ${it.items}")
                            with(viewBinding) {
                                strongNumberList.setItems(it.items)
                                strongNumberList.fadeIn()
                                loadingSpinner.visibility = View.GONE
                            }
                        },
                        onFailure = {
                            // TODO
                            it.printStackTrace()
                        }
                )
                .launchIn(lifecycleScope)
    }

    override fun inflateViewBinding(): ActivityStrongNumberBinding = ActivityStrongNumberBinding.inflate(layoutInflater)
}
