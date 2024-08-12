package com.duckduckgo.app.statistics.pixels

import android.content.Context
import com.duckduckgo.app.statistics.pixels.FeatureRetentionSegmentsPixelSender.Companion.PIXEL_VALUE_APP_USE
import com.duckduckgo.app.statistics.pixels.FeatureRetentionSegmentsPixelSender.Companion.RETENTION_SEGMENTS_PREF_FILE
import com.duckduckgo.app.statistics.pixels.Pixel.StatisticsPixelName
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class FeatureRetentionSegmentsPixelSenderTest {
    private lateinit var testee: FeatureRetentionSegmentsPixelSender

    private val preferences = InMemorySharedPreferences()

    private val mockContext = mock<Context>()
    private val mockPixel = mock<Pixel>()
    private val mockStore = mock<StatisticsDataStore>()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Before
    fun before() {
        testee = FeatureRetentionSegmentsPixelSender(
            mockContext,
            mockPixel,
            coroutineTestRule.testScope,
            coroutineTestRule.testDispatcherProvider,
            mockStore,
        )
    }

    // TODO ANA: Tests don't have real names, they are just placeholders. Look at the oldAtb/newAtb values passed to the function

    @Test
    fun whenSendAppUsePixelIsCalledThenPixelIsSent() = runTest {
        whenever(mockContext.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(preferences)

        testee.fireRetentionSegmentsPixel(
            activityType = PIXEL_VALUE_APP_USE,
            oldAtb = "v129-4",
            newAtb = "v129-6",
        )
        verify(mockPixel).fire(
            StatisticsPixelName.RETENTION_SEGMENTS.pixelName,
            mapOf(
                "activity_type" to "app_use",
                "agent" to "ddg_android",
                "new_set_atb" to "v129-6",
            )
        )
    }

    @Test
    fun whenSendAppUsePixelIsCalledThenPixelIsSent2() = runTest {
        whenever(mockContext.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(preferences)

        testee.fireRetentionSegmentsPixel(
            activityType = PIXEL_VALUE_APP_USE,
            oldAtb = "v128-4",
            newAtb = "v129-6",
        )
        verify(mockPixel).fire(
            StatisticsPixelName.RETENTION_SEGMENTS.pixelName,
            mapOf(
                "activity_type" to "app_use",
                "agent" to "ddg_android",
                "new_set_atb" to "v129-6",
                "count_as_wau" to "true",
                "count_as_mau_n" to "ftff",
            )
        )
    }

    @Test
    fun whenSendAppUsePixelIsCalledThenPixelIsSent3() = runTest {
        whenever(mockContext.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(preferences)

        testee.fireRetentionSegmentsPixel(
            activityType = PIXEL_VALUE_APP_USE,
            oldAtb = "v127-4",
            newAtb = "v129-6",
        )
        verify(mockPixel).fire(
            StatisticsPixelName.RETENTION_SEGMENTS.pixelName,
            mapOf(
                "activity_type" to "app_use",
                "agent" to "ddg_android",
                "new_set_atb" to "v129-6",
                "count_as_wau" to "true",
                "count_as_mau_n" to "ttff",
                "segments_today" to "reactivated_wau"
            )
        )
    }

    @Test
    fun whenSendAppUsePixelIsCalledThenPixelIsSent4() = runTest {
        whenever(mockContext.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(preferences)

        testee.fireRetentionSegmentsPixel(
            activityType = PIXEL_VALUE_APP_USE,
            oldAtb = "v126-4",
            newAtb = "v129-6",
        )
        verify(mockPixel).fire(
            StatisticsPixelName.RETENTION_SEGMENTS.pixelName,
            mapOf(
                "activity_type" to "app_use",
                "agent" to "ddg_android",
                "new_set_atb" to "v129-6",
                "count_as_wau" to "true",
                "count_as_mau_n" to "ttft",
                "segments_today" to "reactivated_wau"
            )
        )
    }

    @Test
    fun whenSendAppUsePixelIsCalledThenPixelIsSent5() = runTest {
        whenever(mockContext.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(preferences)

        testee.fireRetentionSegmentsPixel(
            activityType = PIXEL_VALUE_APP_USE,
            oldAtb = "v125-4",
            newAtb = "v129-6",
        )
        verify(mockPixel).fire(
            StatisticsPixelName.RETENTION_SEGMENTS.pixelName,
            mapOf(
                "activity_type" to "app_use",
                "agent" to "ddg_android",
                "new_set_atb" to "v129-6",
                "count_as_wau" to "true",
                "count_as_mau_n" to "tttt",
                "segments_today" to "reactivated_wau"
            )
        )
    }

    @Test
    fun whenSendAppUsePixelIsCalledThenPixelIsSent6() = runTest {
        whenever(mockContext.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(preferences)

        testee.fireRetentionSegmentsPixel(
            activityType = PIXEL_VALUE_APP_USE,
            oldAtb = "v124-4",
            newAtb = "v129-6",
        )
        verify(mockPixel).fire(
            StatisticsPixelName.RETENTION_SEGMENTS.pixelName,
            mapOf(
                "activity_type" to "app_use",
                "agent" to "ddg_android",
                "new_set_atb" to "v129-6",
                "count_as_wau" to "true",
                "count_as_mau_n" to "tttt",
                "segments_today" to "reactivated_wau,reactivated_mau_1"
            )
        )
    }

    @Test
    fun whenSendAppUsePixelIsCalledThenPixelIsSent7() = runTest {
        whenever(mockContext.getSharedPreferences(RETENTION_SEGMENTS_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(preferences)

        testee.fireRetentionSegmentsPixel(
            activityType = PIXEL_VALUE_APP_USE,
            oldAtb = "v123-4",
            newAtb = "v129-6",
        )
        verify(mockPixel).fire(
            StatisticsPixelName.RETENTION_SEGMENTS.pixelName,
            mapOf(
                "activity_type" to "app_use",
                "agent" to "ddg_android",
                "new_set_atb" to "v129-6",
                "count_as_wau" to "true",
                "count_as_mau_n" to "tttt",
                "segments_today" to "reactivated_wau,reactivated_mau_0,reactivated_mau_1"
            )
        )
    }
}