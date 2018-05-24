package com.github.genaku.compactcalendarview

import com.github.genaku.compactcalendarview.domain.Event
import java.util.*

internal class Events(val timeInMillis: Long, val events: ArrayList<Event>) {

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (other !is Events) return false

        if (timeInMillis != other.timeInMillis) return false

        return events == other.events
    }

    override fun hashCode(): Int {
        var result = events.hashCode()
        result = 31 * result + (timeInMillis xor timeInMillis.ushr(32)).toInt()
        return result
    }

    override fun toString(): String = "Events{events=$events, timeInMillis=$timeInMillis}"

}
