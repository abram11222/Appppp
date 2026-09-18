package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Deacon
import com.example.model.DeaconRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("خدمة الشمامسة", appName)
  }

  @Test
  fun `deacon missing data detection`() {
    val incomplete = Deacon(name = "", phone = "", street = "")
    assertTrue(incomplete.isDataIncomplete)
    assertTrue(incomplete.getMissingDataList().contains("الاسم مفقود"))

    val complete = Deacon(
      name = "كيرلس مينا",
      dateOfBirth = "2012-05-14",
      phone = "01234567890",
      street = "شارع الترعة",
      buildingNumber = "12",
      photoUrl = "https://example.com/photo.jpg"
    )
    assertTrue(complete.getMissingDataList().isEmpty())
  }

  @Test
  fun `deacon grades verification`() {
    val deacon = Deacon(name = "مينا جرجس", grade = com.example.model.DeaconGrade.PREP_1)
    assertEquals("أولى إعدادي", deacon.grade.arabicTitle)
    assertEquals(13, com.example.model.DeaconGrade.values().size)
  }

  @Test
  fun `excel row parser conversion test`() {
    val rows = listOf(
      com.example.util.ExcelRowData(name = "مينا سمير جرجس", phone = "01234567890", rankText = "إبصالتيس"),
      com.example.util.ExcelRowData(name = "كيرلس فايز", phone = "01000000000", rankText = "أغنسطس")
    )
    val converted = com.example.util.ExcelParserHelper.convertToDeacons(
      items = rows,
      targetGrade = com.example.model.DeaconGrade.PRIMARY_3,
      existingDeacons = emptyList()
    )
    assertEquals(2, converted.size)
    assertEquals("مينا سمير جرجس", converted[0].name)
    assertEquals(com.example.model.DeaconGrade.PRIMARY_3, converted[0].grade)
    assertEquals(DeaconRank.EPSALTOS, converted[0].rank)
    assertEquals("كنيستا العزب وأبي سيفين ودير الملاك بمير", converted[0].churchName)
    assertEquals("أسيوط", converted[0].governorate)
    assertEquals("مير", converted[0].area)
  }
}
