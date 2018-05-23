package com.github.sundeepk.compactcalendarview

import com.github.sundeepk.compactcalendarview.comparators.EventComparator
import com.github.sundeepk.compactcalendarview.domain.Event
import java.util.*

class EventsContainer(private val eventsCalendar: Calendar) {

    private val mEventsByMonthAndYearMap = HashMap<String, ArrayList<Events>>()
    private val mEventsComparator = EventComparator()

    fun addEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        val key = getKeyForCalendarEvent(eventsCalendar)
        var eventsForMonth: ArrayList<Events>? = mEventsByMonthAndYearMap[key]
        if (eventsForMonth == null) {
            eventsForMonth = ArrayList()
        }
        val eventsForTargetDay = getEventDayEvent(event.timeInMillis)
        if (eventsForTargetDay == null) {
            val events = ArrayList<Event>()
            events.add(event)
            eventsForMonth.add(Events(event.timeInMillis, events))
        } else {
            eventsForTargetDay.events.add(event)
        }
        mEventsByMonthAndYearMap[key] = eventsForMonth
    }

    fun removeAllEvents() {
        mEventsByMonthAndYearMap.clear()
    }

    fun addEvents(events: ArrayList<Event>) {
        val count = events.size
        for (i in 0 until count) {
            addEvent(events[i])
        }
    }

    fun getEventsFor(epochMillis: Long): ArrayList<Event>? {
        val events = getEventDayEvent(epochMillis)
        return if (events == null) {
            ArrayList()
        } else {
            events.events
        }
    }

    fun getEventsForMonthAndYear(month: Int, year: Int): ArrayList<Events> {
        return mEventsByMonthAndYearMap[year.toString() + "_" + month] ?: ArrayList()
    }

    fun getEventsForMonth(eventTimeInMillis: Long): ArrayList<Event> {
        eventsCalendar.timeInMillis = eventTimeInMillis
        val keyForCalendarEvent = getKeyForCalendarEvent(eventsCalendar)
        val events = mEventsByMonthAndYearMap[keyForCalendarEvent]
        val allEventsForMonth = ArrayList<Event>()
        if (events != null) {
            for (eve in events) {
                allEventsForMonth.addAll(eve.events)
            }
        }
        Collections.sort(allEventsForMonth, mEventsComparator)
        return allEventsForMonth
    }

    private fun getEventDayEvent(eventTimeInMillis: Long): Events? {
        eventsCalendar.timeInMillis = eventTimeInMillis
        val dayInMonth = eventsCalendar.get(Calendar.DAY_OF_MONTH)
        val keyForCalendarEvent = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthsAndYear = mEventsByMonthAndYearMap[keyForCalendarEvent]
        if (eventsForMonthsAndYear != null) {
            for (events in eventsForMonthsAndYear) {
                eventsCalendar.timeInMillis = events.timeInMillis
                val dayInMonthFromCache = eventsCalendar.get(Calendar.DAY_OF_MONTH)
                if (dayInMonthFromCache == dayInMonth) {
                    return events
                }
            }
        }
        return null
    }

    fun removeEventByEpochMillis(epochMillis: Long) {
        eventsCalendar.timeInMillis = epochMillis
        val dayInMonth = eventsCalendar.get(Calendar.DAY_OF_MONTH)
        val key = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthAndYear = mEventsByMonthAndYearMap[key]
        if (eventsForMonthAndYear != null) {
            val calendarDayEventIterator = eventsForMonthAndYear.iterator()
            while (calendarDayEventIterator.hasNext()) {
                val next = calendarDayEventIterator.next()
                eventsCalendar.timeInMillis = next.timeInMillis
                val dayInMonthFromCache = eventsCalendar.get(Calendar.DAY_OF_MONTH)
                if (dayInMonthFromCache == dayInMonth) {
                    calendarDayEventIterator.remove()
                    break
                }
            }
            if (eventsForMonthAndYear.isEmpty()) {
                mEventsByMonthAndYearMap.remove(key)
            }
        }
    }

    fun removeEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        val key = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthAndYear = mEventsByMonthAndYearMap[key]
        if (eventsForMonthAndYear != null) {
            val eventsForMonthYrItr = eventsForMonthAndYear.iterator()
            while (eventsForMonthYrItr.hasNext()) {
                val events = eventsForMonthYrItr.next()
                val indexOfEvent = events.events.indexOf(event)
                if (indexOfEvent >= 0) {
                    if (events.events.size == 1) {
                        eventsForMonthYrItr.remove()
                    } else {
                        events.events.removeAt(indexOfEvent)
                    }
                    break
                }
            }
            if (eventsForMonthAndYear.isEmpty()) {
                mEventsByMonthAndYearMap.remove(key)
            }
        }
    }

    fun removeEvents(events: List<Event>) {
        val count = events.size
        for (i in 0 until count) {
            removeEvent(events[i])
        }
    }

    //E.g. 4 2016 becomes 2016_4
    private fun getKeyForCalendarEvent(cal: Calendar): String {
        return cal.get(Calendar.YEAR).toString() + "_" + cal.get(Calendar.MONTH)
    }

}
