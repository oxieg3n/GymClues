package com.gymclues.app.ui

import com.gymclues.app.data.TemplateItem
import kotlinx.serialization.Serializable

@Serializable data class ActiveDraft(val sessionId: String, val workoutDate: String, val exercises: List<DraftExercise>)
@Serializable data class DraftExercise(val uid: String, val exerciseId: String, val swappedFrom: String?,
  val reps: String, val restSec: Int, val item: TemplateItem?, val tracking: String,
  val sets: List<DraftSet>, val cardio: DraftCardio?, val superSetId: String = "", val sethStarts: List<Int> = emptyList(),
  val sethIds: Map<Int, String> = emptyMap())
@Serializable data class DraftSet(val weight: String, val reps: String, val done: Boolean, val logged: Boolean,
  val logId: Long = 0, val loggedAt: Long = 0)
@Serializable data class DraftCardio(val time: String, val speed: String, val incline: String, val floors: String,
  val distance: String, val resistance: String, val edited: Boolean)

fun SessionExercise.draft() = DraftExercise(uid, exerciseId, swappedFrom, reps, restSec, item, tracking,
  sets.map { DraftSet(it.weight, it.reps, it.done, it.logged, it.logId, it.loggedAt) }, cardio?.let {
    DraftCardio(it.time, it.speed, it.incline, it.floors, it.distance, it.resistance, it.edited) }, superSetId, sethStarts.toList(), sethIds.toMap())

fun DraftExercise.restore(): SessionExercise = SessionExercise(exerciseId, swappedFrom, reps, restSec,
  sets.size, item, tracking, uid).also { exercise ->
  exercise.superSetId = superSetId; exercise.sethStarts.clear(); exercise.sethStarts.addAll(sethStarts)
  exercise.sethIds.clear(); exercise.sethIds.putAll(sethIds)
  exercise.sets.clear()
  sets.forEach { saved -> exercise.sets += SetRow().apply {
    weight = saved.weight; reps = saved.reps; done = saved.done; logged = saved.logged; logId = saved.logId; loggedAt = saved.loggedAt
  } }
  cardio?.let { saved -> exercise.cardio?.apply {
    time = saved.time; speed = saved.speed; incline = saved.incline; floors = saved.floors
    distance = saved.distance; resistance = saved.resistance; edited = saved.edited
  } }
}
