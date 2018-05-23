package sundeepk.github.com.sample

import android.app.Activity
import android.graphics.Color
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.*
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.UiThreadTestRule
import android.test.ActivityInstrumentationTestCase2
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarAnimationListener
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.github.sundeepk.compactcalendarview.CompactCalendarView.Companion.FILL_LARGE_INDICATOR
import com.github.sundeepk.compactcalendarview.CompactCalendarView.Companion.NO_FILL_LARGE_INDICATOR
import com.github.sundeepk.compactcalendarview.CompactCalendarView.Companion.SMALL_INDICATOR
import com.github.sundeepk.compactcalendarview.domain.Event
import junit.framework.Assert
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import java.text.SimpleDateFormat
import java.util.*

class ApplicationTest : ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {
    @Rule
    var uiThreadTestRule = UiThreadTestRule()

    private var dateFormatForMonth: SimpleDateFormat? = null
    private var compactCalendarView: CompactCalendarView? = null
    private var activity: MainActivity? = null
    private var mainContent: View? = null
    private var onClosedCallCount = 0
    private var onOpenedCallCount = 0

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        instrumentation.waitForIdleSync()
        Locale.setDefault(Locale.ENGLISH)
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
        dateFormatForMonth = SimpleDateFormat("MMM - yyyy", Locale.getDefault())
        injectInstrumentation(InstrumentationRegistry.getInstrumentation())
        activity = getActivity()
        compactCalendarView = activity!!.findViewById(R.id.compactcalendar_view)
        mainContent = activity!!.findViewById(R.id.parent)
        onClosedCallCount = 0
        onOpenedCallCount = 0
    }

    @Test
    fun testItDoesNotScrollWhenScrollingIsDisabled() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)
        compactCalendarView!!.shouldScrollMonth(false)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        scrollCalendarForwardBy(1)

        verifyNoMoreInteractions(listener)
        capture("testItDoesNotScrollWhenScrollingIsDisabled")
    }

    @Test
    fun testItDoesNotSelectFirstDayWhenItsDisableOnNextMonth() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))

        shouldSelectFirstDayOfMonthOnScroll(false)
        onView(withId(R.id.next_button)).perform(clickXY(0f, 0f))

        verify(listener).onMonthScroll(Date(1425168000000L))

        syncToolbarDate()
        capture("testItDoesNotSelectFirstDayWhenItsDisableOnNextMonth")
    }

    @Test
    fun testItDoesNotSelectFirstDayWhenItsDisableOnPreviousMonth() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))

        shouldSelectFirstDayOfMonthOnScroll(false)
        onView(withId(R.id.prev_button)).perform(clickXY(0f, 0f))

        verify(listener).onMonthScroll(Date(1420070400000L))

        syncToolbarDate()
        capture("testItDoesNotSelectFirstDayWhenItsDisableOnPreviousMonth")
    }

    @Test
    fun testItDoesSelectFirstDayWhenItsDisableOnNextMonth() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))

        shouldSelectFirstDayOfMonthOnScroll(true)
        onView(withId(R.id.next_button)).perform(clickXY(0f, 0f))

        verify(listener).onMonthScroll(Date(1425168000000L))

        syncToolbarDate()
        capture("testItDoesSelectFirstDayWhenItsDisableOnNextMonth")
    }

    @Test
    fun testItDoesSelectFirstDayWhenItsDisableOnPreviousMonth() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))

        shouldSelectFirstDayOfMonthOnScroll(true)
        onView(withId(R.id.prev_button)).perform(clickXY(0f, 0f))

        verify(listener).onMonthScroll(Date(1420070400000L))

        syncToolbarDate()
        capture("testItDoesSelectFirstDayWhenItsDisableOnPreviousMonth")
    }

    @Test
    fun testCorrectDateIsReturnedWhenShouldSelectFirstDayOfMonthOnScrollIsFalse() {
        compactCalendarView!!.shouldSelectFirstDayOfMonthOnScroll(false)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))

        scrollCalendarForwardBy(4)
        //Mon, 01 Jun 2015 00:00:00 GMT
        Assert.assertEquals(Date(1433116800000L), compactCalendarView!!.firstDayOfCurrentMonth)

        //Wed, 01 Apr 2015 00:00:00 GMT
        scrollCalendarBackwardsBy(2)
        Assert.assertEquals(Date(1427846400000L), compactCalendarView!!.firstDayOfCurrentMonth)

        //Tue, 01 Apr 2014 00:00:00 GMT
        scrollCalendarBackwardsBy(12)
        Assert.assertEquals(Date(1396310400000L), compactCalendarView!!.firstDayOfCurrentMonth)
    }

    @Test
    fun testItDoesNotDrawSelectedDayOnDifferentYearsWhenShouldSelectFirstDayOfMonthOnScrollIsFalse() {
        compactCalendarView!!.shouldSelectFirstDayOfMonthOnScroll(false)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))

        //01 Feb 2016 00:00:00 GMT
        scrollCalendarForwardBy(12)
        Assert.assertEquals(Date(1454284800000L), compactCalendarView!!.firstDayOfCurrentMonth)
        capture("testItDoesNotDrawSelectedDayOnDifferentYearsWhenShouldSelectFirstDayOfMonthOnScrollIsFalse")
    }

    @Test
    fun testWhenShouldSelectFirstDayOfMonthOnScrollIsFalseItDoesNotSelectFIrstDayOfMonth() {
        compactCalendarView!!.shouldSelectFirstDayOfMonthOnScroll(false)
        setDate(Date(1423353600000L))
        scrollCalendarForwardBy(1)
        capture("testWhenShouldSelectFirstDayOfMonthOnScrollIsFalseItDoesNotSelectFIrstDayOfMonth")
    }

    @Test
    fun testOnMonthScrollListenerIsCalled() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        onView(withId(R.id.compactcalendar_view)).perform(scroll(100, 100, -100, 0))

        //Sun, 01 Mar 2015 00:00:00 GMT - expected
        verify(listener).onMonthScroll(Date(1425168000000L))
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun testToolbarIsUpdatedOnScroll() {
        instrumentation.waitForIdleSync()
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        onView(withId(R.id.compactcalendar_view)).perform(scroll(100, 100, -100, 0))

        onView(allOf(instanceOf<Any>(TextView::class.java), withParent(withId(R.id.tool_bar))))
                .check(matches(withText("Mar - 2015")))
        capture("testToolbarIsUpdatedOnScroll")
    }

    @Test
    fun testItDrawNoFillLargeIndicatorOnCurrentSelectedDayWithSmallIndicatorForEvents() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        addEvents(Calendar.FEBRUARY, 2015)
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 150f))
        setIndicatorType(NO_FILL_LARGE_INDICATOR, SMALL_INDICATOR, FILL_LARGE_INDICATOR)
        capture("testItDrawNoFillLargeIndicatorOnCurrentSelectedDayWithSmallIndicatorForEvents")
    }

    @Test
    fun testItDrawNoFillLargeIndicatorOnCurrentSelectedDayWithNoFillLargeIndicatorForEvents() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        addEvents(Calendar.FEBRUARY, 2015)
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 150f))
        setIndicatorType(NO_FILL_LARGE_INDICATOR, NO_FILL_LARGE_INDICATOR, FILL_LARGE_INDICATOR)
        capture("testItDrawNoFillLargeIndicatorOnCurrentSelectedDayWithNoFillLargeIndicatorForEvents")
    }

    @Test
    fun testItDrawFillLargeIndicatorOnCurrentSelectedDayWithSmallIndicatorForEvents() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        addEvents(Calendar.FEBRUARY, 2015)
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 150f))
        setIndicatorType(FILL_LARGE_INDICATOR, SMALL_INDICATOR, FILL_LARGE_INDICATOR)
        capture("testItDrawFillLargeIndicatorOnCurrentSelectedDayWithSmallIndicatorForEvents")
    }

    @Test
    fun testItDrawFillLargeIndicatorOnCurrentSelectedDayWithFillLargeIndicatorForEvents() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        addEvents(Calendar.FEBRUARY, 2015)
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 150f))
        setIndicatorType(FILL_LARGE_INDICATOR, FILL_LARGE_INDICATOR, FILL_LARGE_INDICATOR)
        capture("testItDrawFillLargeIndicatorOnCurrentSelectedDayWithFillLargeIndicatorForEvents")
    }

    @Test
    fun testItAddsAndRemovesEventsForFilledLargeIndicator() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 150f))
        setIndicatorType(FILL_LARGE_INDICATOR, FILL_LARGE_INDICATOR, FILL_LARGE_INDICATOR)
        val events = getEvents(1423353600000L, 1)

        compactCalendarView!!.addEvents(events)
        compactCalendarView!!.removeEvent(events[0])
    }

    @Test
    fun testOnDayClickListenerIsCalled() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 100f))

        //Tue, 03 Feb 2015 00:00:00 GMT - expected
        verify(listener).onDayClick(Date(1422921600000L))
        verifyNoMoreInteractions(listener)
        capture("testOnDayClickListenerIsCalled")
    }

    @Test
    fun testItDrawsEventIndicatorsBelowHighlightedDayIndicators() {
        setDrawEventsBelowDayIndicators(true)
        setDate(Date(1423094400000L))
        addEvents(Calendar.FEBRUARY, 2015)
        capture("testItDrawsEventIndicatorsBelowHighlightedDayIndicators")
    }

    @Test
    fun testItDrawsFillLargeIndicatorForEventsWhenDrawEventsBelowDayIndicatorsIsTrue() {
        // test to make sure calendar does not draw event indicators below highlighted days
        // when the style is FILL_LARGE_INDICATOR
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDrawEventsBelowDayIndicators(true)
        setDate(Date(1423353600000L))
        addEvents(Calendar.FEBRUARY, 2015)
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 150f))
        setIndicatorType(FILL_LARGE_INDICATOR, FILL_LARGE_INDICATOR, FILL_LARGE_INDICATOR)
        capture("testItDrawsFillLargeIndicatorForEventsWhenDrawEventsBelowDayIndicatorsIsTrue")
    }

    @Test
    fun testItDrawsIndicatorsBelowCurrentSelectedDayWithLargeHeight() {
        // test to make sure calendar does not draw event indicators below highlighted days
        //Sun, 08 Feb 2015 00:00:00 GMT
        setHeight(400f)
        setDrawEventsBelowDayIndicators(true)
        setDate(Date(1423353600000L))
        addEvents(Calendar.FEBRUARY, 2015)
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 120f))
        capture("testItDrawsIndicatorsBelowCurrentSelectedDayWithLargeHeight")
    }

    @Test
    fun testItDisplaysDaysFromOtherMonthsForFeb() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setShouldDrawDaysFromOtherMonths(true)
        capture("testItDisplaysDaysFromOtherMonthsForFeb")
    }

    @Test
    fun testItDisplaysDaysFromOtherMonthsForAfterScrollingFromFebToMarch() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setShouldDrawDaysFromOtherMonths(true)
        scrollCalendarForwardBy(1)
        capture("testItDisplaysDaysFromOtherMonthsForAfterScrollingFromFebToMarch")
    }

    @Test
    fun testItDisplaysDaysFromOtherMonthsForAfterScrollingFromFebToJan() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setShouldDrawDaysFromOtherMonths(true)
        setDate(Date(1423353600000L))
        instrumentation.waitForIdleSync()
        scrollCalendarBackwardsBy(1)
        capture("testItDisplaysDaysFromOtherMonthsForAfterScrollingFromFebToJan")
    }

    @Test
    fun testItDrawsSundayAsFirstDayOfMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.SUNDAY)
        capture("testItDrawsSundayAsFirstDayOfMonth")
    }

    @Test
    fun testItDrawsMondayAsFirstDayOfMonth() {
        // defaults to Monday
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        capture("testItDrawsMondayAsFirstDayOfMonth")
    }

    @Test
    fun testItDrawsTuesdayAsFirstDayOfMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.TUESDAY)
        capture("testItDrawsTuesdayAsFirstDayOfMonth")
    }

    @Test
    fun testItDrawsWednesdayAsFirstDayOfMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.WEDNESDAY)
        capture("testItDrawsWednesdayAsFirstDayOfMonth")
    }

    @Test
    fun testItDrawsThursdayAsFirstDayOfMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.THURSDAY)
        capture("testItDrawsThursdayAsFirstDayOfMonth")
    }

    @Test
    fun testItDrawsFridayAsFirstDayOfMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.FRIDAY)
        capture("testItDrawsFridayAsFirstDayOfMonth")
    }

    @Test
    fun testItDrawsSaturdayAsFirstDayOfMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.SATURDAY)
        capture("testItDrawsSaturdayAsFirstDayOfMonth")
    }

    @Test
    fun testItDrawsWedAsFirstDayWithFrenchLocale() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.WEDNESDAY)
        onView(withId(R.id.set_locale)).perform(clickXY(0f, 0f))
        setUseThreeLetterAbbreviation(true)
        capture("testItDrawsWednesdayAsFirstDayWithFrenchLocale")
    }

    @Test
    fun testOnDayClickListenerIsCalledWhenLocaleIsFranceWithWedAsFirstDayOFWeek() {
        val listener = mock(CompactCalendarViewListener::class.java)
        compactCalendarView!!.setListener(listener)

        val locale = Locale.FRANCE
        val timeZone = TimeZone.getTimeZone("Europe/Paris")
        val instance = Calendar.getInstance(timeZone, locale)
        // Thu, 05 Feb 2015 12:00:00 GMT - then set to midnight
        instance.timeInMillis = 1423137600000L
        instance.set(Calendar.HOUR_OF_DAY, 0)
        instance.set(Calendar.MINUTE, 0)
        instance.set(Calendar.SECOND, 0)
        instance.set(Calendar.MILLISECOND, 0)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        setFirstDayOfWeek(Calendar.WEDNESDAY)
        onView(withId(R.id.set_locale)).perform(clickXY(0f, 0f))
        onView(withId(R.id.compactcalendar_view)).perform(clickXY(60f, 100f))

        //Thr, 05 Feb 2015 00:00:00 GMT - expected
        verify(listener).onDayClick(instance.time)
        verifyNoMoreInteractions(listener)
        capture("testOnDayClickListenerIsCalledWhenLocaleIsFranceWithWedAsFirstDayOFWeek")
    }

    // Using mocks for listener causes espresso to throw an error because the callback is called from within animation handler.
    // Maybe a problem with espresso, for now manually check count.
    @Test
    @Throws(Throwable::class)
    fun testOpenedAndClosedListerCalledForExposeAnimationCalendar() {
        // calendar is opened by default.
        val listener = object : CompactCalendarAnimationListener {
            override fun onOpened() {
                onOpenedCallCount++
            }

            override fun onClosed() {
                onClosedCallCount++
            }
        }
        compactCalendarView!!.setAnimationListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        onView(withId(R.id.show_with_animation_calendar)).perform(click())
        onView(withId(R.id.show_with_animation_calendar)).perform(click())

        waitForAnimationFinish()

        Assert.assertEquals(onClosedCallCount, 1)
        Assert.assertEquals(onOpenedCallCount, 1)
    }

    // Using mocks for listener causes espresso to throw an error because the callback is called from within animation handler.
    // Maybe a problem with espresso, for now manually check count.
    @Test
    @Throws(Throwable::class)
    fun testOpenedAndClosedListerCalledForCalendar() {
        // calendar is opened by default.
        val listener = object : CompactCalendarAnimationListener {
            override fun onOpened() {
                onOpenedCallCount++
            }

            override fun onClosed() {
                onClosedCallCount++
            }
        }
        compactCalendarView!!.setAnimationListener(listener)

        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        onView(withId(R.id.slide_calendar)).perform(click())
        onView(withId(R.id.slide_calendar)).perform(click())

        waitForAnimationFinish()

        Assert.assertEquals(onClosedCallCount, 1)
        Assert.assertEquals(onOpenedCallCount, 1)
    }

    private fun waitForAnimationFinish() {
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return ""
            }

            override fun checkCondition(): Boolean {
                return !compactCalendarView!!.isAnimating
            }
        })
    }

    @Test
    fun testItDoesNotThrowNullPointerWhenNoAnimationListenerIsSet() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        compactCalendarView?.setAnimationListener(null)
        setDate(Date(1423353600000L))
        onView(withId(R.id.show_with_animation_calendar)).perform(click())
        onView(withId(R.id.slide_calendar)).perform(click())
    }

    @Test
    fun testItDrawsDifferentColorsForCurrentSelectedDay() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        setDate(Date(1423353600000L))
        compactCalendarView!!.setCurrentDayTextColor(Color.BLACK)
        compactCalendarView!!.setCurrentSelectedDayTextColor(Color.BLUE)
        capture("testItDrawsDifferentColorsForCurrentSelectedDay")
    }

    // Nasty hack to get the toolbar to update the current month
    // TODO sample code should be refactored to do this
    private fun syncToolbarDate() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val toolbar = activity!!.supportActionBar
            toolbar!!.title = dateFormatForMonth!!.format(compactCalendarView!!.firstDayOfCurrentMonth)
        }
    }

    private fun setFirstDayOfWeek(dayOfWeek: Int) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { compactCalendarView!!.setFirstDayOfWeek(dayOfWeek) }
    }

    private fun setUseThreeLetterAbbreviation(useThreeLetterAbbreviation: Boolean) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { compactCalendarView!!.setUseThreeLetterAbbreviation(useThreeLetterAbbreviation) }
    }

    private fun setShouldDrawDaysFromOtherMonths(shouldDrawEventsBelowDayIndicators: Boolean) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { compactCalendarView!!.displayOtherMonthDays(shouldDrawEventsBelowDayIndicators) }
    }

    private fun setDrawEventsBelowDayIndicators(shouldDrawEventsBelowDayIndicators: Boolean) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { compactCalendarView!!.shouldDrawIndicatorsBelowSelectedDays(shouldDrawEventsBelowDayIndicators) }
    }

    private fun setIndicatorType(currentSelectedDayStyle: Int, eventStyle: Int, currentDayStyle: Int) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compactCalendarView!!.setCurrentSelectedDayIndicatorStyle(currentSelectedDayStyle)
            compactCalendarView!!.setEventIndicatorStyle(eventStyle)
            compactCalendarView!!.setCurrentDayIndicatorStyle(currentDayStyle)
        }
    }

    private fun capture(name: String) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compactCalendarView!!.requestLayout()
            ViewHelpers.setupView(mainContent)
                    .setExactHeightPx(mainContent!!.height)
                    .setExactWidthPx(mainContent!!.width)
                    .layout()
            safeSleep(200)
            Screenshot.snap(mainContent)
                    .setName(name)
                    .record()
        }
    }

    private fun setDate(date: Date) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compactCalendarView!!.setCurrentDate(date)
            val toolbar = activity!!.supportActionBar
            toolbar!!.title = dateFormatForMonth!!.format(compactCalendarView!!.firstDayOfCurrentMonth)
        }
    }

    private fun shouldSelectFirstDayOfMonthOnScroll(shouldSelectFirstDay: Boolean) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compactCalendarView!!.shouldSelectFirstDayOfMonthOnScroll(shouldSelectFirstDay)
            val toolbar = activity!!.supportActionBar
            toolbar!!.title = dateFormatForMonth!!.format(compactCalendarView!!.firstDayOfCurrentMonth)
        }
    }

    private fun clickXY(x: Float, y: Float): ViewAction {
        val dm = activity!!.resources.displayMetrics
        val spX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, x, dm)
        val spY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, y, dm)
        return GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view.getLocationOnScreen(screenPos)

                    val screenX = screenPos[0] + spX
                    val screenY = screenPos[1] + spY

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER)
    }

    private fun scroll(startX: Int, startY: Int, endX: Int, endY: Int): ViewAction {
        val dm = activity!!.resources.displayMetrics
        val spStartX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, startX.toFloat(), dm)
        val spStartY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, startY.toFloat(), dm)
        val spEndX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, endX.toFloat(), dm)
        val spEndY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, endY.toFloat(), dm)
        return GeneralSwipeAction(
                Swipe.FAST,
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view.getLocationOnScreen(screenPos)

                    val screenX = screenPos[0] + spStartX
                    val screenY = screenPos[1] + spStartY

                    floatArrayOf(screenX, screenY)
                },
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view.getLocationOnScreen(screenPos)

                    val screenX = screenPos[0] + spEndX
                    val screenY = screenPos[1] + spEndY

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER)
    }

    private fun addEvents(month: Int, year: Int) {
        val context = compactCalendarView!!.context
        (context as Activity).runOnUiThread {
            val currentCalender = Calendar.getInstance()
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
    }

    private fun getEvents(timeInMillis: Long, day: Int): List<Event> {
        return when {
            day < 2 -> Arrays.asList(Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)))
            day in 3..4 -> Arrays.asList(
                    Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + Date(timeInMillis)),
                    Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + Date(timeInMillis)))
            else -> Arrays.asList(
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

    private fun setHeight(height: Float) {
        val context = compactCalendarView!!.context
        (context as Activity).runOnUiThread {
            val newHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, context.getResources().displayMetrics).toInt()
            compactCalendarView!!.layoutParams.height = newHeight
            compactCalendarView!!.setTargetHeight(newHeight)
            compactCalendarView!!.requestLayout()
            compactCalendarView!!.invalidate()
        }
    }

    private fun scrollCalendarForwardBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(scroll(100, 100, -200, 0))
            safeSleep()
        }
    }

    private fun scrollCalendarBackwardsBy(months: Int) {
        for (i in 0 until months) {
            onView(withId(R.id.compactcalendar_view)).perform(scroll(100, 10, 300, 0))
            safeSleep()
        }
    }

    private fun safeSleep(i: Int = 500) {
        try {
            Thread.sleep(i.toLong())
        } catch (e: InterruptedException) {
            Log.e(APPLICATION_TEST_TAG, "Error occurred while sleeping.", e)
        }

    }

    companion object {
        private const val APPLICATION_TEST_TAG = "ApplicationTest"
    }
}