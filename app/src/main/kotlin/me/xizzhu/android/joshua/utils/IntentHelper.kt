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

package me.xizzhu.android.joshua.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.os.Parcelable
import androidx.annotation.VisibleForTesting

// Facebook doesn't want us to pre-fill the message, but still captures ACTION_SEND. Therefore,
// I have to exclude their package from being shown.
// Rants: it's a horrible way to force developers to use their SDK.
// ref. https://developers.facebook.com/bugs/332619626816423
fun createChooserForSharing(context: Context, title: String, text: String): Intent? =
        createChooserForSharing(context.packageManager, "com.facebook.katana", title, text)

@VisibleForTesting
fun createChooserForSharing(packageManager: PackageManager, packageToExclude: String,
                            title: String, text: String): Intent? {
    val sendIntent = Intent(Intent.ACTION_SEND).setType("text/plain")
    val resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0)
    if (resolveInfoList.isEmpty()) return null

    val filteredIntents = ArrayList<Intent>(resolveInfoList.size)
    for (resolveInfo in resolveInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        if (packageToExclude != packageName) {
            val labeledIntent = LabeledIntent(packageName, resolveInfo.loadLabel(packageManager), resolveInfo.iconResource)
            labeledIntent.setAction(Intent.ACTION_SEND).setPackage(packageName)
                    .setComponent(ComponentName(packageName, resolveInfo.activityInfo.name))
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, text)
            filteredIntents.add(labeledIntent)
        }
    }
    if (filteredIntents.isEmpty()) return null

    return Intent.createChooser(filteredIntents.removeAt(0), title).apply {
        val array = arrayOfNulls<Parcelable>(filteredIntents.size)
        putExtra(Intent.EXTRA_INITIAL_INTENTS, filteredIntents.toArray(array))
    }
}
