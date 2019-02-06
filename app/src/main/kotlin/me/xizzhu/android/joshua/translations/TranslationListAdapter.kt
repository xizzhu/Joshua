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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.model.TranslationInfo
import java.util.ArrayList

private class TranslationInfoViewHolder(private val presenter: TranslationManagementPresenter, inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_translation, parent, false)), View.OnClickListener {
    private val textView = itemView as TextView
    private var translationInfo: TranslationInfo? = null

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(translationInfo: TranslationInfo, currentTranslation: Boolean) {
        this.translationInfo = translationInfo

        textView.text = translationInfo.name
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                if (currentTranslation) R.drawable.ic_check else 0, 0)
    }

    override fun onClick(v: View) {
        if (translationInfo != null && !translationInfo!!.downloaded) {
            presenter.downloadTranslation(translationInfo!!)
        }
    }
}

class TranslationListAdapter(context: Context, private val presenter: TranslationManagementPresenter) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    private val translations = ArrayList<TranslationInfo>()
    private var currentTranslation: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            TranslationInfoViewHolder(presenter, inflater, parent)

    override fun getItemCount(): Int = translations.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val translation = translations[position]
        (holder as TranslationInfoViewHolder).bind(translation, translation.shortName == currentTranslation)
    }

    fun setTranslations(translations: List<TranslationInfo>, currentTranslation: String) {
        this.translations.clear()
        this.translations.addAll(translations)
        this.currentTranslation = currentTranslation
        notifyDataSetChanged()
    }
}
