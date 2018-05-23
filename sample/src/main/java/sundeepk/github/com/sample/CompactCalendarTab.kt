package sundeepk.github.com.sample

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CompactCalendarTab : Fragment() {
    private val currentCalender = Calendar.getInstance(Locale.getDefault())
    private val dateFormatForDisplaying = SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault())
    private val dateFormatForMonth = SimpleDateFormat("MMM - yyyy", Locale.getDefault())
    private var shouldShow = false
    private var compactCalendarView: CompactCalendarView? = null
    private var toolbar: ActionBar? = null

    private val calendarShowLis: View.OnClickListener
        get() = View.OnClickListener {
            if (!compactCalendarView!!.isAnimating) {
                if (shouldShow) {
                    compactCalendarView!!.showCalendar()
                } else {
                    compactCalendarView!!.hideCalendar()
                }
                shouldShow = !shouldShow
            }
        }

    private val calendarExposeLis: View.OnClickListener
        get() = View.OnClickListener {
            if (!compactCalendarView!!.isAnimating) {
                if (shouldShow) {
                    compactCalendarView!!.showCalendarWithAnimation()
                } else {
                    compactCalendarView!!.hideCalendarWithAnimation()
                }
                shouldShow = !shouldShow
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.main_tab, container, false)

        val mutableBookings = ArrayList<String>()

        val bookingsListView = v.findViewById<View>(R.id.bookings_listview) as ListView
        val showPreviousMonthBut = v.findViewById<View>(R.id.prev_button) as Button
        val showNextMonthBut = v.findViewById<View>(R.id.next_button) as Button
        val slideCalendarBut = v.findViewById<View>(R.id.slide_calendar) as Button
        val showCalendarWithAnimationBut = v.findViewById<View>(R.id.show_with_animation_calendar) as Button
        val setLocaleBut = v.findViewById<View>(R.id.set_locale) as Button
        val removeAllEventsBut = v.findViewById<View>(R.id.remove_all_events) as Button

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, mutableBookings)
        bookingsListView.adapter = adapter
        compactCalendarView = v.findViewById<View>(R.id.compactcalendar_view) as CompactCalendarView

        // below allows you to configure color for the current day in the month
        // compactCalendarView.setCurrentDayBackgroundColor(getResources().getColor(R.color.black));
        // below allows you to configure colors for the current day the user has selected
        // compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.dark_red));
        compactCalendarView!!.setUseThreeLetterAbbreviation(false)
        compactCalendarView!!.setFirstDayOfWeek(Calendar.MONDAY)

        loadEvents()
        loadEventsForYear(2018)
        compactCalendarView!!.invalidate()

        logEventsByMonth(compactCalendarView!!)

        // below line will display Sunday as the first day of the week
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        // disable scrolling calendar
        // compactCalendarView.shouldScrollMonth(false);

        // show days from other months as greyed out days
        compactCalendarView!!.displayOtherMonthDays(true)

        // show Sunday as first day of month
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        //set initial title
        toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar!!.title = dateFormatForMonth.format(compactCalendarView!!.firstDayOfCurrentMonth)

        //set title on calendar scroll
        compactCalendarView!!.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                toolbar!!.title = dateFormatForMonth.format(dateClicked)
                val bookingsFromMap = compactCalendarView!!.getEvents(dateClicked)
                Log.d(TAG, "inside onclick " + dateFormatForDisplaying.format(dateClicked))
                if (bookingsFromMap != null) {
                    Log.d(TAG, bookingsFromMap.toString())
                    mutableBookings.clear()
                    for (booking in bookingsFromMap) {
                        mutableBookings.add(booking.data as String? ?: "")
                    }
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                toolbar!!.title = dateFormatForMonth.format(firstDayOfNewMonth)
            }
        })

        showPreviousMonthBut.setOnClickListener { compactCalendarView!!.showPreviousMonth() }

        showNextMonthBut.setOnClickListener { compactCalendarView!!.showNextMonth() }

        val showCalendarOnClickLis = calendarShowLis
        slideCalendarBut.setOnClickListener(showCalendarOnClickLis)

        val exposeCalendarListener = calendarExposeLis
        showCalendarWithAnimationBut.setOnClickListener(exposeCalendarListener)

        compactCalendarView!!.setAnimationListener(object : CompactCalendarView.CompactCalendarAnimationListener {
            override fun onOpened() {}

            override fun onClosed() {}
        })

        setLocaleBut.setOnClickListener {
            compactCalendarView!!.setCurrentDate(Date())
            //                Locale locale = Locale.FRANCE;
            //                dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", locale);
            //                TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");
            //                dateFormatForDisplaying.setTimeZone(timeZone);
            //                dateFormatForMonth.setTimeZone(timeZone);
            //                compactCalendarView.setLocale(timeZone, locale);
            //                compactCalendarView.setUseThreeLetterAbbreviation(false);
            //                loadEvents();
            //                loadEventsForYear(2017);
            //                logEventsByMonth(compactCalendarView);
        }

        removeAllEventsBut.setOnClickListener { compactCalendarView!!.removeAllEvents() }


        // uncomment below to show indicators above small indicator events
        compactCalendarView!!.shouldDrawIndicatorsBelowSelectedDays(true)

        // uncomment below to open onCreate
        openCalendarOnCreate(v)

        return v
    }

    private fun openCalendarOnCreate(v: View) {
        val layout = v.findViewById<View>(R.id.main_content) as RelativeLayout
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                compactCalendarView!!.showCalendarWithAnimation()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        toolbar!!.title = dateFormatForMonth.format(compactCalendarView!!.firstDayOfCurrentMonth)
        // Set to current day on resume to set calendar to latest day
        // toolbar.setTitle(dateFormatForMonth.format(new Date()));
    }

    private fun loadEvents() {
        addEvents(-1, -1)
        addEvents(Calendar.DECEMBER, -1)
        addEvents(Calendar.AUGUST, -1)
    }

    private fun loadEventsForYear(year: Int) {
        addEvents(Calendar.MAY, year)
        addEvents(Calendar.AUGUST, year)
    }

    private fun logEventsByMonth(compactCalendarView: CompactCalendarView) {
        currentCalender.time = Date()
        currentCalender.set(Calendar.DAY_OF_MONTH, 1)
        currentCalender.set(Calendar.MONTH, Calendar.AUGUST)
        val dates = ArrayList<String>()
        for (e in compactCalendarView.getEventsForMonth(Date())) {
            dates.add(dateFormatForDisplaying.format(e.timeInMillis))
        }
        Log.d(TAG, "Events for Aug with simple date formatter: $dates")
        Log.d(TAG, "Events for Aug month using default local and timezone: " + compactCalendarView.getEventsForMonth(currentCalender.time))
    }

    private fun addEvents(month: Int, year: Int) {
        currentCalender.time = Date()
        currentCalender.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = currentCalender.time
        for (i in 0..5) {
            currentCalender.time = firstDayOfMonth
            if (month > -1) {
                currentCalender.set(Calendar.MONTH, month)
            }
            if (year > -1) {
                currentCalender.set(Calendar.ERA, GregorianCalendar.AD)
                currentCalender.set(Calendar.YEAR, year)
            }
            currentCalender.add(Calendar.DATE, i)
            setToMidnight(currentCalender)
            val timeInMillis = currentCalender.timeInMillis

            val events = getEvents(timeInMillis, i)

            compactCalendarView!!.addEvents(events)
        }
    }

    private fun getEvents(timeInMillis: Long, day: Int): ArrayList<Event> {
        return when {
            day < 2 -> arrayListOf(Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)))
            day in 3..4 -> arrayListOf(
                    Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)),
                    Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + Date(timeInMillis)))
            else -> arrayListOf(
                    Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)),
                    Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + Date(timeInMillis)),
                    Event(Color.argb(255, 70, 68, 65), timeInMillis, "Event 3 at " + Date(timeInMillis)))
        }
    }

    private fun setToMidnight(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}