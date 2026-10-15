package com.gymcue.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gymcue.app.ui.*
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
    ShareCard("This week", "4 WORKOUTS", "22 sets, 12,300 lb lifted, 9-day streak", "c"))
  @Test fun cardIsRenderedAtFullSizeAndSavedAsPng() {
    cards.forEachIndexed { i, c ->
      val f = saveShareCard(ctx, c)
      val b = android.graphics.BitmapFactory.decodeFile(f.path)
      assertEquals(1080 to 1350, b.width to b.height)
      java.io.File(System.getProperty("shareOut") ?: return@forEachIndexed).let { d -> d.mkdirs(); f.copyTo(java.io.File(d, "card$i.png"), true) }
    }
  }
}
