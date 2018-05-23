package com.github.sundeepk.compactcalendarview

import com.github.sundeepk.compactcalendarview.domain.Event

class Events(val timeInMillis: Long, val events: ArrayList<Event>) {

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (other !is Events) return false

        if (timeInMillis != other.timeInMillis) return false

        return !if (events != null) events != other.events else other.events != null
    }

    override fun hashCode(): Int {
        var result = events.hashCode()
        result = 31 * result + (timeInMillis xor timeInMillis.ushr(32)).toInt()
        return result
    }

    override fun toString(): String = "Events{events=$events, timeInMillis=$timeInMillis}"
}
