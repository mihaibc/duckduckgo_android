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

package com.duckduckgo.sync.api.promotion

/**
 * Used for determining if a sync promotion should be shown to the user and for recording when a promotion has been dismissed.
 */
interface SyncPromotions {

    /**
     * Returns true if the bookmarks promotion should be shown to the user.
     * @param savedBookmarks The number of bookmarks saved by the user.
     */
    suspend fun canShowBookmarksPromotion(savedBookmarks: Int): Boolean

    /**
     * Records that the bookmarks promotion has been dismissed.
     */
    suspend fun recordBookmarksPromotionDismissed()

    /**
     * Returns true if the passwords promotion should be shown to the user.
     */
    suspend fun canShowPasswordsPromotion(savedPasswords: Int): Boolean

    /**
     * Records that the passwords promotion has been dismissed.
     */
    suspend fun recordPasswordsPromotionDismissed()
}
