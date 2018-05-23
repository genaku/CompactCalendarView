package com.github.sundeepk.compactcalendarview.comparators

import com.github.sundeepk.compactcalendarview.domain.Event
import java.util.*

class EventComparator : Comparator<Event> {
    override fun compare(lhs: Event, rhs: Event): Int = when {
        lhs.timeInMillis < rhs.timeInMillis -> -1
        lhs.timeInMillis == rhs.timeInMillis -> 0
        else -> 1
    }
}
