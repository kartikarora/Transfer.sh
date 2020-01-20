package me.kartikarora.transfersh

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.kartikarora.transfersh.models.TransferActivityModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BaseUnitTest {

    lateinit var context: Context
    lateinit var model: TransferActivityModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        model = TransferActivityModel()
        model.injectContext(context)
    }

    @Test
    fun testPreferences() {
        model.storeBooleanInPreference("boolean_test", true)
        assertEquals(model.getBooleanFromPreference("boolean_test"), true)
    }
}