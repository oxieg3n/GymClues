package com.gymguide.app.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Serializable data class ProgramLibrary(val programs: List<WorkoutProgram>)
@Serializable data class WorkoutProgram(val id: String, val name: String, val split: String, val author: String,
  val level: String, val duration: String, val description: String, val schedule: String, val progression: String,
  val rest: String, val sourceUrl: String, val cycleLength: Int, val days: List<ProgramDay>, val notes: List<ProgramNote>)
@Serializable data class ProgramNote(val topic: String, val text: String)
@Serializable data class ProgramDay(val day: Int, val session: String, val training: Boolean, val items: List<Prescription>)
@Serializable data class Prescription(val phase: String, val exercise: String, val sets: String, val reps: String,
  val repsNote: String, val rest: String, val restSec: Int, val setCount: Int, val notes: String, val substitution: String)
data class AssignedDay(val program: WorkoutProgram, val day: ProgramDay, val date: LocalDate)

/** Date-only arithmetic handles DST and the powerbuilding program's 8-day cycle. */
fun scheduledDay(programs: List<WorkoutProgram>, assignments: List<ProgramAssignmentEntity>, date: LocalDate): AssignedDay? {
  val a = assignments.filter { !it.deleted && it.startDate <= date.toString() &&
    (it.endDate.isBlank() || date.toString() < it.endDate) }.maxByOrNull { it.startDate } ?: return null
  val p = programs.firstOrNull { it.id == a.programId } ?: return null
  val offset = ChronoUnit.DAYS.between(LocalDate.parse(a.startDate), date)
  val day = p.days.firstOrNull { it.day == (offset % p.cycleLength).toInt() + 1 } ?: return null
  return AssignedDay(p, day, date)
}

fun programExerciseId(name: String) = "ex_program_" + name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
fun programPlan(p: WorkoutProgram, day: ProgramDay, resolve: (String) -> String? = { null }): WorkoutPlan = WorkoutPlan("${p.id}_${day.day}",
  "${p.name} - ${day.session}", day.items.map { r ->
    TemplateItem(resolve(r.exercise) ?: programExerciseId(r.exercise), r.setCount, r.reps, r.restSec, r.sets, r.repsNote, r.phase,
      r.rest, r.notes, r.substitution, r.exercise) }, false, p.progression)

class ProgramRepository(ctx: Context, private val db: GymDb, private val session: UserSession, private val catalog: CatalogRepository) {
  val programs = Json.decodeFromString<ProgramLibrary>(ctx.assets.open("programs.json").bufferedReader().use { it.readText() }).programs
  private val dao = db.dao()
  init { programs.forEach { pr -> pr.days.forEach { d -> d.items.forEach { catalog.aliases[programExerciseId(it.exercise)] = it.exercise } } } }
  fun assignments() = dao.assignments(session.userId)
  fun find(id: String) = programs.firstOrNull { it.id == id }
  suspend fun plan(id: String): WorkoutPlan? {
    val pid = id.substringBeforeLast('_'); val n = id.substringAfterLast('_').toIntOrNull()
    val p = find(pid) ?: return null
    return p.days.firstOrNull { it.day == n && it.training }?.let { programPlan(p, it) { n -> catalog.byName(n)?.id } }
  }
  suspend fun assign(programId: String, start: LocalDate) = db.withTransaction {
    require(find(programId) != null)
    require(!start.isBefore(LocalDate.now()))
    val existing = dao.assignmentsOnce(session.userId)
    existing.filter { !it.deleted && (it.endDate.isBlank() || it.endDate > start.toString()) }.forEach { old ->
      val ended = old.copy(endDate = start.toString(), deleted = old.startDate >= start.toString(),
        updatedAt = now(), syncState = SyncState.PENDING)
      dao.upsertAssignment(ended); dao.enqueue(OutboxEntity(entityType = "program_assignment", entityId = old.id, op = "update"))
    }
    val a = ProgramAssignmentEntity(programId = programId, startDate = start.toString(), ownerId = session.userId, syncState = SyncState.PENDING)
    dao.upsertAssignment(a); dao.enqueue(OutboxEntity(entityType = "program_assignment", entityId = a.id, op = "create"))
  }
  suspend fun stop() = db.withTransaction {
    val date = LocalDate.now().toString()
    dao.assignmentsOnce(session.userId).filter { !it.deleted && (it.endDate.isBlank() || it.endDate > date) }.forEach { old ->
      dao.upsertAssignment(old.copy(endDate = date, deleted = old.startDate >= date, updatedAt = now(), syncState = SyncState.PENDING))
      dao.enqueue(OutboxEntity(entityType = "program_assignment", entityId = old.id, op = "update"))
    }
  }
}
