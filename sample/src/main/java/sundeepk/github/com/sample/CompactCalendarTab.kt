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

class CompactCalendarTab : Fragment() {

    private val mCurrentCalender = Calendar.getInstance(Locale.getDefault())
    private var mDateFormatForDisplaying = SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault())
    private val mDateFormatForMonth = SimpleDateFormat("MMM - yyyy", Locale.getDefault())
    private var mShouldShow = false
    private lateinit var compactCalendarView: CompactCalendarView
    private var toolbar: ActionBar? = null

    private val calendarShowListener: View.OnClickListener
        get() = View.OnClickListener {
            if (!compactCalendarView.isAnimating) {
                if (mShouldShow) {
                    compactCalendarView.showCalendar()
                } else {
                    compactCalendarView.hideCalendar()
                }
                mShouldShow = !mShouldShow
            }
        }

    private val calendarExposeListener: View.OnClickListener
        get() = View.OnClickListener {
            if (!compactCalendarView.isAnimating) {
                if (mShouldShow) {
                    compactCalendarView.showCalendarWithAnimation()
                } else {
                    compactCalendarView.hideCalendarWithAnimation()
                }
                mShouldShow = !mShouldShow
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.main_tab, container, false)

        val mutableBookings = ArrayList<String>()

        val bookingsListView = v.findViewById<ListView>(R.id.bookings_listview)
        val showPreviousMonthBut = v.findViewById<Button>(R.id.prev_button)
        val showNextMonthBut = v.findViewById<Button>(R.id.next_button)
        val slideCalendarBut = v.findViewById<Button>(R.id.slide_calendar)
        val showCalendarWithAnimationBut = v.findViewById<Button>(R.id.show_with_animation_calendar)
        val goTodayBut = v.findViewById<Button>(R.id.go_today)
        val removeAllEventsBut = v.findViewById<Button>(R.id.remove_all_events)

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, mutableBookings)
        bookingsListView.adapter = adapter
        compactCalendarView = v.findViewById(R.id.compactcalendar_view)

        // below allows you to configure color for the current day in the month
        // compactCalendarView.setCurrentDayBackgroundColor(getResources().getColor(R.color.black));
        // below allows you to configure colors for the current day the user has selected
        // compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.dark_red));
        compactCalendarView.setUseThreeLetterAbbreviation(false)
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY)

        loadEvents()
        loadEventsForYear(2017)
        compactCalendarView.invalidate()

        logEventsByMonth(compactCalendarView)

        // below line will display Sunday as the first day of the week
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        // disable scrolling calendar
        // compactCalendarView.shouldScrollMonth(false);

        // show days from other months as greyed out days
        // compactCalendarView.displayOtherMonthDays(true);

        // show Sunday as first day of month
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        //set initial title
        toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.title = mDateFormatForMonth.format(compactCalendarView.firstDayOfCurrentMonth)

        //set title on calendar scroll
        compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                toolbar?.title = mDateFormatForMonth.format(dateClicked)
                val bookingsFromMap = compactCalendarView.getEvents(dateClicked)
                Log.d(TAG, "inside onclick " + mDateFormatForDisplaying.format(dateClicked))
                Log.d(TAG, bookingsFromMap.toString())
                mutableBookings.clear()
                for (booking in bookingsFromMap) {
                    mutableBookings.add(booking.data as String)
                }
                adapter.notifyDataSetChanged()

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                toolbar?.title = mDateFormatForMonth.format(firstDayOfNewMonth)
            }
        })

        showPreviousMonthBut.setOnClickListener { compactCalendarView.scrollPreviousMonth() }

        showNextMonthBut.setOnClickListener { compactCalendarView.scrollNextMonth() }

        val showCalendarOnClickLis = calendarShowListener
        slideCalendarBut.setOnClickListener(showCalendarOnClickLis)

        val exposeCalendarListener = calendarExposeListener
        showCalendarWithAnimationBut.setOnClickListener(exposeCalendarListener)

        compactCalendarView.setAnimationListener(object : CompactCalendarView.CompactCalendarAnimationListener {
            override fun onOpened() {}

            override fun onClosed() {}
        })

        goTodayBut.setOnClickListener {
            compactCalendarView.setCurrentDate(Date())
//            val locale = Locale.FRANCE
//            mDateFormatForDisplaying = SimpleDateFormat("dd-M-yyyy hh:mm:ss a", locale)
//            val timeZone = TimeZone.getTimeZone("Europe/Paris")
//            mDateFormatForDisplaying.timeZone = timeZone
//            mDateFormatForMonth.timeZone = timeZone
//            compactCalendarView.setLocale(timeZone, locale)
//            compactCalendarView.setUseThreeLetterAbbreviation(false)
//            loadEvents()
//            loadEventsForYear(2017)
//            logEventsByMonth(compactCalendarView)
        }

        removeAllEventsBut.setOnClickListener { compactCalendarView.removeAllEvents() }


        // uncomment below to show indicators above small indicator events
        // compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        // uncomment below to open onCreate
        // openCalendarOnCreate(v);

        return v
    }

    private fun openCalendarOnCreate(v: View) {
        val layout = v.findViewById<RelativeLayout>(R.id.main_content)
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                compactCalendarView.showCalendarWithAnimation()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        toolbar?.title = mDateFormatForMonth.format(compactCalendarView.firstDayOfCurrentMonth)
        // Set to current day on resume to set calendar to latest day
        // toolbar.setTitle(mDateFormatForMonth.format(new Date()));
    }

    private fun loadEvents() {
        addEvents(-1, -1)
        addEvents(Calendar.DECEMBER, -1)
        addEvents(Calendar.AUGUST, -1)
    }

    private fun loadEventsForYear(year: Int) {
        addEvents(Calendar.DECEMBER, year)
        addEvents(Calendar.AUGUST, year)
    }

    private fun logEventsByMonth(compactCalendarView: CompactCalendarView) {
        mCurrentCalender.time = Date()
        mCurrentCalender.set(Calendar.DAY_OF_MONTH, 1)
        mCurrentCalender.set(Calendar.MONTH, Calendar.AUGUST)
        val dates = ArrayList<String>()
        for (e in compactCalendarView.getEventsForMonth(Date())) {
            dates.add(mDateFormatForDisplaying.format(e.timeInMillis))
        }
        Log.d(TAG, "Events for Aug with simple date formatter: $dates")
        Log.d(TAG, "Events for Aug month using default local and timezone: " + compactCalendarView.getEventsForMonth(mCurrentCalender.time))
    }

    private fun addEvents(month: Int, year: Int) {
        mCurrentCalender.time = Date()
        mCurrentCalender.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = mCurrentCalender.time
        for (i in 0..5) {
            mCurrentCalender.time = firstDayOfMonth
            if (month > -1) {
                mCurrentCalender.set(Calendar.MONTH, month)
            }
            if (year > -1) {
                mCurrentCalender.set(Calendar.ERA, GregorianCalendar.AD)
                mCurrentCalender.set(Calendar.YEAR, year)
            }
            mCurrentCalender.add(Calendar.DATE, i)
            setToMidnight(mCurrentCalender)
            val timeInMillis = mCurrentCalender.timeInMillis

            val events = getEvents(timeInMillis, i)

            compactCalendarView.addEvents(events)
        }
    }

    private fun getEvents(timeInMillis: Long, day: Int): ArrayList<Event> {
        val result = ArrayList<Event>()
        when {
            day < 2 -> result.add(Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)))
            day in 3..4 -> {
                result.add(Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)))
                result.add(Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + Date(timeInMillis)))
            }
            else -> {
                result.add(Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)))
                result.add(Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + Date(timeInMillis)))
                result.add(Event(Color.argb(255, 70, 68, 65), timeInMillis, "Event 3 at " + Date(timeInMillis)))
            }
        }
        return result
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