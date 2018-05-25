package com.github.genaku.compactcalendarview.comparators

import com.github.genaku.compactcalendarview.domain.Event
import java.util.*

class EventComparator : Comparator<Event> {

    override fun compare(lhs: Event, rhs: Event): Int {
        return when {
            lhs.timeInMillis < rhs.timeInMillis -> -1
            lhs.timeInMillis > rhs.timeInMillis -> 1
            else -> 0
        }
    }
}
