package com.github.genaku.compactcalendarview

import java.text.DateFormatSymbols
import java.util.*

class WeekUtils {

    companion object {

        fun getWeekdayNames(locale: Locale, day: Int, useThreeLetterAbbreviation: Boolean): ArrayList<String> {
            val dateFormatSymbols = DateFormatSymbols(locale)
            val dayNames = dateFormatSymbols.shortWeekdays
                    ?: throw IllegalStateException("Unable to determine weekday names from default locale")
            if (dayNames.size != 8) {
                throw IllegalStateException("Expected weekday names from default locale to be of size 7 but: "
                        + Arrays.toString(dayNames) + " with size " + dayNames.size + " was returned.")
            }

            val weekDayNames = ArrayList<String>()
            val weekDaysFromSunday = arrayOf(dayNames[1], dayNames[2], dayNames[3], dayNames[4], dayNames[5], dayNames[6], dayNames[7])
            var currentDay = day - 1
            for (i in 0..6) {
                currentDay = if (currentDay >= 7) 0 else currentDay
                weekDayNames.add(weekDaysFromSunday[currentDay])
                currentDay++
            }

            if (!useThreeLetterAbbreviation) {
                for (idx in weekDayNames.indices) {
                    weekDayNames[idx] = weekDayNames[idx].substring(0, 1)
                }
            }

            return weekDayNames
        }

    }

}
