package com.gymclues.app.data.nutrition

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class FoodUsage(val foodId: String, val uses: Int, val lastUsed: Long)

@Dao interface NutritionDao {
  @Query("SELECT * FROM nutrition_foods WHERE ownerId=:owner AND deleted=0 ORDER BY favorite DESC, updatedAt DESC") fun foods(owner: String): Flow<List<FoodEntity>>
  @Query("SELECT * FROM nutrition_foods WHERE ownerId=:owner AND id=:id AND deleted=0") suspend fun food(owner: String, id: String): FoodEntity?
  @Query("SELECT * FROM nutrition_foods WHERE ownerId=:owner AND barcode=:code AND deleted=0 LIMIT 1") suspend fun barcode(owner: String, code: String): FoodEntity?
  @Query("SELECT * FROM nutrition_foods WHERE id=:id") suspend fun foodById(id: String): FoodEntity?
  @Upsert suspend fun putFood(food: FoodEntity)
  @Query("SELECT * FROM nutrition_diary WHERE ownerId=:owner AND deleted=0 AND date BETWEEN :start AND :end ORDER BY date DESC, createdAt") fun diary(owner: String, start: String, end: String): Flow<List<DiaryEntry>>
  @Query("SELECT * FROM nutrition_diary WHERE ownerId=:owner AND date=:date AND deleted=0 ORDER BY createdAt") suspend fun dayOnce(owner: String, date: String): List<DiaryEntry>
  @Query("SELECT * FROM nutrition_diary WHERE ownerId=:owner AND deleted=0 ORDER BY createdAt DESC LIMIT 500") fun recent(owner: String): Flow<List<DiaryEntry>>
  @Query("SELECT foodId, COUNT(*) AS uses, MAX(createdAt) AS lastUsed FROM nutrition_diary WHERE ownerId=:owner AND deleted=0 GROUP BY foodId ORDER BY uses DESC, lastUsed DESC") fun usage(owner: String): Flow<List<FoodUsage>>
  @Query("SELECT * FROM nutrition_diary WHERE ownerId=:owner AND id=:id AND deleted=0") suspend fun entry(owner: String, id: String): DiaryEntry?
  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertEntry(entry: DiaryEntry): Long
  @Update suspend fun updateEntry(entry: DiaryEntry)
  @Query("SELECT * FROM nutrition_bundles WHERE ownerId=:owner AND deleted=0 ORDER BY name") fun bundles(owner: String): Flow<List<FoodBundle>>
  @Query("SELECT * FROM nutrition_bundles WHERE ownerId=:owner AND id=:id AND deleted=0") suspend fun bundle(owner: String, id: String): FoodBundle?
  @Query("SELECT * FROM nutrition_bundles WHERE id=:id") suspend fun bundleById(id: String): FoodBundle?
  @Query("SELECT * FROM nutrition_bundle_items WHERE bundleId=:id ORDER BY rowid") suspend fun bundleItems(id: String): List<BundleItem>
  @Upsert suspend fun putBundle(bundle: FoodBundle)
  @Query("DELETE FROM nutrition_bundle_items WHERE bundleId=:id") suspend fun clearBundle(id: String)
  @Insert suspend fun putItems(items: List<BundleItem>)
  @Query("SELECT * FROM nutrition_goals WHERE ownerId=:owner ORDER BY effectiveDate DESC") fun goals(owner: String): Flow<List<NutritionGoal>>
  @Upsert suspend fun putGoal(goal: NutritionGoal)
  @Query("SELECT * FROM nutrition_weights WHERE ownerId=:owner AND deleted=0 ORDER BY date") fun weights(owner: String): Flow<List<WeightLog>>
  @Query("SELECT * FROM nutrition_weights WHERE ownerId=:owner AND id=:id") suspend fun weight(owner: String, id: String): WeightLog?
  @Upsert suspend fun putWeight(weight: WeightLog)
  @Query("SELECT * FROM nutrition_water WHERE ownerId=:owner AND deleted=0 AND date BETWEEN :start AND :end ORDER BY date, createdAt") fun water(owner: String, start: String, end: String): Flow<List<WaterLog>>
  @Query("SELECT * FROM nutrition_water WHERE ownerId=:owner AND id=:id") suspend fun waterEntry(owner: String, id: String): WaterLog?
  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertWater(water: WaterLog): Long
  @Update suspend fun updateWater(water: WaterLog)
  @Query("DELETE FROM nutrition_diary WHERE ownerId=:owner") suspend fun clearDiary(owner: String)
  @Query("DELETE FROM nutrition_water WHERE ownerId=:owner") suspend fun clearWater(owner: String)
  @Query("DELETE FROM nutrition_weights WHERE ownerId=:owner") suspend fun clearWeights(owner: String)
  @Query("DELETE FROM nutrition_bundles WHERE ownerId=:owner") suspend fun clearBundles(owner: String)
  @Query("DELETE FROM nutrition_foods WHERE ownerId=:owner") suspend fun clearFoods(owner: String)
  @Query("DELETE FROM nutrition_goals WHERE ownerId=:owner") suspend fun clearGoals(owner: String)
}

/** Each licensed provider has its own private cache file; no shared/community catalog or nutrition outbox. */
@Dao interface FoodCacheDao {
  @Query("SELECT * FROM nutrition_foods WHERE ownerId=:owner AND deleted=0 AND (name || ' ' || brand) LIKE :pattern ESCAPE '\\' ORDER BY CASE WHEN id IN (:usedIds) THEN 0 ELSE 1 END, favorite DESC, updatedAt DESC LIMIT 500") fun foods(owner: String, pattern: String = "%", usedIds: List<String> = emptyList()): Flow<List<FoodEntity>>
  @Query("SELECT * FROM nutrition_foods WHERE ownerId=:owner AND id=:id AND deleted=0") suspend fun food(owner: String, id: String): FoodEntity?
  @Query("SELECT * FROM nutrition_foods WHERE ownerId=:owner AND barcode=:code AND deleted=0 LIMIT 1") suspend fun barcode(owner: String, code: String): FoodEntity?
  @Upsert suspend fun put(food: FoodEntity)
  @Query("DELETE FROM nutrition_foods WHERE ownerId=:owner") suspend fun clear(owner: String)
}
@Database(entities = [FoodEntity::class], version = 1, exportSchema = true)
@TypeConverters(NutritionConverters::class)
abstract class FoodCacheDb : RoomDatabase() { abstract fun foods(): FoodCacheDao }
