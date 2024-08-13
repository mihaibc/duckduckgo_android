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
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.ContributeToActivityStarter
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.navigation.api.GlobalActivityStarter
import com.duckduckgo.sync.impl.databinding.ActivitySyncGetOnOtherDevicesBinding
import com.duckduckgo.sync.impl.promotion.SyncGetOnOtherPlatformsViewModel.Command
import com.duckduckgo.sync.impl.promotion.SyncGetOnOtherPlatformsViewModel.Command.ShowShareSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@InjectWith(ActivityScope::class, delayGeneration = true)
@ContributeToActivityStarter(SyncActivityGetOnOtherPlatforms::class)
class SyncGetOnOtherPlatformsActivity : DuckDuckGoActivity() {
    private val binding: ActivitySyncGetOnOtherDevicesBinding by viewBinding()
    private val viewModel: SyncGetOnOtherPlatformsViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar.toolbar)
        configureUiEventHandlers()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.commands()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { processCommand(it) }
            .launchIn(lifecycleScope)
    }

    private fun processCommand(it: Command) {
        Timber.w("Processing command: $it")
        when (it) {
            is ShowShareSheet -> launchSharePageChooser(it.intent)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun launchSharePageChooser(intent: Intent) {
        try {
            startActivity(Intent.createChooser(intent, null))
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "Activity not found")
        }
    }

    private fun configureUiEventHandlers() = with(binding) {
        getDesktopApp.setOnClickListener { viewModel.onUserClickedGetDesktopApp() }
        getIosApp.setOnClickListener { viewModel.onUserClickedGetiOSApp() }
        getAndroidApp.setOnClickListener { viewModel.onUserClickedGetAndroidApp() }
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, SyncGetOnOtherPlatformsActivity::class.java)
        }
    }
}

data object SyncActivityGetOnOtherPlatforms : GlobalActivityStarter.ActivityParams {
    private fun readResolve(): Any = SyncActivityGetOnOtherPlatforms
}
