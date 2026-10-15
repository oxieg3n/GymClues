package com.gymcue.app.ui

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.gymcue.app.R
import java.io.File

/** Text drawn over the share background. [caption] is the plain text sent along for apps that use it (X). */
data class ShareCard(val title: String, val big: String, val sub: String, val caption: String)

private const val GREEN = 0xFF00FF41.toInt()

/** Draws [card] over res/drawable-nodpi/share_bg.jpg (1080x1350) and returns the bitmap. */
fun renderShareCard(ctx: Context, card: ShareCard): Bitmap {
  val bg = BitmapFactory.decodeResource(ctx.resources, R.drawable.share_bg)
  val bmp = bg.copy(Bitmap.Config.ARGB_8888, true)
  val c = Canvas(bmp); val w = bmp.width.toFloat(); val h = bmp.height.toFloat()
  fun paint(size: Float, color: Int, bold: Boolean = true) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = size * w / 1080f; this.color = color
    typeface = Typeface.create("sans-serif-condensed", if (bold) Typeface.BOLD else Typeface.NORMAL) }
  fun draw(text: String, p: TextPaint, y: Float, maxLines: Int = 2): Float {
    val l = StaticLayout.Builder.obtain(text, 0, text.length, p, (w * .84f).toInt()).setAlignment(Layout.Alignment.ALIGN_CENTER)
      .setMaxLines(maxLines).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
    c.save(); c.translate(w * .08f, y); l.draw(c); c.restore(); return y + l.height }
  // Brand row: logo + wordmark
  runCatching { BitmapFactory.decodeResource(ctx.resources, R.drawable.gymcue_logo) }.getOrNull()?.let {
    val s = 150f * w / 1080f; c.drawBitmap(it, null, RectF(w / 2 - s / 2, h * .09f, w / 2 + s / 2, h * .09f + s), Paint(Paint.FILTER_BITMAP_FLAG)) }
  draw("GYMCUE", paint(64f, Color.WHITE), h * .09f + 170f * w / 1080f, 1)
  var y = draw(card.title.uppercase(), paint(54f, GREEN), h * .36f, 2)
  // Shrink long headline numbers until they fit on one line
  var size = 230f; val big = paint(size, GREEN)
  while (big.measureText(card.big) > w * .84f && size > 80f) { size -= 10f; big.textSize = size * w / 1080f }
  y = draw(card.big, big, y + 20f * w / 1080f, 1)
  draw(card.sub, paint(52f, Color.WHITE, false), y + 30f * w / 1080f, 4)
  return bmp
}

/** Saves the card as PNG in cache/share and returns the file (exposed through FileProvider, see res/xml/file_paths.xml). */
fun saveShareCard(ctx: Context, card: ShareCard): File {
  val f = File(File(ctx.cacheDir, "share").apply { mkdirs() }, "gymcue_share.png")
  f.outputStream().use { renderShareCard(ctx, card).compress(Bitmap.CompressFormat.PNG, 100, it) }
  return f
}
