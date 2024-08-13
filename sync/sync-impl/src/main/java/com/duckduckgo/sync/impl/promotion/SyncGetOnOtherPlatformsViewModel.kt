/*
 * Copyright (c) 2024 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.sync.impl.promotion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.sync.impl.R
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
@ContributesViewModel(ActivityScope::class)
class SyncGetOnOtherPlatformsViewModel @Inject constructor(
    private val context: Context,
) : ViewModel() {

    private val command = Channel<Command>(1, DROP_OLDEST)
    fun commands(): Flow<Command> = command.receiveAsFlow()

    fun onUserClickedGetDesktopApp() {
        val link = DESKTOP_APP_LINK.withAttribution()
        val title = context.getString(R.string.sync_get_apps_on_other_platform_desktop_share_sheet_title)
        val message = context.getString(R.string.sync_get_apps_on_other_platform_desktop_share_sheet_message, link)
        viewModelScope.launch {
            command.send(Command.ShowShareSheet(buildIntent(title = title, message = message)))
        }
    }

    fun onUserClickedGetiOSApp() {
        val link = IOS_APP_LINK.withAttribution()
        val title = context.getString(R.string.sync_get_apps_on_other_platform_ios_share_sheet_title)
        val message = context.getString(R.string.sync_get_apps_on_other_platform_ios_share_sheet_message, link)
        viewModelScope.launch {
            command.send(Command.ShowShareSheet(buildIntent(title = title, message = message)))
        }
    }

    fun onUserClickedGetAndroidApp() {
        val link = ANDROID_APP_LINK.withAttribution()
        val title = context.getString(R.string.sync_get_apps_on_other_platform_android_share_sheet_title)
        val message = context.getString(R.string.sync_get_apps_on_other_platform_android_share_sheet_message, link)
        viewModelScope.launch {
            command.send(Command.ShowShareSheet(buildIntent(title = title, message = message)))
        }
    }

    private fun buildIntent(title: String, message: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_TITLE, title)
        }
    }

    private fun String.withAttribution(): String {
        return "$this?$ATTRIBUTION"
    }

    companion object {
        private const val ATTRIBUTION = "origin=funnel_browser_android_sync"
        const val DESKTOP_APP_LINK = "https://duckduckgo.com/browser"
        const val IOS_APP_LINK = "https://duckduckgo.com/ios"
        const val ANDROID_APP_LINK = "https://duckduckgo.com/android"
    }

    sealed interface Command {
        data class ShowShareSheet(val intent: Intent) : Command
    }
}
