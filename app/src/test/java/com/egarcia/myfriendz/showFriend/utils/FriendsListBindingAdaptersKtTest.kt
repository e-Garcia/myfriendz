package com.egarcia.myfriendz.showFriend.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.egarcia.myfriendz.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DetermineStatusBackgroundTest {

    companion object {
        private const val RED_COLOR = 0xFFFF0000.toInt()
        private const val ORANGE_COLOR = 0xFFFFA500.toInt()
        private const val YELLOW_COLOR = 0xFFFFFF00.toInt()
        private const val GREEN_COLOR = 0xFF00FF00.toInt()
    }

    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `determineStatusBackground returns red for last contacted more than 6 months ago`() {
        // Given
        val sixMonthsAgo = LocalDate.now().minusMonths(6)
        val moreThanSixMonthsAgo = sixMonthsAgo.minusDays(1)
        every { ContextCompat.getColor(any(), R.color.red) } returns RED_COLOR

        // When
        val result = determineStatusBackground(mockContext, moreThanSixMonthsAgo)

        // Then
        assertEquals(RED_COLOR, result)
    }

    @Test
    fun `determineStatusBackground returns orange for last contacted between 3 and 6 months ago`() {
        // Given
        val sixMonthsAgo = LocalDate.now().minusMonths(6)
        val threeMonthsAgo = LocalDate.now().minusMonths(3)
        val almostSixMonthsAgo = sixMonthsAgo.plusDays(1)
        val pastThreeMonthsAgo = threeMonthsAgo.minusDays(1)
        every { ContextCompat.getColor(any(), R.color.orange) } returns ORANGE_COLOR

        // When
        val result = determineStatusBackground(mockContext, almostSixMonthsAgo)
        val result2 = determineStatusBackground(mockContext, pastThreeMonthsAgo)

        // Then
        assertEquals(ORANGE_COLOR, result)
        assertEquals(ORANGE_COLOR, result2)
    }

    @Test
    fun `determineStatusBackground returns yellow for last contacted between 1 and 3 months ago`() {
        // Given
        val threeMonthsAgo = LocalDate.now().minusMonths(3)
        val betweenOneAndThreeMonthsAgo = threeMonthsAgo.plusDays(1)
        every { ContextCompat.getColor(any(), R.color.yellow) } returns YELLOW_COLOR

        // When
        val result = determineStatusBackground(mockContext, betweenOneAndThreeMonthsAgo)

        // Then
        assertEquals(YELLOW_COLOR, result)
    }

    @Test
    fun `determineStatusBackground returns green for last contacted less than 1 month ago`() {
        // Given
        val oneMonthAgo = LocalDate.now().minusMonths(1)
        val lessThanOneMonthAgo = oneMonthAgo.plusDays(1)
        every { ContextCompat.getColor(any(), R.color.green) } returns GREEN_COLOR

        // When
        val result = determineStatusBackground(mockContext, lessThanOneMonthAgo)

        // Then
        assertEquals(GREEN_COLOR, result)
    }

    @Test
    fun `determineStatusBackground returns green for last contacted today`() {
        // Given
        val today = LocalDate.now()
        every { ContextCompat.getColor(any(), R.color.green) } returns GREEN_COLOR

        // When
        val result = determineStatusBackground(mockContext, today)

        // Then
        assertEquals(GREEN_COLOR, result)
    }
}