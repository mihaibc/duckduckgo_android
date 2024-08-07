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

package com.duckduckgo.app.browser.viewstate

data class OmnibarViewState(
    val omnibarText: String = "",
    val isEditing: Boolean = false,
    val shouldMoveCaretToEnd: Boolean = false,
    val forceExpand: Boolean = true,
    val showSearchIcon: Boolean = false,
    val showClearButton: Boolean = false,
    val showVoiceSearch: Boolean = false,
    val showTabsButton: Boolean = true,
    val fireButton: HighlightableButton = HighlightableButton.Visible(),
    val showMenuButton: HighlightableButton = HighlightableButton.Visible(),
)
