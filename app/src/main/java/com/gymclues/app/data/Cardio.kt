package com.gymclues.app.data

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable data class CardioMetrics(
  val minutes: Double, val speed: Double? = null, val incline: Double? = null,
  val floors: Int? = null, val distance: Double? = null, val resistance: Double? = null,
  val distanceUnit: String = "mi", val speedUnit: String = "mph"
) {
  fun valid() = minutes.isFinite() && minutes > 0 &&
    listOf(speed, distance, resistance).all { it == null || (it.isFinite() && it >= 0) } &&
    (incline == null || (incline.isFinite() && incline in -100.0..100.0)) &&
    (floors == null || floors >= 0) && distanceUnit in listOf("mi", "km", "m") &&
    speedUnit in listOf("mph", "km/h", "steps/min")

  fun summary(): String = buildList {
    add("${cardioNumber(minutes)} min")
    distance?.let { add("${cardioNumber(it)} $distanceUnit") }
    speed?.let { add("${cardioNumber(it)} $speedUnit") }
    incline?.let { add("${cardioNumber(it)}% incline") }
    floors?.let { add("$it floors") }
    resistance?.let { add("resistance ${cardioNumber(it)}") }
  }.joinToString(" · ")
}

fun cardioNumber(value: Double) = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

/** Decimal minutes or m:ss; invalid values stay invalid instead of becoming zero. */
fun cardioMinutes(text: String): Double? {
  val parts = text.trim().split(':')
  if (parts.size == 1) return parts[0].toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
  if (parts.size != 2) return null
  val min = parts[0].toIntOrNull() ?: return null
  val sec = parts[1].toIntOrNull() ?: return null
  return (min + sec / 60.0).takeIf { min >= 0 && sec in 0..59 && it > 0 }
}

val Exercise.timedCardio get() = tracking in listOf("locomotion", "stairs", "bike", "rower")
