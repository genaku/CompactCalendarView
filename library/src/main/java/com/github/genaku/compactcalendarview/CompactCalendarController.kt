package com.github.genaku.compactcalendarview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.OverScroller
import com.github.genaku.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.github.genaku.compactcalendarview.CompactCalendarView.Companion.FILL_LARGE_INDICATOR
import com.github.genaku.compactcalendarview.CompactCalendarView.Companion.NO_FILL_LARGE_INDICATOR
import com.github.genaku.compactcalendarview.CompactCalendarView.Companion.SMALL_INDICATOR
import com.github.genaku.compactcalendarview.domain.Event
import java.util.*

internal class CompactCalendarController(
        private val dayPaint: Paint,
        private val scroller: OverScroller,
        private val textSizeRect: Rect,
        attrs: AttributeSet?,
        context: Context,
        private var currentDayBackgroundColor: Int,
        private var calenderTextColor: Int,
        private var currentSelectedDayBackgroundColor: Int,
        velocityTracker: VelocityTracker,
        // colors
        private var multiEventIndicatorColor: Int,
        private var eventsContainer: EventsContainerNew,
        private var locale: Locale,
        private var timeZone: TimeZone?
) {

    private var mEventIndicatorStyle = SMALL_INDICATOR
    private var mCurrentDayIndicatorStyle = FILL_LARGE_INDICATOR
    private var mCurrentSelectedDayIndicatorStyle = FILL_LARGE_INDICATOR
    private var mPaddingWidth = 40
    private var mPaddingHeight = 40
    private var mTextHeight: Int = 0
    private var mWidthPerDay: Int = 0
    private var mMonthsScrolledSoFar: Int = 0
    var heightPerDay: Int = 0
        private set
    private var mTextSize = 30
    var width: Int = 0
        private set
    private var mHeight: Int = 0
    private var mPaddingRight: Int = 0
    private var mPaddingLeft: Int = 0
    private var mMaximumVelocity: Int = 0
    private var mDensityAdjustedSnapVelocity: Int = 0
    private var mDistanceThresholdForAutoScroll: Int = 0
    var targetHeight: Int = 0
    private var mAnimationStatus = 0
    private var mFirstDayOfWeekToDraw = Calendar.MONDAY
    private var mxIndicatorOffset: Float = 0f
    private var mMultiDayIndicatorStrokeWidth: Float = 0f
    var dayIndicatorRadius: Float = 0f
        private set
    private var mSmallIndicatorRadius: Float = 0f
    private var mGrowFactor = 0f
    private var mScreenDensity = 1f
    var growFactorIndicator: Float = 0f
    private var mDistanceX: Float = 0f
    private var mLastAutoScrollFromFling: Long = 0

    private var mUseThreeLetterAbbreviation = false
    private var mIsSmoothScrolling: Boolean = false
    private var mIsScrolling: Boolean = false
    private var mShouldDrawDaysHeader = true
    private var mShouldDrawIndicatorsBelowSelectedDays = false
    private var mShouldDrawOnlyOneIndicator = false
    private var mDisplayOtherMonthDays = false
    private var mShouldSelectFirstDayOfMonthOnScroll = true

    private var mListener: CompactCalendarViewListener? = null
    private var mVelocityTracker: VelocityTracker? = null
    private var mCurrentDirection = Direction.NONE
    private var mCurrentDate = Date()

    private lateinit var mCurrentCalender: Calendar
    private lateinit var mTodayCalender: Calendar
    private lateinit var mCalendarWithFirstDayOfMonth: Calendar
    private lateinit var mEventsCalendar: Calendar
    private lateinit var mCurrentVisibleCalendar: Calendar

    private val mAccumulatedScrollOffset = PointF()
    private val mBackground = Paint()
    private var mDayColumnNames: ArrayList<String> = ArrayList()
    private var mCurrentDayTextColor: Int = 0
    private var mCurrentSelectedDayTextColor: Int = 0
    private var mCalenderBackgroundColor = Color.WHITE
    private var mOtherMonthDaysTextColor: Int = 0

    /**
     * Only used in onDrawCurrentMonth to temporarily calculate previous month days
     */
    private lateinit var mTempPreviousMonthCalendar: Calendar

    //assume square around each day of width and height = heightPerDay and get diagonal line length
    //interpolate height and radius
    //https://en.wikipedia.org/wiki/Linear_interpolation
    // take into account indicator offset
    // pick a point which is almost half way through heightPerDay and textSizeRect
    private val interpolatedBigCircleIndicator: Float
        get() {
            val x0 = textSizeRect.height().toFloat()
            val x1 = heightPerDay.toFloat()
            val x = (x1 + textSizeRect.height()) / 2f
            val y1 = 0.5 * Math.sqrt((x1 * x1 + x1 * x1).toDouble())
            val y0 = 0.5 * Math.sqrt((x0 * x0 + x0 * x0).toDouble())

            return (y0 + (y1 - y0) * ((x - x0) / (x1 - x0))).toFloat()
        }

    val weekNumberForCurrentMonth: Int
        get() {
            val calendar = Calendar.getInstance(timeZone, locale)
            calendar.time = mCurrentDate
            return calendar.get(Calendar.WEEK_OF_MONTH)
        }

    val firstDayOfCurrentMonth: Date
        get() {
            val calendar = Calendar.getInstance(timeZone, locale)
            calendar.time = mCurrentDate
            calendar.add(Calendar.MONTH, -mMonthsScrolledSoFar)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            setToMidnight(calendar)
            return calendar.time
        }

    var currentDate: Date
        get() = mCurrentCalender.time
        set(value) {
            mDistanceX = 0f
            mMonthsScrolledSoFar = 0
            mAccumulatedScrollOffset.x = 0f
            scroller.startScroll(0, 0, 0, 0)
            mCurrentDate = Date(value.time)
            mCurrentCalender.time = mCurrentDate
            mTodayCalender = Calendar.getInstance(timeZone, locale)
            setToMidnight(mCurrentCalender)
            mCurrentDate
        }

/*
    fun setCurrentDate(dateTimeMonth: Date) {
        mDistanceX = 0f
        mMonthsScrolledSoFar = 0
        mAccumulatedScrollOffset.x = 0f
        scroller.startScroll(0, 0, 0, 0)
        mCurrentDate = Date(dateTimeMonth.time)
        mCurrentCalender.time = mCurrentDate
        mTodayCalender = Calendar.getInstance(timeZone, locale)
        setToMidnight(mCurrentCalender)
    }
*/


    private enum class Direction {
        NONE, HORIZONTAL, VERTICAL
    }

    init {
        mOtherMonthDaysTextColor = calenderTextColor
        mVelocityTracker = velocityTracker
        mDisplayOtherMonthDays = false
        loadAttributes(attrs, context)
        init(context)
    }

    private fun loadAttributes(attrs: AttributeSet?, context: Context?) {
        attrs ?: return
        context ?: return
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CompactCalendarView, 0, 0)
        try {
            currentDayBackgroundColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarCurrentDayBackgroundColor, currentDayBackgroundColor)
            calenderTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarTextColor, calenderTextColor)
            mCurrentDayTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarCurrentDayTextColor, calenderTextColor)
            mOtherMonthDaysTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarOtherMonthDaysTextColor, mOtherMonthDaysTextColor)
            currentSelectedDayBackgroundColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarCurrentSelectedDayBackgroundColor, currentSelectedDayBackgroundColor)
            mCurrentSelectedDayTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarCurrentSelectedDayTextColor, calenderTextColor)
            mCalenderBackgroundColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarBackgroundColor, mCalenderBackgroundColor)
            multiEventIndicatorColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarMultiEventIndicatorColor, multiEventIndicatorColor)
            mTextSize = typedArray.getDimensionPixelSize(R.styleable.CompactCalendarView_compactCalendarTextSize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize.toFloat(), context.resources.displayMetrics).toInt())
            targetHeight = typedArray.getDimensionPixelSize(R.styleable.CompactCalendarView_compactCalendarTargetHeight,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetHeight.toFloat(), context.resources.displayMetrics).toInt())
            mEventIndicatorStyle = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarEventIndicatorStyle, SMALL_INDICATOR)
            mCurrentDayIndicatorStyle = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarCurrentDayIndicatorStyle, FILL_LARGE_INDICATOR)
            mCurrentSelectedDayIndicatorStyle = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarCurrentSelectedDayIndicatorStyle, FILL_LARGE_INDICATOR)
            mDisplayOtherMonthDays = typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarDisplayOtherMonthDays, mDisplayOtherMonthDays)
            mShouldSelectFirstDayOfMonthOnScroll = typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarShouldSelectFirstDayOfMonthOnScroll, mShouldSelectFirstDayOfMonthOnScroll)
        } finally {
            typedArray.recycle()
        }
    }

    private fun init(context: Context?) {
        mCurrentCalender = Calendar.getInstance(timeZone, locale)
        mTodayCalender = Calendar.getInstance(timeZone, locale)
        mCalendarWithFirstDayOfMonth = Calendar.getInstance(timeZone, locale)
        mCurrentVisibleCalendar = Calendar.getInstance(timeZone, locale)
        mEventsCalendar = Calendar.getInstance(timeZone, locale)
        mTempPreviousMonthCalendar = Calendar.getInstance(timeZone, locale)

        // make setMinimalDaysInFirstWeek same across android versions
        mEventsCalendar.minimalDaysInFirstWeek = 1
        mCalendarWithFirstDayOfMonth.minimalDaysInFirstWeek = 1
        mTodayCalender.minimalDaysInFirstWeek = 1
        mCurrentCalender.minimalDaysInFirstWeek = 1
        mTempPreviousMonthCalendar.minimalDaysInFirstWeek = 1

        setFirstDayOfWeek(mFirstDayOfWeekToDraw)

        setUseWeekDayAbbreviation(false)
        dayPaint.textAlign = Paint.Align.CENTER
        dayPaint.style = Paint.Style.STROKE
        dayPaint.flags = Paint.ANTI_ALIAS_FLAG
        dayPaint.typeface = Typeface.SANS_SERIF
        dayPaint.textSize = mTextSize.toFloat()
        dayPaint.color = calenderTextColor
        dayPaint.getTextBounds("31", 0, "31".length, textSizeRect)
        mTextHeight = textSizeRect.height() * 3
        //        textWidth = textSizeRect.width() * 2;

        mTodayCalender.time = Date()
        setToMidnight(mTodayCalender)

        mCurrentCalender.time = mCurrentDate
        setCalenderToFirstDayOfMonth(mCalendarWithFirstDayOfMonth, mCurrentDate, -mMonthsScrolledSoFar, 0)

        initScreenDensityRelatedValues(context)

        mxIndicatorOffset = 3.5f * mScreenDensity

        //scale small indicator by screen density
        mSmallIndicatorRadius = 2.5f * mScreenDensity

        //just set a default grow Factor to draw full calendar when initialised
        mGrowFactor = Integer.MAX_VALUE.toFloat()
    }

    private fun initScreenDensityRelatedValues(context: Context?) {
        context ?: return
        mScreenDensity = context.resources.displayMetrics.density
        val configuration = ViewConfiguration.get(context)
        mDensityAdjustedSnapVelocity = (mScreenDensity * SNAP_VELOCITY_DIP_PER_SECOND).toInt()
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity

        val dm = context.resources.displayMetrics
        mMultiDayIndicatorStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
    }

    private fun setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth: Calendar, currentDate: Date, scrollOffset: Int, monthOffset: Int) {
        setMonthOffset(calendarWithFirstDayOfMonth, currentDate, scrollOffset, monthOffset)
        calendarWithFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
    }

    private fun setMonthOffset(calendarWithFirstDayOfMonth: Calendar, currentDate: Date, scrollOffset: Int, monthOffset: Int) {
        calendarWithFirstDayOfMonth.time = currentDate
        calendarWithFirstDayOfMonth.add(Calendar.MONTH, scrollOffset + monthOffset)
        calendarWithFirstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        calendarWithFirstDayOfMonth.set(Calendar.MINUTE, 0)
        calendarWithFirstDayOfMonth.set(Calendar.SECOND, 0)
        calendarWithFirstDayOfMonth.set(Calendar.MILLISECOND, 0)
    }

    fun setShouldSelectFirstDayOfMonthOnScroll(shouldSelectFirstDayOfMonthOnScroll: Boolean) {
        mShouldSelectFirstDayOfMonthOnScroll = shouldSelectFirstDayOfMonthOnScroll
    }

    fun setDisplayOtherMonthDays(displayOtherMonthDays: Boolean) {
        mDisplayOtherMonthDays = displayOtherMonthDays
    }

    fun shouldDrawIndicatorsBelowSelectedDays(shouldDrawIndicatorsBelowSelectedDays: Boolean) {
        mShouldDrawIndicatorsBelowSelectedDays = shouldDrawIndicatorsBelowSelectedDays
    }

    fun shouldDrawOnlyOneIndicator(shouldDrawOnlyOneIndicator: Boolean) {
        mShouldDrawOnlyOneIndicator = shouldDrawOnlyOneIndicator
    }

    fun setCurrentDayIndicatorStyle(currentDayIndicatorStyle: Int) {
        mCurrentDayIndicatorStyle = currentDayIndicatorStyle
    }

    fun setEventIndicatorStyle(eventIndicatorStyle: Int) {
        mEventIndicatorStyle = eventIndicatorStyle
    }

    fun setCurrentSelectedDayIndicatorStyle(currentSelectedDayIndicatorStyle: Int) {
        mCurrentSelectedDayIndicatorStyle = currentSelectedDayIndicatorStyle
    }

    fun setAnimationStatus(animationStatus: Int) {
        mAnimationStatus = animationStatus
    }

    fun setListener(listener: CompactCalendarViewListener) {
        mListener = listener
    }

    fun removeAllEvents() {
        eventsContainer.removeAllEvents()
    }

    fun setFirstDayOfWeek(day: Int) {
        if (day < 1 || day > 7) {
            throw IllegalArgumentException("Day must be an int between 1 and 7 or DAY_OF_WEEK from Java Calendar class. For more information please see Calendar.DAY_OF_WEEK.")
        }
        this.mFirstDayOfWeekToDraw = day
        setUseWeekDayAbbreviation(mUseThreeLetterAbbreviation)
        mEventsCalendar.firstDayOfWeek = day
        mCalendarWithFirstDayOfMonth.firstDayOfWeek = day
        mTodayCalender.firstDayOfWeek = day
        mCurrentCalender.firstDayOfWeek = day
        mTempPreviousMonthCalendar.firstDayOfWeek = day
    }

    fun setCurrentSelectedDayBackgroundColor(currentSelectedDayBackgroundColor: Int) {
        this.currentSelectedDayBackgroundColor = currentSelectedDayBackgroundColor
    }

    fun setCurrentSelectedDayTextColor(currentSelectedDayTextColor: Int) {
        mCurrentSelectedDayTextColor = currentSelectedDayTextColor
    }

    fun setCalenderBackgroundColor(calenderBackgroundColor: Int) {
        mCalenderBackgroundColor = calenderBackgroundColor
    }

    fun setCurrentDayBackgroundColor(currentDayBackgroundColor: Int) {
        this.currentDayBackgroundColor = currentDayBackgroundColor
    }

    fun setCurrentDayTextColor(currentDayTextColor: Int) {
        mCurrentDayTextColor = currentDayTextColor
    }

    fun showNextMonth() {
        mMonthsScrolledSoFar--
        mAccumulatedScrollOffset.x = (mMonthsScrolledSoFar * width).toFloat()
        if (mShouldSelectFirstDayOfMonthOnScroll) {
            setCalenderToFirstDayOfMonth(mCalendarWithFirstDayOfMonth, mCurrentCalender.time, 0, 1)
            currentDate = mCalendarWithFirstDayOfMonth.time
        }
        performMonthScrollCallback()
    }

    fun showPreviousMonth() {
        mMonthsScrolledSoFar++
        mAccumulatedScrollOffset.x = (mMonthsScrolledSoFar * width).toFloat()
        if (mShouldSelectFirstDayOfMonthOnScroll) {
            setCalenderToFirstDayOfMonth(mCalendarWithFirstDayOfMonth, mCurrentCalender.time, 0, -1)
            currentDate = mCalendarWithFirstDayOfMonth.time
        }
        performMonthScrollCallback()
    }

    fun setLocale(timeZone: TimeZone, locale: Locale) {
        this.locale = locale
        this.timeZone = timeZone
        this.eventsContainer = EventsContainerNew(Calendar.getInstance(this.timeZone, this.locale))
        // passing null will not re-init density related values - and that's ok
        init(null)
    }

    fun setUseWeekDayAbbreviation(useThreeLetterAbbreviation: Boolean) {
        mUseThreeLetterAbbreviation = useThreeLetterAbbreviation
        mDayColumnNames = WeekUtils.getWeekdayNames(locale, mFirstDayOfWeekToDraw, this.mUseThreeLetterAbbreviation)
    }

    fun setDayColumnNames(dayColumnNames: ArrayList<String>) {
        if (dayColumnNames.size != 7) {
            throw IllegalArgumentException("Column names cannot be null and must contain a value for each day of the week")
        }
        mDayColumnNames = dayColumnNames
    }

    fun setShouldDrawDaysHeader(shouldDrawDaysHeader: Boolean) {
        mShouldDrawDaysHeader = shouldDrawDaysHeader
    }

    fun onMeasure(width: Int, height: Int, paddingRight: Int, paddingLeft: Int) {
        mWidthPerDay = width / DAYS_IN_WEEK
        heightPerDay = if (targetHeight > 0) targetHeight / 7 else height / 7
        this.width = width
        mDistanceThresholdForAutoScroll = (width * 0.50).toInt()
        mHeight = height
        mPaddingRight = paddingRight
        mPaddingLeft = paddingLeft

        //makes easier to find radius
        dayIndicatorRadius = interpolatedBigCircleIndicator

        // scale the selected day indicators slightly so that event indicators can be drawn below
        dayIndicatorRadius = if (mShouldDrawIndicatorsBelowSelectedDays && mEventIndicatorStyle == SMALL_INDICATOR) dayIndicatorRadius * 0.85f else dayIndicatorRadius
    }

    fun onDraw(canvas: Canvas) {
        mPaddingWidth = mWidthPerDay / 2
        mPaddingHeight = heightPerDay / 2
        calculateXPositionOffset()

        when (mAnimationStatus) {
            EXPOSE_CALENDAR_ANIMATION -> drawCalendarWhileAnimating(canvas)
            ANIMATE_INDICATORS -> drawCalendarWhileAnimatingIndicators(canvas)
            else -> {
                drawCalenderBackground(canvas)
                drawScrollableCalender(canvas)
            }
        }
    }

    private fun drawCalendarWhileAnimatingIndicators(canvas: Canvas) {
        dayPaint.color = mCalenderBackgroundColor
        dayPaint.style = Paint.Style.FILL
        canvas.drawCircle(0f, 0f, mGrowFactor, dayPaint)
        dayPaint.style = Paint.Style.STROKE
        dayPaint.color = Color.WHITE
        drawScrollableCalender(canvas)
    }

    private fun drawCalendarWhileAnimating(canvas: Canvas) {
        mBackground.color = mCalenderBackgroundColor
        mBackground.style = Paint.Style.FILL
        canvas.drawCircle(0f, 0f, mGrowFactor, mBackground)
        dayPaint.style = Paint.Style.STROKE
        dayPaint.color = Color.WHITE
        drawScrollableCalender(canvas)
    }

    fun onSingleTapUp(e: MotionEvent) {
        // Don't handle single tap when calendar is scrolling and is not stationary
        if (isScrolling()) {
            return
        }

        val dayColumn = Math.round((mPaddingLeft + e.x - mPaddingWidth.toFloat() - mPaddingRight.toFloat()) / mWidthPerDay)
        val dayRow = Math.round((e.y - mPaddingHeight) / heightPerDay)

        setCalenderToFirstDayOfMonth(mCalendarWithFirstDayOfMonth, mCurrentDate, -mMonthsScrolledSoFar, 0)

        val firstDayOfMonth = getDayOfWeek(mCalendarWithFirstDayOfMonth)

        val dayOfMonth = (dayRow - 1) * 7 + dayColumn - firstDayOfMonth

        if (dayOfMonth < mCalendarWithFirstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH) && dayOfMonth >= 0) {
            mCalendarWithFirstDayOfMonth.add(Calendar.DATE, dayOfMonth)

            mCurrentCalender.timeInMillis = mCalendarWithFirstDayOfMonth.timeInMillis
            performOnDayClickCallback(mCurrentCalender.time)
        }
    }

    // zero based indexes used internally so instead of returning range of 1-7 like calendar class
    // it returns 0-6 where 0 is Sunday instead of 1
    fun getDayOfWeek(calendar: Calendar): Int {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeekToDraw
        return if (dayOfWeek < 0) 7 + dayOfWeek else dayOfWeek
    }

    // Add a little leeway buy checking if amount scrolled is almost same as expected scroll
    // as it maybe off by a few pixels
    private fun isScrolling(): Boolean {
        val scrolledX = Math.abs(mAccumulatedScrollOffset.x)
        val expectedScrollX = Math.abs(width * mMonthsScrolledSoFar)
        return scrolledX < expectedScrollX - 5 || scrolledX > expectedScrollX + 5
    }

    private fun performOnDayClickCallback(date: Date) {
        mListener?.onDayClick(date)
    }

    fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        //ignore scrolling callback if already smooth scrolling
        if (mIsSmoothScrolling) {
            return true
        }

        if (mCurrentDirection == Direction.NONE) {
            mCurrentDirection = if (Math.abs(distanceX) > Math.abs(distanceY)) {
                Direction.HORIZONTAL
            } else {
                Direction.VERTICAL
            }
        }

        mIsScrolling = true
        mDistanceX = distanceX
        return true
    }

    fun onTouch(event: MotionEvent): Boolean {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }

        mVelocityTracker?.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                }
                mIsSmoothScrolling = false
            }
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker?.addMovement(event)
                mVelocityTracker?.computeCurrentVelocity(500)
            }
            MotionEvent.ACTION_UP -> {
                handleHorizontalScrolling()
                mVelocityTracker?.recycle()
                mVelocityTracker?.clear()
                mVelocityTracker = null
                mIsScrolling = false
            }
        }
        return false
    }

    private fun snapBackScroller() {
        val remainingScrollAfterFingerLifted1 = mAccumulatedScrollOffset.x - mMonthsScrolledSoFar * width
        scroller.startScroll(mAccumulatedScrollOffset.x.toInt(), 0, (-remainingScrollAfterFingerLifted1).toInt(), 0)
    }

    private fun handleHorizontalScrolling() {
        val velocityX = computeVelocity()
        handleSmoothScrolling(velocityX)

        mCurrentDirection = Direction.NONE
        setCalenderToFirstDayOfMonth(mCalendarWithFirstDayOfMonth, mCurrentDate, -mMonthsScrolledSoFar, 0)

        if (mCalendarWithFirstDayOfMonth.get(Calendar.MONTH) != mCurrentCalender.get(Calendar.MONTH) && mShouldSelectFirstDayOfMonthOnScroll) {
            setCalenderToFirstDayOfMonth(mCurrentCalender, mCurrentDate, -mMonthsScrolledSoFar, 0)
        }
    }

    private fun computeVelocity(): Int {
        mVelocityTracker?.computeCurrentVelocity(VELOCITY_UNIT_PIXELS_PER_SECOND, mMaximumVelocity.toFloat())
        return mVelocityTracker?.xVelocity?.toInt() ?: 0
    }

    private fun handleSmoothScrolling(velocityX: Int) {
        val distanceScrolled = (mAccumulatedScrollOffset.x - width * mMonthsScrolledSoFar).toInt()
        val isEnoughTimeElapsedSinceLastSmoothScroll = System.currentTimeMillis() - mLastAutoScrollFromFling > LAST_FLING_THRESHOLD_MILLIS
        when {
            isEnoughTimeElapsedSinceLastSmoothScroll && velocityX > mDensityAdjustedSnapVelocity -> scrollPreviousMonth()
            isEnoughTimeElapsedSinceLastSmoothScroll && velocityX < -mDensityAdjustedSnapVelocity -> scrollNextMonth()
            mIsScrolling && distanceScrolled > mDistanceThresholdForAutoScroll -> scrollPreviousMonth()
            mIsScrolling && distanceScrolled < -mDistanceThresholdForAutoScroll -> scrollNextMonth()
            else -> {
                mIsSmoothScrolling = false
                snapBackScroller()
            }
        }
    }

    fun scrollNextMonth() {
        mLastAutoScrollFromFling = System.currentTimeMillis()
        mMonthsScrolledSoFar--
        performScroll()
        mIsSmoothScrolling = true
        performMonthScrollCallback()
    }

    fun scrollPreviousMonth() {
        mLastAutoScrollFromFling = System.currentTimeMillis()
        mMonthsScrolledSoFar++
        performScroll()
        mIsSmoothScrolling = true
        performMonthScrollCallback()
    }

    private fun performMonthScrollCallback() {
        mListener?.onMonthScroll(firstDayOfCurrentMonth)
    }

    private fun performScroll() {
        val targetScroll = mMonthsScrolledSoFar * width
        val remainingScrollAfterFingerLifted = targetScroll - mAccumulatedScrollOffset.x
        scroller.startScroll(mAccumulatedScrollOffset.x.toInt(), 0, remainingScrollAfterFingerLifted.toInt(), 0,
                (Math.abs(remainingScrollAfterFingerLifted.toInt()) / width.toFloat() * ANIMATION_SCREEN_SET_DURATION_MILLIS).toInt())
    }

    fun scrollToDate(dateTimeMonth: Date) {
        val monthDiff = monthDiff(dateTimeMonth)
        Log.d("TAG", "scroll $monthDiff months")
        for (i in 1..(-monthDiff)) {
            scrollNextMonth()
        }
        for (i in 1..monthDiff) {
            scrollPreviousMonth()
        }
        mCurrentDate = Date(dateTimeMonth.time)
        mCurrentCalender.time = mCurrentDate
        mTodayCalender = Calendar.getInstance(timeZone, locale)
        setToMidnight(mCurrentCalender)
    }

    private fun monthDiff(dateTo: Date): Int {
        val currentYear = mCurrentVisibleCalendar.get(Calendar.YEAR)
        val currentMonth = mCurrentVisibleCalendar.get(Calendar.MONTH)
        val calendar = Calendar.getInstance(timeZone, locale)
        calendar.time = dateTo
        val newYear = calendar.get(Calendar.YEAR)
        val newMonth = calendar.get(Calendar.MONTH)
        return currentYear * 12 + currentMonth - newYear * 12 - newMonth
    }

    private fun setToMidnight(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun addEvent(event: Event) {
        eventsContainer.addEvent(event)
    }

    fun addEvents(events: ArrayList<Event>) {
        eventsContainer.addEvents(events)
    }

    fun updateEvents(events: ArrayList<Event>) {
        eventsContainer.setEvents(events)
    }

    fun getCalendarEventsFor(date: Date): ArrayList<Event> {
        return eventsContainer.getDateEvents(date)
    }

    fun getCalendarEventsForMonth(epochMillis: Long): ArrayList<Event> {
        return eventsContainer.getEventsForMonth(epochMillis)
    }

    fun removeEventsFor(epochMillis: Long) {
        eventsContainer.removeEventByEpochMillis(epochMillis)
    }

    fun removeEvent(event: Event) {
        eventsContainer.removeEvent(event)
    }

    fun removeEvents(events: ArrayList<Event>) {
        eventsContainer.removeEvents(events)
    }

    fun setGrowProgress(grow: Float) {
        mGrowFactor = grow
    }

    fun onDown(e: MotionEvent): Boolean {
        scroller.forceFinished(true)
        return true
    }

    fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        scroller.forceFinished(true)
        return true
    }

    fun computeScroll(): Boolean {
        if (scroller.computeScrollOffset()) {
            mAccumulatedScrollOffset.x = scroller.currX.toFloat()
            return true
        }
        return false
    }

    private fun drawScrollableCalender(canvas: Canvas) {
        drawPreviousMonth(canvas)
        drawCurrentMonth(canvas)
        drawNextMonth(canvas)
    }

    private fun drawNextMonth(canvas: Canvas) {
        setCalenderToFirstDayOfMonth(mCalendarWithFirstDayOfMonth, mCurrentDate, -mMonthsScrolledSoFar, 1)
        drawMonth(canvas, mCalendarWithFirstDayOfMonth, width * (-mMonthsScrolledSoFar + 1))
    }

    private fun drawCurrentMonth(canvas: Canvas) {
        setCalenderToFirstDayOfMonth(mCurrentVisibleCalendar, mCurrentDate, -mMonthsScrolledSoFar, 0)
        drawMonth(canvas, mCurrentVisibleCalendar, width * -mMonthsScrolledSoFar)
    }

    private fun drawPreviousMonth(canvas: Canvas) {
        setCalenderToFirstDayOfMonth(mCalendarWithFirstDayOfMonth, mCurrentDate, -mMonthsScrolledSoFar, -1)
        drawMonth(canvas, mCalendarWithFirstDayOfMonth, width * (-mMonthsScrolledSoFar - 1))
    }

    private fun calculateXPositionOffset() {
        if (mCurrentDirection == Direction.HORIZONTAL) {
            mAccumulatedScrollOffset.x -= mDistanceX
        }
    }

    private fun drawCalenderBackground(canvas: Canvas) {
        dayPaint.apply {
            color = mCalenderBackgroundColor
            style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, width.toFloat(), mHeight.toFloat(), this)
            style = Paint.Style.STROKE
            color = calenderTextColor
        }
    }

    fun drawEvents(canvas: Canvas, currentMonthToDrawCalender: Calendar, offset: Int, uniqueEvents: ArrayList<Events>?) {
        uniqueEvents ?: return
        val currentMonth = currentMonthToDrawCalender.get(Calendar.MONTH)

        val shouldDrawCurrentDayCircle = currentMonth == mTodayCalender.get(Calendar.MONTH)
        val shouldDrawSelectedDayCircle = currentMonth == mCurrentCalender.get(Calendar.MONTH)

        val todayDayOfMonth = mTodayCalender.get(Calendar.DAY_OF_MONTH)
        val currentYear = mTodayCalender.get(Calendar.YEAR)
        val selectedDayOfMonth = mCurrentCalender.get(Calendar.DAY_OF_MONTH)
        val indicatorOffset = dayIndicatorRadius / 2
        for (i in uniqueEvents.indices) {
            val events = uniqueEvents[i]
            val timeMillis = events.timeInMillis
            mEventsCalendar.timeInMillis = timeMillis

            val dayOfWeek = getDayOfWeek(mEventsCalendar)

            val weekNumberForMonth = mEventsCalendar.get(Calendar.WEEK_OF_MONTH)
            val xPosition = (mWidthPerDay * dayOfWeek).toFloat() + mPaddingWidth.toFloat() + mPaddingLeft.toFloat() + mAccumulatedScrollOffset.x + offset.toFloat() - mPaddingRight
            var yPosition = (weekNumberForMonth * heightPerDay + mPaddingHeight).toFloat()

            if ((mAnimationStatus == EXPOSE_CALENDAR_ANIMATION || mAnimationStatus == ANIMATE_INDICATORS) && xPosition >= mGrowFactor || yPosition >= mGrowFactor) {
                // only draw small event indicators if enough of the calendar is exposed
                continue
            } else if (mAnimationStatus == EXPAND_COLLAPSE_CALENDAR && yPosition >= mGrowFactor) {
                // expanding animation, just draw event indicators if enough of the calendar is visible
                continue
            } else if (mAnimationStatus == EXPOSE_CALENDAR_ANIMATION && (mEventIndicatorStyle == FILL_LARGE_INDICATOR || mEventIndicatorStyle == NO_FILL_LARGE_INDICATOR)) {
                // Don't draw large indicators during expose animation, until animation is done
                continue
            }

            val eventsList = events.events
            val dayOfMonth = mEventsCalendar.get(Calendar.DAY_OF_MONTH)
            val eventYear = mEventsCalendar.get(Calendar.YEAR)
            val isSameDayAsCurrentDay = shouldDrawCurrentDayCircle && todayDayOfMonth == dayOfMonth && eventYear == currentYear
            val isCurrentSelectedDay = shouldDrawSelectedDayCircle && selectedDayOfMonth == dayOfMonth

            if (mShouldDrawIndicatorsBelowSelectedDays || !mShouldDrawIndicatorsBelowSelectedDays && !isSameDayAsCurrentDay && !isCurrentSelectedDay || mAnimationStatus == EXPOSE_CALENDAR_ANIMATION) {
                if (mEventIndicatorStyle == FILL_LARGE_INDICATOR || mEventIndicatorStyle == NO_FILL_LARGE_INDICATOR) {
                    if (!eventsList.isEmpty()) {
                        val event = eventsList[0]
                        drawEventIndicatorCircle(canvas, xPosition, yPosition, event.color)
                    }
                } else {
                    yPosition += indicatorOffset
                    // offset event indicators to draw below selected day indicators
                    // this makes sure that they do no overlap
                    if (mShouldDrawIndicatorsBelowSelectedDays && (isSameDayAsCurrentDay || isCurrentSelectedDay)) {
                        yPosition += indicatorOffset
                    }

                    when {
                        mShouldDrawOnlyOneIndicator -> drawSingleEvent(canvas, xPosition, yPosition, eventsList)
                        eventsList.size >= 3 -> drawEventsWithPlus(canvas, xPosition, yPosition, eventsList)
                        eventsList.size == 2 -> drawTwoEvents(canvas, xPosition, yPosition, eventsList)
                        eventsList.size == 1 -> drawSingleEvent(canvas, xPosition, yPosition, eventsList)
                    }
                }
            }
        }
    }

    private fun drawSingleEvent(canvas: Canvas, xPosition: Float, yPosition: Float, eventsList: ArrayList<Event>) {
        val event = eventsList[0]
        drawEventIndicatorCircle(canvas, xPosition, yPosition, event.color)
    }

    private fun drawTwoEvents(canvas: Canvas, xPosition: Float, yPosition: Float, eventsList: ArrayList<Event>) {
        //draw fist event just left of center
        drawEventIndicatorCircle(canvas, xPosition + mxIndicatorOffset * -1, yPosition, eventsList[0].color)
        //draw second event just right of center
        drawEventIndicatorCircle(canvas, xPosition + mxIndicatorOffset * 1, yPosition, eventsList[1].color)
    }

    //draw 2 eventsByMonthAndYearMap followed by plus indicator to show there are more than 2 eventsByMonthAndYearMap
    private fun drawEventsWithPlus(canvas: Canvas, xPosition: Float, yPosition: Float, eventsList: ArrayList<Event>) {
        // k = size() - 1, but since we don't want to draw more than 2 indicators, we just stop after 2 iterations so we can just hard k = -2 instead
        // we can use the below loop to draw arbitrary eventsByMonthAndYearMap based on the current screen size, for example, larger screens should be able to
        // display more than 2 evens before displaying plus indicator, but don't draw more than 3 indicators for now
        var j = 0
        var k = -2
        while (j < 3) {
            val event = eventsList[j]
            val xStartPosition = xPosition + mxIndicatorOffset * k
            if (j == 2) {
                dayPaint.color = multiEventIndicatorColor
                dayPaint.strokeWidth = mMultiDayIndicatorStrokeWidth
                canvas.drawLine(xStartPosition - mSmallIndicatorRadius, yPosition, xStartPosition + mSmallIndicatorRadius, yPosition, dayPaint)
                canvas.drawLine(xStartPosition, yPosition - mSmallIndicatorRadius, xStartPosition, yPosition + mSmallIndicatorRadius, dayPaint)
                dayPaint.strokeWidth = 0f
            } else {
                drawEventIndicatorCircle(canvas, xStartPosition, yPosition, event.color)
            }
            j++
            k += 2
        }
    }

    fun drawWeek(canvas: Canvas, weekToDrawCalender: Calendar, offset: Int) {
        mEventsCalendar.timeInMillis = weekToDrawCalender.timeInMillis
        val events = eventsContainer.getEventsForWeek(mEventsCalendar)
        drawEvents(
                canvas = canvas,
                currentMonthToDrawCalender = weekToDrawCalender,
                offset = offset,
                uniqueEvents = events
        )
        drawWeekDays(canvas, offset)
        for (dayRow in 1..6) {
            drawWeek(dayRow, canvas, weekToDrawCalender, offset)
        }
    }

    fun drawMonth(canvas: Canvas, monthToDrawCalender: Calendar, offset: Int) {
        mEventsCalendar.timeInMillis = monthToDrawCalender.timeInMillis
        val events = eventsContainer.getEventsForMonth(mEventsCalendar)
        drawEvents(
                canvas = canvas,
                currentMonthToDrawCalender = monthToDrawCalender,
                offset = offset,
                uniqueEvents = events
        )
        drawWeekDays(canvas, offset)
        for (dayRow in 1..6) {
            drawWeek(dayRow, canvas, monthToDrawCalender, offset)
        }
    }

    private fun drawWeek(dayRow: Int, canvas: Canvas, monthToDrawCalender: Calendar, offset: Int) {
        val isSameMonthAsToday = monthToDrawCalender.get(Calendar.MONTH) == mTodayCalender.get(Calendar.MONTH)
        val isSameYearAsToday = monthToDrawCalender.get(Calendar.YEAR) == mTodayCalender.get(Calendar.YEAR)
        val isSameMonthAsCurrentCalendar = monthToDrawCalender.get(Calendar.MONTH) == mCurrentCalender.get(Calendar.MONTH) && monthToDrawCalender.get(Calendar.YEAR) == mCurrentCalender.get(Calendar.YEAR)
        val todayDayOfMonth = mTodayCalender.get(Calendar.DAY_OF_MONTH)
        val isAnimatingWithExpose = mAnimationStatus == EXPOSE_CALENDAR_ANIMATION

        val maximumMonthDay = monthToDrawCalender.getActualMaximum(Calendar.DAY_OF_MONTH)
        mTempPreviousMonthCalendar.timeInMillis = monthToDrawCalender.timeInMillis
        mTempPreviousMonthCalendar.add(Calendar.MONTH, -1)
        val maximumPreviousMonthDay = mTempPreviousMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        //offset by one because we want to start from Monday
        val firstDayOfMonth = getDayOfWeek(monthToDrawCalender)
        val xBasePosition = mPaddingWidth.toFloat() + mPaddingLeft.toFloat() + mAccumulatedScrollOffset.x + offset.toFloat() - mPaddingRight
        val isAnimatingWithExposeOrIndicators = isAnimatingWithExpose || mAnimationStatus == ANIMATE_INDICATORS
        val yPosition = (dayRow * heightPerDay + mPaddingHeight).toFloat()
        val baseDay = (dayRow - 1) * 7 + 1 - firstDayOfMonth

        for (dayColumn in 0..6) {
            if (dayColumn == mDayColumnNames.size) {
                break
            }

            val xPosition = xBasePosition + (mWidthPerDay * dayColumn).toFloat()
            if (xPosition >= mGrowFactor && isAnimatingWithExposeOrIndicators || yPosition >= mGrowFactor) {
                break
            }

            val day = baseDay + dayColumn
            var defaultCalenderTextColorToUse = calenderTextColor
            if (mCurrentCalender.get(Calendar.DAY_OF_MONTH) == day && isSameMonthAsCurrentCalendar && !isAnimatingWithExpose) {
                drawDayCircleIndicator(mCurrentSelectedDayIndicatorStyle, canvas, xPosition, yPosition, currentSelectedDayBackgroundColor)
                defaultCalenderTextColorToUse = mCurrentSelectedDayTextColor
            }
            if (isSameYearAsToday && isSameMonthAsToday && todayDayOfMonth == day && !isAnimatingWithExpose) {
                // TODO calculate position of circle in a more reliable way
                drawDayCircleIndicator(mCurrentDayIndicatorStyle, canvas, xPosition, yPosition, currentDayBackgroundColor)
                defaultCalenderTextColorToUse = mCurrentDayTextColor
            }
            if (mDisplayOtherMonthDays) {
                dayPaint.style = Paint.Style.FILL
                dayPaint.color = mOtherMonthDaysTextColor
                if (day <= 0) {
                    // Display day month before
                    canvas.drawText((maximumPreviousMonthDay + day).toString(), xPosition, yPosition, dayPaint)
                } else if (day > maximumMonthDay) {
                    // Display day month after
                    canvas.drawText((day - maximumMonthDay).toString(), xPosition, yPosition, dayPaint)
                }
            }
            if (day in 1..maximumMonthDay) {
                dayPaint.style = Paint.Style.FILL
                dayPaint.color = defaultCalenderTextColorToUse
                canvas.drawText(day.toString(), xPosition, yPosition, dayPaint)
            }
        }
    }

    private fun drawWeekDays(canvas: Canvas, offset: Int) {
        if (!mShouldDrawDaysHeader) {
            return
        }
        if (mPaddingHeight.toFloat() >= mGrowFactor) {
            return
        }

        val isAnimating = (mAnimationStatus == EXPOSE_CALENDAR_ANIMATION || mAnimationStatus == ANIMATE_INDICATORS)
        val xBasePosition = mPaddingWidth.toFloat() + mPaddingLeft.toFloat() + mAccumulatedScrollOffset.x + offset.toFloat() - mPaddingRight

        dayPaint.color = calenderTextColor
        dayPaint.typeface = Typeface.DEFAULT_BOLD
        dayPaint.style = Paint.Style.FILL
        dayPaint.color = calenderTextColor

        for (dayColumn in 0..6) {
            val xPosition = xBasePosition + (mWidthPerDay * dayColumn).toFloat()
            if (!isAnimating || xPosition < mGrowFactor) {
                canvas.drawText(mDayColumnNames[dayColumn], xPosition, mPaddingHeight.toFloat(), dayPaint)
            }
        }

        dayPaint.typeface = Typeface.DEFAULT
    }

    private fun drawDayCircleIndicator(indicatorStyle: Int, canvas: Canvas, x: Float, y: Float, color: Int, circleScale: Float = 1f) {
        val strokeWidth = dayPaint.strokeWidth
        if (indicatorStyle == NO_FILL_LARGE_INDICATOR) {
            dayPaint.strokeWidth = 2 * mScreenDensity
            dayPaint.style = Paint.Style.STROKE
        } else {
            dayPaint.style = Paint.Style.FILL
        }
        drawCircle(canvas, x, y, color, circleScale)
        dayPaint.strokeWidth = strokeWidth
        dayPaint.style = Paint.Style.FILL
    }

    // Draw Circle on certain days to highlight them
    private fun drawCircle(canvas: Canvas, x: Float, y: Float, color: Int, circleScale: Float) {
        dayPaint.color = color
        if (mAnimationStatus == ANIMATE_INDICATORS) {
            val maxRadius = circleScale * dayIndicatorRadius * 1.4f
            drawCircle(canvas, if (growFactorIndicator > maxRadius) maxRadius else growFactorIndicator, x, y - mTextHeight / 6f)
        } else {
            drawCircle(canvas, circleScale * dayIndicatorRadius, x, y - mTextHeight / 6)
        }
    }

    private fun drawEventIndicatorCircle(canvas: Canvas, x: Float, y: Float, color: Int) {
        dayPaint.color = color
        when (mEventIndicatorStyle) {
            SMALL_INDICATOR -> {
                dayPaint.style = Paint.Style.FILL
                drawCircle(canvas, mSmallIndicatorRadius, x, y)
            }
            NO_FILL_LARGE_INDICATOR -> {
                dayPaint.style = Paint.Style.STROKE
                drawDayCircleIndicator(NO_FILL_LARGE_INDICATOR, canvas, x, y, color)
            }
            FILL_LARGE_INDICATOR -> drawDayCircleIndicator(FILL_LARGE_INDICATOR, canvas, x, y, color)
        }
    }

    private fun drawCircle(canvas: Canvas, radius: Float, x: Float, y: Float) {
        canvas.drawCircle(x, y, radius, dayPaint)
    }

    companion object {
        const val IDLE = 0
        const val EXPOSE_CALENDAR_ANIMATION = 1
        const val EXPAND_COLLAPSE_CALENDAR = 2
        const val ANIMATE_INDICATORS = 3
        private const val VELOCITY_UNIT_PIXELS_PER_SECOND = 1000
        private const val LAST_FLING_THRESHOLD_MILLIS = 300
        private const val DAYS_IN_WEEK = 7
        private const val SNAP_VELOCITY_DIP_PER_SECOND = 400f
        private const val ANIMATION_SCREEN_SET_DURATION_MILLIS = 700f
    }

}
