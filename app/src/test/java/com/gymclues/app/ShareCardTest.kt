package com.gymclues.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.ui.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class) @GraphicsMode(GraphicsMode.Mode.NATIVE) @Config(sdk = [34])
class ShareCardTest {
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private val cards = listOf(
    ShareCard("Program milestone", "25%", "Muscle & Strength M-F Body Part Split", "c"),
    ShareCard("New program", "Started", "Push Pull Legs, 6 days a week, the very long program name that wraps", "c"),
    ShareCard("9-day streak", "12,300 lb", "moved in 22 sets", "c"),
    ShareCard("Workout complete", "Back day", "", "c", listOf("Lat Pulldown" to "", "Seated Cable Row" to "",
      "Barbell Row" to "", "Face Pull" to "", "Bicep Curl" to "")),
    ShareCard("Workout complete", "Full body, week four of twelve", "", "c", (1..14).map { "Exercise number $it" to "" }))
  @Test fun cardIsRenderedAtFullSizeAndSavedAsPng() {
    cards.forEachIndexed { i, c ->
      val f = saveShareCard(ctx, c)
      val b = android.graphics.BitmapFactory.decodeFile(f.path)
      assertEquals(1080 to 1350, b.width to b.height)
      java.io.File(System.getProperty("shareOut") ?: return@forEachIndexed).let { d -> d.mkdirs(); f.copyTo(java.io.File(d, "card$i.png"), true) }
    }
  }
}
