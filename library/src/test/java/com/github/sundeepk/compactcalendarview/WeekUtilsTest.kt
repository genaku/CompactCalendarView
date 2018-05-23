package com.github.sundeepk.compactcalendarview

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.util.*

class WeekUtilsTest {

    @Test
    fun itShouldReturnCorrectWeekDaysWhenSundayIsFirstDay() {
        val expectedWeekDays = arrayOf("S", "M", "T", "W", "T", "F", "S")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.SUNDAY, false).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenMondayIsFirstDay() {
        val expectedWeekDays = arrayOf("M", "T", "W", "T", "F", "S", "S")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.MONDAY, false).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenTuesdayIsFirstDay() {
        val expectedWeekDays = arrayOf("T", "W", "T", "F", "S", "S", "M")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.TUESDAY, false).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenWednesdayIsFirstDay() {
        val expectedWeekDays = arrayOf("W", "T", "F", "S", "S", "M", "T")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.WEDNESDAY, false).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenThursdayIsFirstDay() {
        val expectedWeekDays = arrayOf("T", "F", "S", "S", "M", "T", "W")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.THURSDAY, false).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenFridayIsFirstDay() {
        val expectedWeekDays = arrayOf("F", "S", "S", "M", "T", "W", "T")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.FRIDAY, false).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenSaturdayIsFirstDay() {
        val expectedWeekDays = arrayOf("S", "S", "M", "T", "W", "T", "F")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.SATURDAY, false).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenSundayIsFirstDayWith3Letters() {
        val expectedWeekDays = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.SUNDAY, true).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenMondayIsFirstDayWith3Letters() {
        val expectedWeekDays = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.MONDAY, true).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenTuesdayIsFirstDayWith3Letters() {
        val expectedWeekDays = arrayOf("Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "Mon")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.TUESDAY, true).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenWednesdayIsFirstDayWith3Letters() {
        val expectedWeekDays = arrayOf("Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.WEDNESDAY, true).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenThursdayIsFirstDayWith3Letters() {
        val expectedWeekDays = arrayOf("Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.THURSDAY, true).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenFridayIsFirstDayWith3Letters() {
        val expectedWeekDays = arrayOf("Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.FRIDAY, true).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

    @Test
    fun itShouldReturnCorrectWeekDaysWhenSaturdayIsFirstDayWith3Letters() {
        val expectedWeekDays = arrayOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
        val actualWeekDays = WeekUtils.getWeekdayNames(Locale.ENGLISH, Calendar.SATURDAY, true).toArray()
        assertArrayEquals(expectedWeekDays, actualWeekDays)
    }

}