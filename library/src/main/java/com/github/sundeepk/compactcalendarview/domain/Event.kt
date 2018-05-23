package com.github.sundeepk.compactcalendarview.domain

class Event {

    var color: Int = 0
        private set
    var timeInMillis: Long = 0
        private set
    var data: Any? = null

    constructor(color: Int, timeInMillis: Long) {
        this.color = color
        this.timeInMillis = timeInMillis
    }

    constructor(color: Int, timeInMillis: Long, data: Any) {
        this.color = color
        this.timeInMillis = timeInMillis
        this.data = data
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (other !is Event) return false

        if (color != other.color) return false

        if (timeInMillis != other.timeInMillis) return false

        return !if (data != null) data != other.data else other.data != null
    }

    override fun hashCode(): Int {
        var result = color
        result = 31 * result + (timeInMillis xor timeInMillis.ushr(32)).toInt()
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Event{color=$color, timeInMillis=$timeInMillis, data=$data}"
}
