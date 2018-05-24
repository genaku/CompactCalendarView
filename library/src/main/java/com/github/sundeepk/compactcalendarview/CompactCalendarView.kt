package com.github.sundeepk.compactcalendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
import com.github.sundeepk.compactcalendarview.domain.Event
import java.util.*

class CompactCalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val mAnimationHandler: AnimationHandler

    private val mCompactCalendarController: CompactCalendarController = CompactCalendarController(Paint(), OverScroller(getContext()),
            Rect(), attrs, context, Color.argb(255, 233, 84, 81),
            Color.argb(255, 64, 64, 64), Color.argb(255, 219, 219, 219), VelocityTracker.obtain(),
            Color.argb(255, 100, 68, 65), EventsContainer(Calendar.getInstance()),
            Locale.getDefault(), TimeZone.getDefault())

    private val mGestureDetector: GestureDetectorCompat

    private var mHorizontalScrollEnabled = true

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {}

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            mCompactCalendarController.onSingleTapUp(e)
            invalidate()
            return super.onSingleTapUp(e)
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (mHorizontalScrollEnabled) {
                if (Math.abs(distanceX) > 0) {
                    parent.requestDisallowInterceptTouchEvent(true)

                    mCompactCalendarController.onScroll(e1, e2, distanceX, distanceY)
                    invalidate()
                    return true
                }
            }

            return false
        }
    }

    val heightPerDay: Int
        get() = mCompactCalendarController.heightPerDay

    val firstDayOfCurrentMonth: Date
        get() = mCompactCalendarController.firstDayOfCurrentMonth

    val weekNumberForCurrentMonth: Int
        get() = mCompactCalendarController.weekNumberForCurrentMonth

    val isAnimating: Boolean
        get() = mAnimationHandler.isAnimating

    init {
        mGestureDetector = GestureDetectorCompat(getContext(), gestureListener)
        mAnimationHandler = AnimationHandler(mCompactCalendarController, this)
    }

    /*
    Sets the name for each day of the week. No attempt is made to adjust width or text size based on the length of each day name.
    Works best with 3-4 characters for each day.
     */
    fun setDayColumnNames(dayColumnNames: ArrayList<String>) {
        mCompactCalendarController.setDayColumnNames(dayColumnNames)
    }

    /**
     * Adds multiple events to the calendar and invalidates the view once all events are added.
     */
    fun addEvents(events: ArrayList<Event>) {
        mCompactCalendarController.addEvents(events)
        invalidate()
    }

    fun setAnimationListener(compactCalendarAnimationListener: CompactCalendarAnimationListener) {
        mAnimationHandler.setCompactCalendarAnimationListener(compactCalendarAnimationListener)
    }

    /*
    Use a custom locale for compact calendar and reinitialise the view.
     */
    fun setLocale(timeZone: TimeZone, locale: Locale) {
        mCompactCalendarController.setLocale(timeZone, locale)
        invalidate()
    }

    /*
    Compact calendar will use the locale to determine the abbreviation to use as the day column names.
    The default is to use the default locale and to abbreviate the day names to one character.
    Setting this to true will displace the short weekday string provided by java.
     */
    fun setUseThreeLetterAbbreviation(useThreeLetterAbbreviation: Boolean) {
        mCompactCalendarController.setUseWeekDayAbbreviation(useThreeLetterAbbreviation)
        invalidate()
    }

    fun setCalendarBackgroundColor(calenderBackgroundColor: Int) {
        mCompactCalendarController.setCalenderBackgroundColor(calenderBackgroundColor)
        invalidate()
    }

    /**
     * Fetches the events for the date passed in
     * @param date
     * @return
     */
    fun getEvents(date: Date): ArrayList<Event> =
            mCompactCalendarController.getCalendarEventsFor(date.time)

    fun setFirstDayOfWeek(dayOfWeek: Int) {
        mCompactCalendarController.setFirstDayOfWeek(dayOfWeek)
        invalidate()
    }

    fun setCurrentSelectedDayBackgroundColor(currentSelectedDayBackgroundColor: Int) {
        mCompactCalendarController.setCurrentSelectedDayBackgroundColor(currentSelectedDayBackgroundColor)
        invalidate()
    }

    fun setCurrentDayBackgroundColor(currentDayBackgroundColor: Int) {
        mCompactCalendarController.setCurrentDayBackgroundColor(currentDayBackgroundColor)
        invalidate()
    }

    fun setListener(listener: CompactCalendarViewListener) {
        mCompactCalendarController.setListener(listener)
    }

    fun shouldDrawIndicatorsBelowSelectedDays(shouldDrawIndicatorsBelowSelectedDays: Boolean) {
        mCompactCalendarController.shouldDrawIndicatorsBelowSelectedDays(shouldDrawIndicatorsBelowSelectedDays)
    }

    fun setCurrentDate(dateTimeMonth: Date) {
        mCompactCalendarController.setCurrentDate(dateTimeMonth)
        invalidate()
    }

    fun scrollToDate(dateTimeMonth: Date) {
        mCompactCalendarController.scrollToDate(dateTimeMonth)
        invalidate()
    }

    fun setShouldDrawDaysHeader(shouldDrawDaysHeader: Boolean) {
        mCompactCalendarController.setShouldDrawDaysHeader(shouldDrawDaysHeader)
    }

    fun setCurrentSelectedDayTextColor(currentSelectedDayTextColor: Int) {
        mCompactCalendarController.setCurrentSelectedDayTextColor(currentSelectedDayTextColor)
    }

    fun setCurrentDayTextColor(currentDayTextColor: Int) {
        mCompactCalendarController.setCurrentDayTextColor(currentDayTextColor)
    }

    /**
     * Adds an event to be drawn as an indicator in the calendar.
     * If adding multiple events see [addEvents(List)][.]} method.
     * @param event to be added to the calendar
     * @param shouldInvalidate true if the view should invalidate
     */
    @JvmOverloads
    fun addEvent(event: Event, shouldInvalidate: Boolean = true) {
        mCompactCalendarController.addEvent(event)
        if (shouldInvalidate) {
            invalidate()
        }
    }

    /**
     * Fetches the events for the epochMillis passed in
     * @param epochMillis
     * @return
     */
    fun getEvents(epochMillis: Long): ArrayList<Event> =
            mCompactCalendarController.getCalendarEventsFor(epochMillis)

    /**
     * Fetches the events for the month of the epochMillis passed in and returns a sorted list of events
     * @param epochMillis
     * @return
     */
    fun getEventsForMonth(epochMillis: Long): ArrayList<Event> =
            mCompactCalendarController.getCalendarEventsForMonth(epochMillis)

    /**
     * Fetches the events for the month of the date passed in and returns a sorted list of events
     * @param date
     * @return
     */
    fun getEventsForMonth(date: Date): ArrayList<Event> =
            mCompactCalendarController.getCalendarEventsForMonth(date.time)

    /**
     * Removes multiple events from the calendar and invalidates the view once all events are added.
     */
    fun removeEvents(events: ArrayList<Event>) {
        mCompactCalendarController.removeEvents(events)
        invalidate()
    }

    interface CompactCalendarViewListener {
        fun onDayClick(dateClicked: Date)

        fun onMonthScroll(firstDayOfNewMonth: Date)
    }

    /**
     * Remove the event associated with the Date passed in
     * @param date
     */
    fun removeEvents(date: Date) {
        mCompactCalendarController.removeEventsFor(date.time)
    }

    fun removeEvents(epochMillis: Long) {
        mCompactCalendarController.removeEventsFor(epochMillis)
    }

    /**
     * Removes an event from the calendar.
     * If removing multiple events see [removeEvents(List)][.]
     *
     * @param event event to remove from the calendar
     * @param shouldInvalidate true if the view should invalidate
     */
    @JvmOverloads
    fun removeEvent(event: Event, shouldInvalidate: Boolean = true) {
        mCompactCalendarController.removeEvent(event)
        if (shouldInvalidate) {
            invalidate()
        }
    }

    interface CompactCalendarAnimationListener {
        fun onOpened()

        fun onClosed()
    }

    /**
     * Clears all Events from the calendar.
     */
    fun removeAllEvents() {
        mCompactCalendarController.removeAllEvents()
        invalidate()
    }

    fun shouldSelectFirstDayOfMonthOnScroll(shouldSelectFirstDayOfMonthOnScroll: Boolean) {
        mCompactCalendarController.setShouldSelectFirstDayOfMonthOnScroll(shouldSelectFirstDayOfMonthOnScroll)
    }

    fun setCurrentSelectedDayIndicatorStyle(currentSelectedDayIndicatorStyle: Int) {
        mCompactCalendarController.setCurrentSelectedDayIndicatorStyle(currentSelectedDayIndicatorStyle)
        invalidate()
    }

    fun setCurrentDayIndicatorStyle(currentDayIndicatorStyle: Int) {
        mCompactCalendarController.setCurrentDayIndicatorStyle(currentDayIndicatorStyle)
        invalidate()
    }

    fun setEventIndicatorStyle(eventIndicatorStyle: Int) {
        mCompactCalendarController.setEventIndicatorStyle(eventIndicatorStyle)
        invalidate()
    }

    private fun checkTargetHeight() {
        if (mCompactCalendarController.targetHeight <= 0) {
            throw IllegalStateException("Target height must be set in xml properties in order to expand/collapse CompactCalendar.")
        }
    }

    fun displayOtherMonthDays(displayOtherMonthDays: Boolean) {
        mCompactCalendarController.setDisplayOtherMonthDays(displayOtherMonthDays)
        invalidate()
    }

    fun setTargetHeight(targetHeight: Int) {
        mCompactCalendarController.targetHeight = targetHeight
        checkTargetHeight()
    }

    fun showCalendar() {
        checkTargetHeight()
        mAnimationHandler.openCalendar()
    }

    fun hideCalendar() {
        checkTargetHeight()
        mAnimationHandler.closeCalendar()
    }

    fun showCalendarWithAnimation() {
        checkTargetHeight()
        mAnimationHandler.openCalendarWithAnimation()
    }

    fun hideCalendarWithAnimation() {
        checkTargetHeight()
        mAnimationHandler.closeCalendarWithAnimation()
    }

    fun showNextMonth() {
        mCompactCalendarController.showNextMonth()
        invalidate()
    }

    fun showPreviousMonth() {
        mCompactCalendarController.showPreviousMonth()
        invalidate()
    }

    fun scrollNextMonth() {
        mCompactCalendarController.scrollNextMonth()
        invalidate()
    }

    fun scrollPreviousMonth() {
        mCompactCalendarController.scrollPreviousMonth()
        invalidate()
    }

    override fun onMeasure(parentWidth: Int, parentHeight: Int) {
        super.onMeasure(parentWidth, parentHeight)
        val width = View.MeasureSpec.getSize(parentWidth)
        val height = View.MeasureSpec.getSize(parentHeight)
        if (width > 0 && height > 0) {
            mCompactCalendarController.onMeasure(width, height, paddingRight, paddingLeft)
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        mCompactCalendarController.onDraw(canvas)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mCompactCalendarController.computeScroll()) {
            invalidate()
        }
    }

    fun shouldScrollMonth(enableHorizontalScroll: Boolean) {
        this.mHorizontalScrollEnabled = enableHorizontalScroll
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mHorizontalScrollEnabled) {
            mCompactCalendarController.onTouch(event)
            invalidate()
        }

        // on touch action finished (CANCEL or UP), we re-allow the parent container to intercept touch events (scroll inside ViewPager + RecyclerView issue #82)
        if ((event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP) && mHorizontalScrollEnabled) {
            parent.requestDisallowInterceptTouchEvent(false)
        }

        // always allow gesture Detector to detect onSingleTap and scroll events
        return mGestureDetector.onTouchEvent(event)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (this.visibility == View.GONE) {
            false
        } else this.mHorizontalScrollEnabled
        // Prevents ViewPager from scrolling horizontally by announcing that (issue #82)
    }

    companion object {
        const val FILL_LARGE_INDICATOR = 1
        const val NO_FILL_LARGE_INDICATOR = 2
        const val SMALL_INDICATOR = 3
    }

}
/**
 * see [.addEvent] when adding single events to control if calendar should redraw
 * or [addEvents(java.util.List)][.]  when adding multiple events
 * @param event
 */
/**
 * see [.removeEvent] when removing single events to control if calendar should redraw
 * or [removeEvents(java.util.List)][.] (java.util.List)}  when removing multiple events
 * @param event
 */
