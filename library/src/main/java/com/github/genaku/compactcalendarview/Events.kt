package com.github.genaku.compactcalendarview

import com.github.genaku.compactcalendarview.domain.Event
import java.util.*

data class Events(val timeInMillis: Long, val events: ArrayList<Event>)