package com.gymclues.app.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable data class ProgramLibrary(val programs: List<WorkoutProgram>)
@Serializable data class WorkoutProgram(val id: String, val name: String, val split: String, val author: String,
  val level: String, val duration: String, val description: String, val schedule: String, val progression: String,
  val rest: String, val sourceUrl: String, val cycleLength: Int, val days: List<ProgramDay>, val notes: List<ProgramNote>,
  val totalWeeks: Int = 12)   // upper bound of the published duration; 12 when the source gives no fixed length
@Serializable data class ProgramNote(val topic: String, val text: String)
@Serializable data class ProgramDay(val day: Int, val session: String, val training: Boolean, val items: List<Prescription>)
@Serializable data class Prescription(val phase: String, val exercise: String, val sets: String, val reps: String,
  val repsNote: String, val rest: String, val restSec: Int, val setCount: Int, val notes: String, val substitution: String)
data class AssignedDay(val program: WorkoutProgram, val day: ProgramDay, val date: LocalDate, val userRest: Boolean = false)
private val REST = ProgramDay(0, "Rest day", false, emptyList())

fun assignmentOn(assignments: List<ProgramAssignmentEntity>, date: LocalDate) = assignments.filter { !it.deleted &&
  it.startDate <= date.toString() && (it.endDate.isBlank() || date.toString() < it.endDate) }.maxByOrNull { it.startDate }

/**
 * Streak while a program is assigned: consecutive scheduled training days with a logged workout, counted back from today.
 * Cycle rest days and the user's own rest days are skipped, so they never break it; a missed training day does.
 * Today only counts once logged and never breaks the streak. Returns null when no program is active today.
 */
fun programStreak(done: Set<LocalDate>, programs: List<WorkoutProgram>, assignments: List<ProgramAssignmentEntity>,
                  rest: RestRules, today: LocalDate): Int? = programStreakDays(done, programs, assignments, rest, today)?.size

/** The completed training days that make up the current program streak (null when no program is active today). */
fun programStreakDays(done: Set<LocalDate>, programs: List<WorkoutProgram>, assignments: List<ProgramAssignmentEntity>,
                      rest: RestRules, today: LocalDate): Set<LocalDate>? {
  if (assignmentOn(assignments, today) == null) return null
  val days = HashSet<LocalDate>(); var d = today
  while (true) {
    val a = scheduledDay(programs, assignments, d, rest) ?: break
    if (a.day.training && !a.userRest) { if (d in done) days.add(d) else if (d != today) break }
    d = d.minusDays(1)
  }
  return days
}

/** Completed scheduled training days vs. the program's total (training days per cycle x weeks, scaled to the cycle length). */
data class ProgramProgress(val program: WorkoutProgram, val done: Int, val total: Int) {
  val pct get() = if (total == 0) 0 else minOf(100, done * 100 / total)
  val complete get() = done >= total
}
fun programProgress(done: Set<LocalDate>, programs: List<WorkoutProgram>, assignments: List<ProgramAssignmentEntity>,
                    rest: RestRules, today: LocalDate): ProgramProgress? {
  val a = assignmentOn(assignments, today) ?: return null
  val p = programs.firstOrNull { it.id == a.programId } ?: return null
  val total = maxOf(1, Math.round(p.days.count { it.training } * p.totalWeeks * 7.0 / p.cycleLength).toInt())
  var n = 0; var d = LocalDate.parse(a.startDate)
  while (!d.isAfter(today)) { if (d in done && scheduledDay(programs, assignments, d, rest)?.let { it.day.training && !it.userRest } == true) n++; d = d.plusDays(1) }
  return ProgramProgress(p, n, total)
}

/** The user's days off. */
class RestRules(private val rules: List<RestDayEntity> = emptyList()) {
  fun isRest(d: LocalDate): Boolean { val s = d.toString()
    return rules.any { !it.deleted && s >= it.startDate && (it.endDate.isBlank() || s < it.endDate) && (it.weekday == 0 || it.weekday == d.dayOfWeek.value) } }
  companion object { val NONE = RestRules() }
}

/**
 * Cycle day = number of non-rest days since the start date, mod cycle length, so a user rest day pushes the whole
 * program back one day instead of skipping a session. Date-only arithmetic handles DST and the 8-day P3 cycle.
 */
fun scheduledDay(programs: List<WorkoutProgram>, assignments: List<ProgramAssignmentEntity>, date: LocalDate,
                 rest: RestRules = RestRules.NONE): AssignedDay? {
  val a = assignmentOn(assignments, date) ?: return null
  val p = programs.firstOrNull { it.id == a.programId } ?: return null
  if (rest.isRest(date)) return AssignedDay(p, REST, date, userRest = true)
  var idx = 0; var d = LocalDate.parse(a.startDate)
  while (d.isBefore(date)) { if (!rest.isRest(d)) idx++; d = d.plusDays(1) }
  val day = p.days.firstOrNull { it.day == idx % p.cycleLength + 1 } ?: return null
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

  // ---- rest days ----
  fun restRules() = dao.restDays(session.userId).map { RestRules(it) }
  fun restDays() = dao.restDays(session.userId)
  private suspend fun active(): List<RestDayEntity> { val t = LocalDate.now().toString()
    return dao.restDaysOnce(session.userId).filter { !it.deleted && (it.endDate.isBlank() || it.endDate > t) } }
  private suspend fun save(r: RestDayEntity, op: String) { dao.upsertRestDay(r.copy(updatedAt = now(), syncState = SyncState.PENDING))
    dao.enqueue(OutboxEntity(entityType = "rest_day", entityId = r.id, op = op)) }
  private suspend fun end(r: RestDayEntity) { val t = LocalDate.now().toString()
    save(if (r.startDate >= t) r.copy(deleted = true) else r.copy(endDate = t), "update") }

  /** Repeat every [weekday] (1 = Monday ... 7 = Sunday). Refuses to block all seven days. */
  suspend fun setWeekly(weekday: Int, on: Boolean): Boolean = db.withTransaction {
    val cur = active().filter { it.weekday != 0 }
    val mine = cur.filter { it.weekday == weekday }
    if (!on) { mine.forEach { end(it) }; return@withTransaction true }
    if (mine.isNotEmpty()) return@withTransaction true
    if (cur.map { it.weekday }.toSet().size >= 6) return@withTransaction false
    save(RestDayEntity(ownerId = session.userId, weekday = weekday, startDate = LocalDate.now().toString()), "create"); true
  }
  /** A single rest day (today or later). */
  suspend fun addRestDate(date: LocalDate) = db.withTransaction {
    require(!date.isBefore(LocalDate.now()))
    if (active().none { it.weekday == 0 && it.startDate == date.toString() })
      save(RestDayEntity(ownerId = session.userId, weekday = 0, startDate = date.toString(), endDate = date.plusDays(1).toString()), "create")
  }
  suspend fun removeRestDate(date: LocalDate) = db.withTransaction {
    dao.restDaysOnce(session.userId).filter { !it.deleted && it.weekday == 0 && it.startDate == date.toString() }
      .forEach { save(it.copy(deleted = true), "update") } }
}
