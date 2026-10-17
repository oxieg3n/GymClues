package com.gymclues.app.data
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
 * Every user-created record uses a UUID string id + ownerId + timestamps + syncState so it can be uploaded to,
 * and downloaded from, a shared GymClues server without id collisions. Deletes are soft (deleted = 1) so the
 * deletion itself can be synced. Each local write also adds a row to Outbox for a future sync worker.
 */

class Converters {
  private val json = Json
  @TypeConverter fun fromList(v: List<String>): String = json.encodeToString(v)
  @TypeConverter fun toList(v: String): List<String> = if (v.isBlank()) emptyList() else runCatching { json.decodeFromString<List<String>>(v) }.getOrDefault(emptyList())
}

@Entity data class Unavailable(@PrimaryKey val equipmentId: String)

@Entity data class SetLog(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: Long,
  val exerciseId: String, val setNo: Int, val reps: Int, val weight: Double, val units: String = "lb", val notes: String = "",
  @ColumnInfo(defaultValue = "") val sessionId: String = "",
  @ColumnInfo(defaultValue = "") val gymId: String = "")

@Entity(tableName = "workouts") data class WorkoutEntity(
  @PrimaryKey val id: String = newId(), val name: String, val description: String = "", val split: String = "Custom",
  val ownerId: String, val visibility: String = Visibility.PRIVATE, val sourceTemplateId: String = "",
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL, val deleted: Boolean = false)

@Entity(tableName = "workout_items", indices = [Index("workoutId")]) data class WorkoutItemEntity(
  @PrimaryKey val id: String = newId(), val workoutId: String, val position: Int,
  val exerciseId: String, val sets: Int = 3, val reps: String = "10-12", val restSec: Int = 90)

@Entity(tableName = "gyms", indices = [Index(value = ["provider", "providerPlaceId"], unique = true)]) data class GymEntity(
  @PrimaryKey val id: String = newId(),
  val provider: String,              // "osm", "google", or "manual"
  val providerPlaceId: String,       // stable id from the directory, used to detect "already added"
  val name: String, val address: String = "", val lat: Double = 0.0, val lng: Double = 0.0,
  val status: String = ReviewStatus.APPROVED, val createdBy: String,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL, val deleted: Boolean = false)

@Entity(tableName = "gym_equipment", indices = [Index("gymId")]) data class GymEquipmentEntity(
  @PrimaryKey val id: String = newId(), val gymId: String,
  val catalogEquipmentId: String = "",            // link to catalog Equipment when known
  val name: String, val manufacturer: String = "", val model: String = "", val category: String = "",
  val alternateNames: List<String> = emptyList(), val primaryMuscles: List<String> = emptyList(),
  val howToUse: List<String> = emptyList(), val qrCode: String = "", val videoUrl: String = "", val notes: String = "",
  val photoUris: List<String> = emptyList(),
  /** field name -> where its value came from ("user", "catalog:eq_x", "admin"); blank fields are simply absent */
  val fieldSources: List<String> = emptyList(),
  val status: String = ReviewStatus.SUBMITTED, val reviewNote: String = "", val submittedBy: String,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL, val deleted: Boolean = false)

/** A program the user follows. Cycle day = days since startDate mod cycle length; a newer assignment replaces the old one from its start date. */
@Entity(tableName = "program_assignments", indices = [Index("ownerId")]) data class ProgramAssignmentEntity(
  @PrimaryKey val id: String = newId(), val programId: String, val startDate: String, val endDate: String = "",
  val ownerId: String, val visibility: String = Visibility.PRIVATE,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL, val deleted: Boolean = false)

/**
 * A day the user cannot train. weekday 1-7 (Mon-Sun) = repeats weekly, 0 = a single date. Applies to dates in [startDate, endDate);
 * removing a rule ends it today instead of rewriting history. Programs skip these days (see scheduledDay).
 */
@Entity(tableName = "rest_days", indices = [Index("ownerId")]) data class RestDayEntity(
  @PrimaryKey val id: String = newId(), val ownerId: String, val weekday: Int, val startDate: String, val endDate: String = "",
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL, val deleted: Boolean = false)

/** A workout the user finished (tapped Done). Its sets are the SetLog rows with the same sessionId. */
@Entity(tableName = "completed_sessions", indices = [Index("ownerId")]) data class CompletedSessionEntity(
  @PrimaryKey val id: String, val ownerId: String, val name: String, val gymId: String = "", val completedAt: Long = now(),
  val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL, val deleted: Boolean = false)

/** An exercise a user added. Private until the user shares it; merged into the catalog on this device. */
@Entity(tableName = "custom_exercises", indices = [Index("ownerId")]) data class CustomExerciseEntity(
  @PrimaryKey val id: String = "ex_u_" + newId(), val name: String, val equipmentIds: List<String> = emptyList(),
  val primaryMuscles: List<String> = emptyList(), val movement: String = "", val steps: List<String> = emptyList(),
  val safety: String = "", val ownerId: String, val visibility: String = Visibility.PRIVATE, val status: String = ReviewStatus.DRAFT,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL, val deleted: Boolean = false)

@Entity(tableName = "users") data class UserEntity(
  @PrimaryKey val id: String = newId(), val displayName: String, val email: String = "",
  val role: String = Roles.USER, val status: String = AccountStatus.ACTIVE,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = SyncState.LOCAL)

@Entity(tableName = "audit_log") data class AuditEntity(
  @PrimaryKey val id: String = newId(), val actorId: String, val action: String,
  val targetType: String, val targetId: String, val detail: String = "", val at: Long = now())

@Entity(tableName = "outbox") data class OutboxEntity(
  @PrimaryKey val id: String = newId(), val entityType: String, val entityId: String,
  val op: String, val createdAt: Long = now(), val attempts: Int = 0)

@Dao interface GymDao {
  // equipment toggles + logs (v1)
  @Query("SELECT * FROM Unavailable") fun unavailable(): Flow<List<Unavailable>>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun markUnavailable(u: Unavailable)
  @Delete suspend fun markAvailable(u: Unavailable)
  @Insert suspend fun log(s: SetLog)
  @Query("SELECT * FROM SetLog ORDER BY date DESC") fun history(): Flow<List<SetLog>>

  // workouts
  @Query("SELECT * FROM workouts WHERE deleted = 0 AND ownerId = :owner ORDER BY updatedAt DESC") fun workouts(owner: String): Flow<List<WorkoutEntity>>
  @Query("SELECT * FROM workouts WHERE id = :id") suspend fun workout(id: String): WorkoutEntity?
  @Query("SELECT * FROM workout_items WHERE workoutId = :id ORDER BY position") suspend fun workoutItems(id: String): List<WorkoutItemEntity>
  @Query("SELECT * FROM workout_items") fun allWorkoutItems(): Flow<List<WorkoutItemEntity>>
  @Upsert suspend fun upsertWorkout(w: WorkoutEntity)
  @Query("DELETE FROM workout_items WHERE workoutId = :id") suspend fun clearItems(id: String)
  @Insert suspend fun insertItems(items: List<WorkoutItemEntity>)
  @Query("UPDATE workouts SET ownerId = :owner WHERE ownerId = '__local__'") suspend fun adoptOwner(owner: String)

  // gyms
  @Query("SELECT * FROM gyms WHERE deleted = 0 ORDER BY name") fun gyms(): Flow<List<GymEntity>>
  @Query("SELECT * FROM gyms WHERE id = :id") suspend fun gym(id: String): GymEntity?
  @Query("SELECT * FROM gyms WHERE id = :id") fun gymFlow(id: String): Flow<GymEntity?>
  @Query("SELECT * FROM gyms WHERE provider = :p AND providerPlaceId = :pid LIMIT 1") suspend fun gymByPlace(p: String, pid: String): GymEntity?
  @Upsert suspend fun upsertGym(g: GymEntity)

  // gym equipment / submissions
  @Query("SELECT * FROM gym_equipment WHERE deleted = 0 AND gymId = :gymId ORDER BY name") fun gymEquipment(gymId: String): Flow<List<GymEquipmentEntity>>
  @Query("SELECT * FROM gym_equipment WHERE deleted = 0 ORDER BY updatedAt DESC") fun allGymEquipment(): Flow<List<GymEquipmentEntity>>
  @Query("SELECT * FROM gym_equipment WHERE id = :id") suspend fun gymEquipmentItem(id: String): GymEquipmentEntity?
  @Upsert suspend fun upsertGymEquipment(e: GymEquipmentEntity)

  // program assignments + user-added exercises
  @Query("SELECT * FROM program_assignments WHERE deleted = 0 AND ownerId = :owner ORDER BY startDate") fun assignments(owner: String): Flow<List<ProgramAssignmentEntity>>
  @Query("SELECT * FROM program_assignments WHERE ownerId = :owner") suspend fun assignmentsOnce(owner: String): List<ProgramAssignmentEntity>
  @Upsert suspend fun upsertAssignment(a: ProgramAssignmentEntity)
  @Query("SELECT * FROM custom_exercises WHERE deleted = 0 AND ownerId = :owner ORDER BY name") fun customExercises(owner: String): Flow<List<CustomExerciseEntity>>
  @Query("SELECT * FROM custom_exercises WHERE id = :id") suspend fun customExercise(id: String): CustomExerciseEntity?
  @Upsert suspend fun upsertCustomExercise(e: CustomExerciseEntity)
  @Query("SELECT * FROM rest_days WHERE deleted = 0 AND ownerId = :owner ORDER BY startDate") fun restDays(owner: String): Flow<List<RestDayEntity>>
  @Query("SELECT * FROM rest_days WHERE ownerId = :owner") suspend fun restDaysOnce(owner: String): List<RestDayEntity>
  @Upsert suspend fun upsertRestDay(r: RestDayEntity)
  @Upsert suspend fun upsertSession(s: CompletedSessionEntity)
  @Query("SELECT * FROM completed_sessions WHERE id = :id") suspend fun session(id: String): CompletedSessionEntity?
  @Update suspend fun updateSet(s: SetLog)
  @Query("DELETE FROM SetLog WHERE id = :id") suspend fun deleteSet(id: Long)
  @Query("DELETE FROM SetLog WHERE sessionId = :sessionId") suspend fun deleteSets(sessionId: String)
  @Query("SELECT * FROM completed_sessions WHERE deleted = 0 AND ownerId = :owner ORDER BY completedAt DESC") fun sessions(owner: String): Flow<List<CompletedSessionEntity>>

  // users / admin
  @Query("SELECT * FROM users ORDER BY displayName") fun users(): Flow<List<UserEntity>>
  @Query("SELECT * FROM users WHERE id = :id") suspend fun user(id: String): UserEntity?
  @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'") suspend fun activeAdminCount(): Int
  @Upsert suspend fun upsertUser(u: UserEntity)
  @Insert suspend fun audit(a: AuditEntity)
  @Query("SELECT * FROM audit_log ORDER BY at DESC LIMIT 200") fun auditLog(): Flow<List<AuditEntity>>

  // sync outbox
  @Insert suspend fun enqueue(o: OutboxEntity)
  @Query("SELECT COUNT(*) FROM outbox") fun pendingSyncCount(): Flow<Int>
  @Query("SELECT * FROM outbox ORDER BY createdAt LIMIT :limit") suspend fun outbox(limit: Int = 100): List<OutboxEntity>
  @Query("DELETE FROM outbox WHERE id IN (:ids)") suspend fun clearOutbox(ids: List<String>)
}

@Database(entities = [Unavailable::class, SetLog::class, WorkoutEntity::class, WorkoutItemEntity::class,
  GymEntity::class, GymEquipmentEntity::class, UserEntity::class, AuditEntity::class, OutboxEntity::class,
  ProgramAssignmentEntity::class, CustomExerciseEntity::class, RestDayEntity::class, CompletedSessionEntity::class],
  version = 5, exportSchema = true)
@TypeConverters(Converters::class)
abstract class GymDb : RoomDatabase() {
  abstract fun dao(): GymDao
  companion object { @Volatile private var i: GymDb? = null
    fun get(c: Context) = i ?: synchronized(this) {
      i ?: Room.databaseBuilder(c, GymDb::class.java, "gym.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { i = it } } }
}

/** v1 -> v3 app (db 1 -> 2): keeps workout history and any old CustomWorkout rows. */
val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `SetLog` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT ''")
    db.execSQL("ALTER TABLE `SetLog` ADD COLUMN `gymId` TEXT NOT NULL DEFAULT ''")
    SCHEMA_V2_CREATE.forEach { db.execSQL(it) }
    // Move v1 CustomWorkout(id, name, exerciseIdsCsv) rows into workouts/workout_items, owned by "local" until a user exists.
    val c = db.query("SELECT name, exerciseIdsCsv FROM CustomWorkout")
    while (c.moveToNext()) {
      val wid = newId(); val t = now()
      db.insert("workouts", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
        put("id", wid); put("name", c.getString(0)); put("description", ""); put("split", "Custom")
        put("ownerId", LOCAL_OWNER_PLACEHOLDER); put("visibility", Visibility.PRIVATE); put("sourceTemplateId", "")
        put("createdAt", t); put("updatedAt", t); put("syncState", SyncState.LOCAL); put("deleted", 0) })
      c.getString(1).split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEachIndexed { i, ex ->
        db.insert("workout_items", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
          put("id", newId()); put("workoutId", wid); put("position", i); put("exerciseId", ex)
          put("sets", 3); put("reps", "10-12"); put("restSec", 90) })
      }
    }
    c.close()
    db.execSQL("DROP TABLE IF EXISTS `CustomWorkout`")
  }
}
const val LOCAL_OWNER_PLACEHOLDER = "__local__"

/** db 2 -> 3: program assignments and user-added exercises. */
val MIGRATION_2_3 = object : Migration(2, 3) {
  override fun migrate(db: SupportSQLiteDatabase) { SCHEMA_V3_CREATE.forEach { db.execSQL(it) } }
}

/** db 4 -> 5: finished workouts shown under Completed Sessions. */
val MIGRATION_4_5 = object : Migration(4, 5) {
  override fun migrate(db: SupportSQLiteDatabase) { SCHEMA_V5_CREATE.forEach { db.execSQL(it) } }
}

/** db 3 -> 4: user rest days. */
val MIGRATION_3_4 = object : Migration(3, 4) {
  override fun migrate(db: SupportSQLiteDatabase) { SCHEMA_V4_CREATE.forEach { db.execSQL(it) } }
}
