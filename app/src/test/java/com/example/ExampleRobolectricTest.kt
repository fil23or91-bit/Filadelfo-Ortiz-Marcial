package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EduFacial Pro", appName)
  }

  @Test
  fun `test whatsapp absence message generation`() {
    val message = com.example.service.WhatsAppHelper.buildAbsenceMessage(
      studentName = "Alejandro Morales",
      controlNumber = "21040101",
      semester = 4,
      groupName = "A",
      date = "15/10/2026"
    )
    org.junit.Assert.assertTrue(message.contains("21040101"))
    org.junit.Assert.assertTrue(message.contains("Alejandro Morales"))
  }
}
