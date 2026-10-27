package com.gymclues.app.ui

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.gymclues.app.R
import com.gymclues.app.data.SetLog
import java.io.File

/** Text drawn over the share background. [caption] is the plain text sent along for apps that use it (X). */
data class ShareCard(val title: String, val big: String, val sub: String, val caption: String,
                     val rows: List<Pair<String, String>> = emptyList())

/** "135 lb x 10", or "10 reps" for bodyweight. */
fun setLabel(s: SetLog): String {
  s.cardio?.let { return it.summary() }
  val w = if (s.weight % 1.0 == 0.0) s.weight.toLong().toString() else "%.1f".format(s.weight)
  return if (s.weight > 0) "$w ${s.units} x ${s.reps}" else "${s.reps} reps"
}

/** One line of sets for an exercise; identical sets in a row are merged, e.g. "135 lb x 10 (2 sets), 140 lb x 8". */
fun setsSummary(sets: List<SetLog>): String {
  val out = ArrayList<String>(); var i = 0
  while (i < sets.size) {
    val l = setLabel(sets[i]); var n = 1
    while (i + n < sets.size && setLabel(sets[i + n]) == l) n++
    out.add(if (n > 1) "$l ($n ${if (sets[i].cardio != null) "entries" else "sets"})" else l); i += n
  }
  return out.joinToString(", ")
}

private const val GREEN = 0xFF00FF41.toInt()

/** Draws [card] over res/drawable-nodpi/share_bg.jpg (1080x1350) and returns the bitmap. */
fun renderShareCard(ctx: Context, card: ShareCard): Bitmap {
  if (card.rows.isNotEmpty() && card.title == "Workout complete") return renderWorkoutCard(ctx, card)
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
  val list = card.rows.isNotEmpty()
  val k = w / 1080f
  // Brand row: logo + wordmark
  val logo = if (list) 110f * k else 150f * k; val top = if (list) h * .05f else h * .09f
  runCatching { BitmapFactory.decodeResource(ctx.resources, R.drawable.gymclues_logo) }.getOrNull()?.let {
    c.drawBitmap(it, null, RectF(w / 2 - logo / 2, top, w / 2 + logo / 2, top + logo), Paint(Paint.FILTER_BITMAP_FLAG)) }
  draw("GYMCLUES", paint(if (list) 50f else 64f, Color.WHITE), top + logo + 14f * k, 1)
  var y = draw(card.title.uppercase(), paint(if (list) 44f else 54f, GREEN), if (list) h * .25f else h * .36f, 2)
  // Shrink long headline text until it fits on one line
  var size = if (list) 110f else 230f; val big = paint(size, GREEN)
  while (big.measureText(card.big) > w * .84f && size > 60f) { size -= 6f; big.textSize = size * k }
  y = draw(card.big, big, y + 12f * k, 1)
  if (list) drawRows(c, card.rows, w, h, y + 30f * k)
  else draw(card.sub, paint(52f, Color.WHITE), y + 30f * k, 4)
  return bmp
}

private fun renderWorkoutCard(ctx: Context, card: ShareCard): Bitmap {
  val width = 1080
  val columns = if (card.rows.size > 6) 2 else 1
  val gap = 20
  val cardWidth = (900 - gap * (columns - 1)) / columns
  fun ink(size: Float, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.WHITE; textSize = size; typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
  }
  fun layout(value: String, size: Float, bold: Boolean, available: Int, alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL) =
    StaticLayout.Builder.obtain(value, 0, value.length, ink(size, bold), available).setAlignment(alignment).build()
  val title = layout(card.big, 55f, true, 900, Layout.Alignment.ALIGN_CENTER)
  val date = layout(card.sub, 34f, false, 900, Layout.Alignment.ALIGN_CENTER)
  val blocks = card.rows.map { (name, details) ->
    val available = cardWidth - 44
    val heading = layout(name, if (columns == 2) 36f else 44f, true, available)
    val entries = layout(details, if (columns == 2) 29f else 35f, false, available)
    Triple(heading, entries, heading.height + entries.height + 58)
  }
  val rowHeights = blocks.chunked(columns).map { row -> row.maxOf { it.third } }
  val start = 235 + title.height + date.height + 36
  val height = maxOf(1350, start + rowHeights.sum() + gap * rowHeights.size + 90)
  val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(out)
  val background = BitmapFactory.decodeResource(ctx.resources, R.drawable.share_bg)
  canvas.drawBitmap(background, null, Rect(0, 0, width, height), Paint(Paint.FILTER_BITMAP_FLAG))
  val logo = BitmapFactory.decodeResource(ctx.resources, R.drawable.gymclues_logo)
  canvas.drawBitmap(logo, null, Rect(480, 28, 600, 148), Paint(Paint.FILTER_BITMAP_FLAG))
  val brand = ink(48f, true).apply { textAlign = Paint.Align.CENTER }
  canvas.drawText("GYMCLUES", 540f, 206f, brand)
  canvas.save(); canvas.translate(90f, 225f); title.draw(canvas); canvas.restore()
  canvas.save(); canvas.translate(90f, (235 + title.height).toFloat()); date.draw(canvas); canvas.restore()
  val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GREEN; style = Paint.Style.STROKE; strokeWidth = 4f }
  var y = start.toFloat()
  blocks.chunked(columns).forEachIndexed { index, row ->
    row.forEachIndexed { column, (heading, entries, blockHeight) ->
      val x = (90 + column * (cardWidth + gap)).toFloat()
      canvas.drawRoundRect(x, y, x + cardWidth, y + blockHeight, 22f, 22f, border)
      canvas.save(); canvas.translate(x + 22f, y + 22f); heading.draw(canvas)
      canvas.translate(0f, heading.height + 12f); entries.draw(canvas); canvas.restore()
    }
    y += rowHeights[index] + gap
  }
  return out
}

/** Saves the card as PNG in cache/share and returns the file (exposed through FileProvider, see res/xml/file_paths.xml). */
fun saveShareCard(ctx: Context, card: ShareCard): File {
  val f = File(File(ctx.cacheDir, "share").apply { mkdirs() }, "gymclues_share.png")
  f.outputStream().use { renderShareCard(ctx, card).compress(Bitmap.CompressFormat.PNG, 100, it) }
  return f
}

/** Exercise name over its sets, left aligned. The type shrinks to fit; rows that still do not fit become "+N more exercises". */
private fun drawRows(c: Canvas, rows: List<Pair<String, String>>, w: Float, h: Float, top: Float) {
  val k = w / 1080f; val bottom = h * .94f; val width = (w * .84f).toInt()
  fun layouts(size: Float, list: List<Pair<String, String>>) = list.map { (n, d) ->
    val np = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size * k; color = Color.WHITE; typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD) }
    val dp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size * .86f * k; color = GREEN; typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL) }
    StaticLayout.Builder.obtain(n, 0, n.length, np, width).setMaxLines(1).setEllipsize(android.text.TextUtils.TruncateAt.END).build() to
      (if (d.isEmpty()) null else StaticLayout.Builder.obtain(d, 0, d.length, dp, width).setMaxLines(3).setEllipsize(android.text.TextUtils.TruncateAt.END).build()) }
  fun height(ls: List<Pair<StaticLayout, StaticLayout?>>, gap: Float) = ls.sumOf { (a, b) -> (a.height + (b?.height ?: 0) + gap).toDouble() }.toFloat()
  for (keep in rows.size downTo 1) {
    val shown = rows.take(keep) + (if (keep < rows.size) listOf("+${rows.size - keep} more exercises" to "") else emptyList())
    for (size in 60 downTo 36 step 2) {
      val ls = layouts(size.toFloat(), shown); val gap = size * .6f * k
      if (height(ls, gap) <= bottom - top || (keep == 1 && size == 36)) { var y = top
        ls.forEach { (a, b) -> c.save(); c.translate(w * .08f, y); a.draw(c); c.translate(0f, a.height.toFloat()); b?.draw(c); c.restore(); y += a.height + (b?.height ?: 0) + gap }
        return }
    }
  }
}
