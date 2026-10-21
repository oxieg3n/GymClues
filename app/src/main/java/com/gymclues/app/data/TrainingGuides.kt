package com.gymclues.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class SetupGuideStep(val title: String, val text: String)
@Serializable data class EquipmentSetupGuide(
  val name: String, val image: String, val steps: List<SetupGuideStep>, val caution: String
)
@Serializable data class ExerciseSafetyProfile(
  val safetyTips: List<String>, val injuryPrevention: List<String>, val mistakes: List<String>
)
@Serializable data class TrainingGuideVariant(val label: String, val profile: String, val equipmentIds: List<String>)
@Serializable data class TrainingGuideSource(val title: String, val url: String)
@Serializable data class TrainingGuideLibrary(
  val equipment: Map<String, EquipmentSetupGuide>,
  val profiles: Map<String, ExerciseSafetyProfile>,
  val exercises: Map<String, List<TrainingGuideVariant>>,
  val exerciseSetups: Map<String, List<EquipmentSetup>>,
  val sources: List<TrainingGuideSource>,
  val equipmentCheck: String,
  val stopRule: String
) {
  fun variants(name: String) = exercises[MuscleMapLibrary.normalize(name)].orEmpty()
  fun safetyFor(name: String) = profiles.getValue(variants(name).firstOrNull()?.profile ?: "general")

  /** The workbook catalog keeps its IDs, instructions, alternatives and authored safety notes. */
  fun enrich(e: Exercise): Exercise {
    val profile = safetyFor(e.name)
    val setups = (e.equipmentSetup + exerciseSetups[e.id].orEmpty()).distinctBy { it.equipmentId }
    return e.copy(safetyTips = e.safetyTips.ifEmpty { profile.safetyTips }, injuryPrevention = e.injuryPrevention.ifEmpty { profile.injuryPrevention },
      mistakes = (if (e.guideKind.isBlank()) profile.mistakes + e.mistakes else e.mistakes).distinct(), equipmentSetup = setups,
      equipmentIds = (e.equipmentIds + setups.map { it.equipmentId }).distinct())
  }

  companion object {
    @Volatile private var cached: TrainingGuideLibrary? = null
    fun load(context: Context): TrainingGuideLibrary = cached ?: synchronized(this) {
      cached ?: context.assets.open("training_guides.json").bufferedReader().use {
        val base = Json.decodeFromString<TrainingGuideLibrary>(it.readText())
        val cardio = context.assets.open("cardio_guides.json").bufferedReader().use { reader ->
          Json.decodeFromString<TrainingGuideLibrary>(reader.readText())
        }
        base.copy(equipment = base.equipment + cardio.equipment, profiles = base.profiles + cardio.profiles,
          exercises = base.exercises + cardio.exercises, exerciseSetups = base.exerciseSetups + cardio.exerciseSetups,
          sources = base.sources + cardio.sources)
      }.also { cached = it }
    }
  }
}
