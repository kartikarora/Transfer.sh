package me.kartikarora.transfersh

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.kartikarora.transfersh.activities.TransferActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransferActivityTest {
    @Test
    fun testLaunchOfTransferActivity() {
        val scenario = launchActivity<TransferActivity>()
        assertEquals(scenario.state, Lifecycle.State.RESUMED)
    }

    @Test
    fun testListToGrid() {
        val scenario = launchActivity<TransferActivity>()
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withContentDescription(R.string.view_grid)).perform(click())
        scenario.onActivity { activity ->
            assertEquals(activity.columnCount, 3)
        }
    }

    @Test
    fun testGridToList() {
        val scenario = launchActivity<TransferActivity>()
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withId(R.id.action_view_grid)).perform(click())
        scenario.onActivity { activity ->
            assertEquals(activity.columnCount, 1)
        }
    }
}