package genaku.github.com.sample

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var pager: ViewPager
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var tabs: SlidingTabLayout
    private val titles = arrayOf<CharSequence>("Home", "Events")
    private val numberOfTabs = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.tool_bar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = ViewPagerAdapter(supportFragmentManager, titles, numberOfTabs)

        // Assigning ViewPager View and setting the adapter
        pager = findViewById(R.id.pager)
        pager.adapter = adapter

        // Assiging the Sliding Tab Layout View
        tabs = findViewById(R.id.tabs)
        tabs.setDistributeEvenly(true) // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer { resources.getColor(R.color.black) }

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return if (item.itemId == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }
}
