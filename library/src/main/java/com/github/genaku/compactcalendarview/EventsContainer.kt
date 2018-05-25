package com.github.genaku.compactcalendarview

import com.github.genaku.compactcalendarview.comparators.EventComparator
import com.github.genaku.compactcalendarview.domain.Event
import java.util.*

internal class EventsContainer(private val eventsCalendar: Calendar) {

    private val eventsByMonthAndYearMap = HashMap<String, ArrayList<Events>>()
    private val eventsComparator = EventComparator()

    fun addEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        val key = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonth = eventsByMonthAndYearMap[key] ?: ArrayList()
        val eventsForTargetDay = getEventDayEvent(event.timeInMillis)
        if (eventsForTargetDay == null) {
            val events = ArrayList<Event>()
            events.add(event)
            eventsForMonth.add(Events(event.timeInMillis, events))
        } else {
            eventsForTargetDay.events.add(event)
        }
        eventsByMonthAndYearMap[key] = eventsForMonth
    }

    fun removeAllEvents() {
        eventsByMonthAndYearMap.clear()
    }

    fun addEvents(events: ArrayList<Event>) = events.forEach {
        addEvent(it)
    }

    fun updateEvents(events: ArrayList<Event>) {
        removeAllEvents()
        addEvents(events)
    }

    fun getEventsFor(epochMillis: Long): ArrayList<Event> =
            getEventDayEvent(epochMillis)?.events ?: ArrayList()

    fun getEventsForMonthAndYear(month: Int, year: Int): ArrayList<Events>? =
            eventsByMonthAndYearMap[year.toString() + "_" + month]

    fun getEventsForMonth(eventTimeInMillis: Long): ArrayList<Event> {
        eventsCalendar.timeInMillis = eventTimeInMillis
        val keyForCalendarEvent = getKeyForCalendarEvent(eventsCalendar)
        val events = eventsByMonthAndYearMap[keyForCalendarEvent]
        val allEventsForMonth = ArrayList<Event>()
        events?.forEach {
            allEventsForMonth.addAll(it.events)
        }
        Collections.sort(allEventsForMonth, eventsComparator)
        return allEventsForMonth
    }

    private fun getEventDayEvent(eventTimeInMillis: Long): Events? {
        eventsCalendar.timeInMillis = eventTimeInMillis

        val keyForCalendarEvent = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthsAndYear = eventsByMonthAndYearMap[keyForCalendarEvent]

        val dayInMonth = eventsCalendar.get(Calendar.DAY_OF_MONTH)
        eventsForMonthsAndYear?.forEach {
            eventsCalendar.timeInMillis = it.timeInMillis
            if (dayInMonth == eventsCalendar.get(Calendar.DAY_OF_MONTH)) {
                return it
            }
        }
        return null
    }

    fun removeEventByEpochMillis(epochMillis: Long) {
        eventsCalendar.timeInMillis = epochMillis
        val dayInMonth = eventsCalendar.get(Calendar.DAY_OF_MONTH)
        val key = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthAndYear = eventsByMonthAndYearMap[key] ?: return

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
            eventsByMonthAndYearMap.remove(key)
        }
    }

    fun removeEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        val key = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthAndYear = eventsByMonthAndYearMap[key] ?: return

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
            eventsByMonthAndYearMap.remove(key)
        }
    }

    fun removeEvents(events: ArrayList<Event>) = events.forEach {
        removeEvent(it)
    }

    //E.g. 4 2016 becomes 2016_4
    private fun getKeyForCalendarEvent(cal: Calendar): String =
            cal.get(Calendar.YEAR).toString() + "_" + cal.get(Calendar.MONTH)

}
