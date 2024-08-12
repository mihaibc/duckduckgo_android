/*
 * Copyright (c) 2023 DuckDuckGo
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

package com.duckduckgo.app.statistics.pixels

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.statistics.api.AtbLifecyclePlugin
import com.duckduckgo.app.statistics.pixels.Pixel.StatisticsPixelName
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

@ContributesMultibinding(AppScope::class)
class FeatureRetentionSegmentsPixelSender @Inject constructor(
    private val context: Context,
    private val pixel: Pixel,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val store: StatisticsDataStore,
) : AtbLifecyclePlugin {

    private val preferences: SharedPreferences by lazy { context.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE) }

    override fun onSearchRetentionAtbRefreshed() {
        coroutineScope.launch(dispatcherProvider.io()) {
            store.searchRetentionAtb?.let { searchRetentionAtb ->
                val currentValue = getSearchDates()
                val newValue = currentValue.toMutableSet()
                newValue.add(searchRetentionAtb)
                setSearchDates(newValue)
            }
        }
    }

    override fun onAppRetentionAtbRefreshed() {
        coroutineScope.launch(dispatcherProvider.io()) {
            store.appRetentionAtb?.let { appRetentionAtb ->
                val currentValue = getAppUseDates()
                val newValue = currentValue.toMutableSet()
                newValue.add(appRetentionAtb)
                setAppUseDates(newValue)
            }
        }
    }

    fun fireRetentionSegmentsPixel(
        activityType: String,
        oldAtb: String,
        newAtb: String,
    ) {
        val parameters = mutableMapOf<String, String>()
        parameters[PIXEL_PARAM_ACTIVITY_TYPE] = activityType
        parameters[PIXEL_PARAM_AGENT] = PIXEL_VALUE_AGENT_ANDROID
        parameters[PIXEL_PARAM_NEW_SET_ATB] = newAtb

        val countAsWau = getCountAsWau(oldAtb, newAtb)
        if (countAsWau) {
            parameters[PIXEL_PARAM_COUNT_AS_WAU] = countAsWau.toString()
        }

        val countAsMau = getCountAsMau(oldAtb, newAtb)
        if (countAsMau.isNotEmpty() && countAsMau != "ffff") {
            parameters[PIXEL_PARAM_COUNT_AS_MAU_N] = countAsMau
        }

        val segmentsToday = getSegmentsToday(activityType, oldAtb, newAtb)
        if (segmentsToday.isNotEmpty()) {
            parameters[PIXEL_PARAM_SEGMENTS_TODAY] = segmentsToday
        }

        val segmentsPrevWeek = getSegmentsPrevWeek(activityType, oldAtb, newAtb)
        if (segmentsPrevWeek.isNotEmpty()) {
            parameters[PIXEL_PARAM_SEGMENTS_PREV_WEEK] = segmentsPrevWeek
        }

        for (n in 0..3) {
            val segmentsPrevMonthN = getSegmentsPrevMonth(n)
            if (segmentsPrevMonthN.isNotEmpty()) {
                parameters["$PIXEL_PARAM_SEGMENTS_PREV_MONTH_N$n"] = segmentsPrevMonthN
            }
        }

        pixel.fire(StatisticsPixelName.RETENTION_SEGMENTS.pixelName, parameters)
    }

    private fun getAppUseDates(): Set<String> {
        return preferences.getStringSet(KEY_APP_USE_DATES, emptySet()) ?: emptySet()
    }

    private fun setAppUseDates(value: Set<String>) {
        preferences.edit { putStringSet(KEY_APP_USE_DATES, value) }
    }

    private fun getSearchDates(): Set<String> {
        return preferences.getStringSet(KEY_SEARCH_DATES, emptySet()) ?: emptySet()
    }

    private fun setSearchDates(value: Set<String>) {
        preferences.edit { putStringSet(KEY_SEARCH_DATES, value) }
    }

    private fun getCountAsWau(oldAtb: String, newAtb: String): Boolean {
        // atb_week(old_set_atb) < atb_week(new_set_atb)
        val oldAtbWeek = atbWeek(oldAtb)
        val newAtbWeek = atbWeek(newAtb)
        return (oldAtbWeek != null && newAtbWeek != null && oldAtbWeek < newAtbWeek)
    }

    private fun getCountAsMau(oldAtb: String, newAtb: String): String {
        // (atb_week(new_set_atb) - n) // 4 > (atb_week(old_set_atb) - n // 4)
        // e.g. fttt
        val oldAtbWeek = atbWeek(oldAtb)
        val newAtbWeek = atbWeek(newAtb)
        if (oldAtbWeek == null || newAtbWeek == null) {
            return ""
        }
        val result = StringBuffer()
        for (n in 0..3) {
            if ((newAtbWeek - n) / 4 > (oldAtbWeek - n) / 4) {
                result.append("t")
            } else {
                result.append("f")
            }
        }
        return result.toString()
    }

    private fun getSegmentsToday(activityType: String, oldAtb: String, newAtb: String): String {
        val result = mutableListOf<String>()
        if (isFirstWeek(newAtb)) {
            result.add(PIXEL_VALUE_SEGMENT_FIRST_WEEK)
        }
        if (isSecondWeek(newAtb)) {
            result.add(PIXEL_VALUE_SEGMENT_SECOND_WEEK)
        }
        if (isFirstMonth(newAtb)) {
            result.add(PIXEL_VALUE_SEGMENT_FIRST_MONTH)
        }
        if (isReinstaller()) {
            result.add(PIXEL_VALUE_SEGMENT_REINSTALLER)
        }
        if (isReactivatedWau(oldAtb, newAtb)) {
            result.add(PIXEL_VALUE_SEGMENT_REACTIVATED_WAU)
        }
        for (n in 0..3) {
            if (isReactivatedMauN(n, oldAtb, newAtb)) {
                result.add("$PIXEL_VALUE_SEGMENT_REACTIVATED_MAU_N$n")
            }
        }
        if (isRegularUser(activityType)) {
            result.add(PIXEL_VALUE_SEGMENT_REGULAR_USER)
        }
        if (isIntermittent(activityType)) {
            result.add(PIXEL_VALUE_SEGMENT_INTERMITTENT)
        }
        return result.joinToString(",")
    }

    private fun isFirstWeek(newAtb: String): Boolean {
        // today's ATB week = install day's ATB week
        val todayAtb = atbWeek(newAtb)
        val installAtb = atbWeek(store.atb?.version ?: "")
        return todayAtb != null && installAtb != null && todayAtb == installAtb
    }

    private fun isSecondWeek(newAtb: String): Boolean {
        // today's ATB week - 1 = install day's ATB week
        val todayAtb = atbWeek(newAtb)
        val installAtb = atbWeek(store.atb?.version ?: "")
        return todayAtb != null && installAtb != null && todayAtb - 1 == installAtb
    }

    private fun isFirstMonth(newAtb: String): Boolean {
        // today's ATB week < install day's ATB week + 4
        val todayAtb = atbWeek(newAtb)
        val installAtb = atbWeek(store.atb?.version ?: "")
        return todayAtb != null && installAtb != null && todayAtb == installAtb + 4
    }

    private fun isReinstaller(): Boolean {
        // The user's ATB cohort currently ends in ru
        val installAtb = store.atb?.version ?: ""
        return installAtb.endsWith("ru")
    }

    private fun isReactivatedWau(oldAtb: String, newAtb: String): Boolean {
        // (last_active_atb_week < current_atb_week - 1) AND NOT first_week
        // This is the user's first activity in at least 2 ATB weeks, and this is not first_week, and count_as_wau is true
        if (isFirstWeek(newAtb)) {
            return false
        }
        if (!getCountAsWau(oldAtb, newAtb)) {
            return false
        }
        val currentAtbWeek = atbWeek(newAtb)
        val lastActiveAtbWeek = atbWeek(oldAtb)
        return currentAtbWeek != null && lastActiveAtbWeek != null && lastActiveAtbWeek < currentAtbWeek - 1
    }

    private fun isReactivatedMauN(n: Int, oldAtb: String, newAtb: String): Boolean {
        // (last_active_atb_week - n) / 4 < (current_atb_week - n) / 4 - 1
        val currentAtbWeek = atbWeek(newAtb)
        val lastActiveAtbWeek = atbWeek(oldAtb)
        if (currentAtbWeek == null || lastActiveAtbWeek == null) {
            return false
        }
        return (lastActiveAtbWeek - n) / 4 < (currentAtbWeek - n) / 4 - 1
    }

    private fun isRegularUser(activityType: String): Boolean {
        // From the history of set_atb for the last 28 days before today, was the user active on >=14 dates?
        val result = if (activityType == PIXEL_VALUE_APP_USE) {
            getAppUseDates()
        } else {
            getSearchDates()
        }
        return result.size >= 14
    }

    private fun isIntermittent(activityType: String): Boolean {
        // From the history of set_atb for the last 28 days before today, was the user active in each of the 4 weeks, but on <14 dates?
        val result = if (activityType == PIXEL_VALUE_APP_USE) {
            getAppUseDates()
        } else {
            getSearchDates()
        }
        if (result.size >= 14) {
            return false
        }
        val atbWeeks = result.mapNotNull { atbWeek(it) }.toSet()
        return atbWeeks.size == 4
    }

    private fun getSegmentsPrevWeek(activityType: String, oldAtb: String, newAtb: String): String {
        val result = mutableListOf<String>()
        if (!getCountAsWau(oldAtb, newAtb)) {
            return ""
        }
        val allAtbs = if (activityType == PIXEL_VALUE_APP_USE) {
            getAppUseDates()
        } else {
            getSearchDates()
        }
        val atbWeeks = allAtbs.mapNotNull { atbWeek(it) }.toSet()
        val lastAtbWeek = atbWeek(newAtb)?.let { it - 1}
        if (lastAtbWeek == null || !atbWeeks.contains(lastAtbWeek)) {
            return ""
        }
        val lastAtb = allAtbs.firstOrNull { atbWeek(it) == lastAtbWeek }
        if (lastAtb == null) {
            return ""
        }
        if (isFirstWeek(lastAtb)) {
            result.add(PIXEL_VALUE_SEGMENT_FIRST_WEEK)
        }
        if (isSecondWeek(lastAtb)) {
            result.add(PIXEL_VALUE_SEGMENT_SECOND_WEEK)
        }
        if (isFirstMonth(lastAtb)) {
            result.add(PIXEL_VALUE_SEGMENT_FIRST_MONTH)
        }
        return result.joinToString(",")
    }

    private fun getSegmentsPrevMonth(n: Int, ): String {
        // TODO ANA: IMPLEMENT THIS
        return ""
    }

    private fun atbWeek(atb: String): Int? {
        val startIndex = atb.indexOf('v') + 1
        val endIndex = atb.indexOf('-', startIndex)
        if (startIndex < endIndex) {
            return atb.substring(startIndex, endIndex).toIntOrNull()
        }
        return null
    }

    companion object {
        const val RETENTION_SEGMENTS_PREF_FILE = "com.duckduckgo.mobile.android.retention.segments.pixels"
        private const val KEY_APP_USE_DATES = "com.duckduckgo.app.statistics.app_use.dates"
        private const val KEY_SEARCH_DATES = "com.duckduckgo.app.statistics.search.dates"

        private const val PIXEL_PARAM_ACTIVITY_TYPE = "activity_type"
        private const val PIXEL_PARAM_AGENT = "agent"
        private const val PIXEL_PARAM_NEW_SET_ATB = "new_set_atb"
        private const val PIXEL_PARAM_COUNT_AS_WAU = "count_as_wau"
        private const val PIXEL_PARAM_COUNT_AS_MAU_N = "count_as_mau_n"
        private const val PIXEL_PARAM_SEGMENTS_TODAY = "segments_today"
        private const val PIXEL_PARAM_SEGMENTS_PREV_WEEK = "segments_prev_week"
        private const val PIXEL_PARAM_SEGMENTS_PREV_MONTH_N = "segments_prev_month_"

        const val PIXEL_VALUE_APP_USE = "app_use"
        const val PIXEL_VALUE_SEARCH = "search"
        private const val PIXEL_VALUE_AGENT_ANDROID = "ddg_android"
        private const val PIXEL_VALUE_SEGMENT_FIRST_WEEK = "first_week"
        private const val PIXEL_VALUE_SEGMENT_SECOND_WEEK = "second_week"
        private const val PIXEL_VALUE_SEGMENT_FIRST_MONTH = "first_month"
        private const val PIXEL_VALUE_SEGMENT_REINSTALLER = "reinstaller"

        // TODO ANA: ARE THESE VALUES CORRECT / EXPECTED?
        private const val PIXEL_VALUE_SEGMENT_REACTIVATED_WAU = "reactivated_wau"
        private const val PIXEL_VALUE_SEGMENT_REACTIVATED_MAU_N = "reactivated_mau_"
        private const val PIXEL_VALUE_SEGMENT_REGULAR_USER = "regular_user"
        private const val PIXEL_VALUE_SEGMENT_INTERMITTENT = "intermittent"
    }
}
