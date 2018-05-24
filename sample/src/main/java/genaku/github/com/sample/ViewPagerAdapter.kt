package genaku.github.com.sample

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class ViewPagerAdapter(fm: FragmentManager, private var titles: Array<CharSequence>, private var numbOfTabs: Int) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment = if (position == 0) {
        CompactCalendarTab()
    } else {
        Tab2()
    }

    override fun getPageTitle(position: Int): CharSequence? = titles[position]

    override fun getCount(): Int = numbOfTabs
}