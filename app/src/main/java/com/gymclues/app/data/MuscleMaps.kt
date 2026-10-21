package com.gymclues.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class MuscleMapProfile(
  val primary: List<String>,
  val assisting: List<String>,
  val stabilizers: List<String>,
  val note: String,
  val kind: String,
  val source: String
) {
  val targetLabel get() = when (kind) {
    "mobility" -> "Mobility targets"
    "recovery" -> "Recovery areas"
    "activation" -> "Activation targets"
    else -> "Target muscles"
  }
}

@Serializable
data class MuscleMapVariant(val label: String, val profile: String, val image: String)

@Serializable
data class MuscleMapLibrary(
  val muscles: Map<String, String>,
  val profiles: Map<String, MuscleMapProfile>,
  val exercises: Map<String, List<MuscleMapVariant>>
) {
  // Exact normalized aliases only. An unknown exercise must never receive a
  // plausible-looking diagram for an unrelated movement.
  fun find(name: String): List<MuscleMapVariant> = exercises[normalize(name)].orEmpty()

  companion object {
    fun normalize(name: String) = name.lowercase(Locale.ROOT)
      .replace(Regex("[^a-z0-9]+"), "_").trim('_')

    @Volatile private var cached: MuscleMapLibrary? = null

    fun load(context: Context): MuscleMapLibrary = cached ?: synchronized(this) {
      cached ?: context.assets.open("muscle_maps.json").bufferedReader().use {
        val base = Json.decodeFromString<MuscleMapLibrary>(it.readText())
        val cardio = context.assets.open("cardio_muscle_maps.json").bufferedReader().use { reader ->
          Json.decodeFromString<MuscleMapLibrary>(reader.readText())
        }
        base.copy(muscles = base.muscles + cardio.muscles, profiles = base.profiles + cardio.profiles,
          exercises = base.exercises + cardio.exercises)
      }.also { cached = it }
    }
  }
}

/** A swap must show the replacement's anatomy, even if the prescription has an original name. */
fun workoutMuscleMapName(originalName: String?, currentName: String, isSwapped: Boolean): String =
  if (isSwapped) currentName else originalName?.takeIf { it.isNotBlank() } ?: currentName
