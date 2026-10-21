package com.gymclues.app.data

enum class EquipmentType(val title: String) {
  CARDIO("Cardio machines"), UPPER_BODY("Upper-body machines"), LOWER_BODY("Lower-body machines"),
  CORE("Core machines"), CABLE("Cable stations"), SMITH("Smith machines"),
  FREE_WEIGHTS("Free weights & racks"), BENCHES("Benches & supports"),
  OTHER_STRENGTH("Other strength machines"), OTHER("Other equipment")
}

// Workbook categories describe loading mechanisms, so they cannot identify every machine's purpose.
fun equipmentType(id: String, category: String): EquipmentType = when (id) {
  "eq_chest_press", "eq_lat_pulldown", "eq_x_assisted_dip_machine", "eq_x_assisted_pull_up_machine",
  "eq_x_incline_chest_press_machine", "eq_x_lateral_raise_machine", "eq_x_pec_deck_machine",
  "eq_x_preacher_curl_machine", "eq_x_seated_row_machine", "eq_x_shoulder_press_machine",
  "eq_x_triceps_extension_machine" -> EquipmentType.UPPER_BODY
  "eq_leg_press", "eq_x_hack_squat_machine", "eq_x_hip_abduction_machine", "eq_x_hip_adduction_machine",
  "eq_x_leg_extension_machine", "eq_x_lying_leg_curl_machine", "eq_x_seated_calf_raise_machine",
  "eq_x_seated_leg_curl_machine", "eq_x_standing_calf_raise_machine" -> EquipmentType.LOWER_BODY
  "eq_x_abdominal_crunch_machine", "eq_x_lumbar_extension_machine", "eq_x_torso_rotation_machine" -> EquipmentType.CORE
  "eq_smith" -> EquipmentType.SMITH
  "eq_x_45_degree_extension_bench" -> EquipmentType.BENCHES
  else -> when (category.trim().lowercase()) {
    "cardio" -> EquipmentType.CARDIO
    "cable" -> EquipmentType.CABLE
    "free weight" -> EquipmentType.FREE_WEIGHTS
    "bench" -> EquipmentType.BENCHES
    "selectorized", "plate-loaded" -> EquipmentType.OTHER_STRENGTH
    else -> EquipmentType.OTHER
  }
}

val Equipment.machineType get() = equipmentType(id, category)

fun equipmentGroups(equipment: List<Equipment>): Map<EquipmentType, List<Equipment>> =
  equipment.groupBy { it.machineType }.toSortedMap().mapValues { (_, items) ->
    items.sortedWith(compareBy({ it.name.lowercase() }, { it.id }))
  }
