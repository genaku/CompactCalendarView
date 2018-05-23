package com.github.sundeepk.compactcalendarview

import android.graphics.Color
import com.github.sundeepk.compactcalendarview.domain.Event
import java.util.*

object CompactCalendarHelper {

    //generate one event per a day for a month
    @JvmOverloads
    fun getOneEventPerDayForMonth(start: Int, days: Int, timeStamp: Long, color: Int = Color.BLUE): ArrayList<Event> {
        val currentCalender = Calendar.getInstance(Locale.getDefault())
        val events = ArrayList<Event>()
        for (i in start until days) {
            setDateTime(timeStamp, currentCalender, i)
            events.add(Event(color, currentCalender.timeInMillis))
        }
        return events
    }

    fun getEvents(start: Int, days: Int, timeStamp: Long): ArrayList<Events> {
        val currentCalender = Calendar.getInstance(Locale.getDefault())
        val events = ArrayList<Events>()
        for (i in start until days) {
            setDateTime(timeStamp, currentCalender, i)
            val eventList = ArrayList<Event>()
            eventList.add(Event(Color.BLUE, currentCalender.timeInMillis))
            val eventsObject = Events(currentCalender.timeInMillis, eventList)
            events.add(eventsObject)
        }
        return events
    }


    fun getDayEventWith2EventsPerDay(start: Int, days: Int, timeStamp: Long): ArrayList<Events> {
        val currentCalender = Calendar.getInstance(Locale.getDefault())
        val events = ArrayList<Events>()
        for (i in start until days) {
            setDateTime(timeStamp, currentCalender, i)
            val eventList = ArrayList<Event>()
            eventList.add(Event(Color.BLUE, currentCalender.timeInMillis))
            eventList.add(Event(Color.RED, currentCalender.timeInMillis + 3600 * 1000))
            val eventsObject = Events(currentCalender.timeInMillis, eventList)
            events.add(eventsObject)
        }
        return events
    }

    fun getDayEventWithMultipleEventsPerDay(start: Int, days: Int, timeStamp: Long): ArrayList<Events> {
        val currentCalender = Calendar.getInstance(Locale.getDefault())
        val events = ArrayList<Events>()
        for (i in start until days) {
            setDateTime(timeStamp, currentCalender, i)
            val eventsList = arrayListOf(Event(Color.BLUE, currentCalender.timeInMillis),
                    Event(Color.RED, currentCalender.timeInMillis + 3600 * 1000),
                    Event(Color.RED, currentCalender.timeInMillis + 3600 * 2 * 1000),
                    Event(Color.RED, currentCalender.timeInMillis + 3600 * 3 * 1000))
            val eventsObject = Events(currentCalender.timeInMillis, eventsList)
            events.add(eventsObject)
        }
        return events
    }

    fun getMultipleEventsForEachDayAsMap(start: Int, days: Int, timeStamp: Long): Map<Long, ArrayList<Event>> {
        val currentCalender = Calendar.getInstance(Locale.getDefault())
        val epochMillisToEvents = HashMap<Long, ArrayList<Event>>()
        for (i in start until days) {
            setDateTime(timeStamp, currentCalender, i)
            val eventList = ArrayList<Event>()
            val events = Arrays.asList(Event(Color.BLUE, currentCalender.timeInMillis),
                    Event(Color.RED, currentCalender.timeInMillis + 3600 * 1000),
                    Event(Color.RED, currentCalender.timeInMillis + 3600 * 2 * 1000),
                    Event(Color.RED, currentCalender.timeInMillis + 3600 * 3 * 1000))
            eventList.addAll(events)
            epochMillisToEvents[currentCalender.timeInMillis] = eventList
        }
        return epochMillisToEvents
    }

    fun setDateTime(timeStamp: Long, currentCalender: Calendar, i: Int) {
        currentCalender.timeInMillis = timeStamp
        currentCalender.set(Calendar.DATE, 1)
        currentCalender.set(Calendar.HOUR_OF_DAY, 0)
        currentCalender.set(Calendar.MINUTE, 0)
        currentCalender.set(Calendar.SECOND, 0)
        currentCalender.set(Calendar.MILLISECOND, 0)
        currentCalender.add(Calendar.DATE, i)
    }

    fun setTimeToMidnightAndGet(cal: Calendar, epoch: Long): Long {
        cal.time = Date(epoch)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

}
