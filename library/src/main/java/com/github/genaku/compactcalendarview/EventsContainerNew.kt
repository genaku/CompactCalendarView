package com.github.genaku.compactcalendarview

import android.util.Log
import android.util.SparseArray
import com.github.genaku.compactcalendarview.comparators.EventComparator
import com.github.genaku.compactcalendarview.domain.Event
import java.util.*
import kotlin.collections.ArrayList

class EventsContainerNew(private val eventsCalendar: Calendar) {

    private val mEventsArray = SparseArray<Events>()
    private val mEventsComparator = EventComparator()

    fun addEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        val key = getKey(eventsCalendar)
        val dayEvents = mEventsArray.get(key)
        if (dayEvents != null) {
            dayEvents.events.add(event)
        } else {
            val events = ArrayList<Event>()
            events.add(event)
            mEventsArray.put(key, Events(event.timeInMillis, events))
        }
    }

    fun removeEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        mEventsArray.get(getKey(eventsCalendar))?.events?.remove(event)
    }

    fun removeEvents(events: ArrayList<Event>) = events.forEach {
        removeEvent(it)
    }

    fun removeAllEvents() {
        mEventsArray.clear()
    }

    fun addEvents(events: ArrayList<Event>) = events.forEach {
        addEvent(it)
    }

    fun setEvents(events: ArrayList<Event>) {
        removeAllEvents()
        addEvents(events)
    }

    fun getDateEvents(date: Date): ArrayList<Event> {
        eventsCalendar.timeInMillis = date.time
        val key = getKey(eventsCalendar)
        return getEventsForKey(key)?.events ?: ArrayList()
    }

    private fun getEventsForMonth(cal: Calendar): ArrayList<Events> {
        val yearMonthKey = getYearMonthKey(cal)
        val firstDay = yearMonthKey + 1
        val lastDay = yearMonthKey + cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return getEvents(firstDay, lastDay)
    }

    fun getEventsForKey(key: Int): Events? {
        return mEventsArray.get(key)
    }

    private fun getEvents(firstDay: Int, lastDay: Int): ArrayList<Events> {
        val result = ArrayList<Events>()
        for (day in firstDay..lastDay) {
            mEventsArray.get(day)?.apply {
                result.add(this)
                Log.d("TAG", "$this")
            }
        }
        return result
    }

    fun getEventsForMonth(eventTimeInMillis: Long): ArrayList<Event> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = eventTimeInMillis
        val events = getEventsForMonth(calendar)
        val allEventsForMonth = ArrayList<Event>()
        events.forEach {
            allEventsForMonth.addAll(it.events)
        }
        Collections.sort(allEventsForMonth, mEventsComparator)
        return allEventsForMonth
    }

    fun removeEventByEpochMillis(epochMillis: Long) {
        TODO()
    }

    private fun getYearMonthKey(cal: Calendar): Int =
            10000 * cal.get(Calendar.YEAR) + 100 * (1 + cal.get(Calendar.MONTH))

    private fun getKey(cal: Calendar): Int =
            getYearMonthKey(cal) + cal.get(Calendar.DAY_OF_MONTH)

}