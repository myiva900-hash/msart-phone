package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.StorageBreakdown
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Yvan android", appName)
    }

    @Test
    fun `storage breakdown enforces 16GB partition`() {
        val storage = StorageBreakdown(
            totalMb = 16384f,
            systemOsMb = 2460f,
            appsMb = 1024f,
            filesAndMediaMb = 512f,
            cacheMb = 100f
        )
        assertEquals(16.0f, storage.totalGb, 0.01f)
        assertEquals(4096f, storage.usedMb, 0.01f)
        assertEquals(12288f, storage.freeMb, 0.01f)
    }
}
