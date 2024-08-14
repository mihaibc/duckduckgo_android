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

package com.duckduckgo.duckplayer.impl

import com.duckduckgo.app.statistics.pixels.Pixel

enum class DuckPlayerPixelNames(override val pixelName: String) : Pixel.PixelName {
    DUCK_PLAYER_OVERLAY_YOUTUBE_IMPRESSIONS("m_duck-player_overlay_youtube_impressions"),
    DUCK_PLAYER_VIEW_FROM_YOUTUBE_MAIN_OVERLAY("m_duck-player_view-from_youtube_main-overlay"),
    DUCK_PLAYER_OVERLAY_YOUTUBE_WATCH_HERE("m_duck-player_overlay_youtube_watch_here"),
    DUCK_PLAYER_WATCH_ON_YOUTUBE("m_duck-player_watch_on_youtube"),
    DUCK_PLAYER_DAILY_UNIQUE_VIEW("m_duck-player_daily-unique-view"),
    DUCK_PLAYER_VIEW_FROM_YOUTUBE_AUTOMATIC("m_duck-player_view-from_youtube_automatic"),
    DUCK_PLAYER_VIEW_FROM_OTHER("m_duck-player_view-from_other"),
    DUCK_PLAYER_SETTINGS_ALWAYS_SETTINGS("m_duck-player_setting_always_settings"),
    DUCK_PLAYER_SETTINGS_BACK_TO_DEAULT("m_duck-player_setting_back-to-default"),
    DUCK_PLAYER_SETTINGS_NEVER_SETTINGS("m_duck-player_setting_never_settings"),
}
