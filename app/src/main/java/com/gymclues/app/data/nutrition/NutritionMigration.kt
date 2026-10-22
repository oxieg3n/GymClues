package com.gymclues.app.data.nutrition

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
  override fun migrate(db: SupportSQLiteDatabase) { NUTRITION_SCHEMA.forEach(db::execSQL) }
}

private val nutrientColumns = listOf("kcal", "protein", "carbs", "fat", "fiber", "sugar", "sodium")
private fun nutrientSql(prefix: String = "") = nutrientColumns.joinToString { "`${prefix}$it` REAL" }
private const val META = "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL"
val NUTRITION_SCHEMA = listOf(
  "CREATE TABLE IF NOT EXISTS `nutrition_foods` (`id` TEXT NOT NULL PRIMARY KEY, `ownerId` TEXT NOT NULL, `name` TEXT NOT NULL, `brand` TEXT NOT NULL, `barcode` TEXT NOT NULL, `source` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `sourceUrl` TEXT NOT NULL, `license` TEXT NOT NULL, `basisQuantity` REAL NOT NULL, `basisUnit` TEXT NOT NULL, ${nutrientSql()}, `portions` TEXT NOT NULL, `favorite` INTEGER NOT NULL, $META)",
  "CREATE UNIQUE INDEX IF NOT EXISTS `index_nutrition_foods_ownerId_source_sourceId` ON `nutrition_foods` (`ownerId`, `source`, `sourceId`)",
  "CREATE INDEX IF NOT EXISTS `index_nutrition_foods_ownerId_barcode` ON `nutrition_foods` (`ownerId`, `barcode`)",
  "CREATE TABLE IF NOT EXISTS `nutrition_diary` (`id` TEXT NOT NULL PRIMARY KEY, `ownerId` TEXT NOT NULL, `date` TEXT NOT NULL, `meal` TEXT NOT NULL, `foodId` TEXT NOT NULL, `snapshot` TEXT NOT NULL, `quantity` REAL NOT NULL, `unit` TEXT NOT NULL, `canonicalPerUnit` REAL NOT NULL, $META)",
  "CREATE INDEX IF NOT EXISTS `index_nutrition_diary_ownerId_date` ON `nutrition_diary` (`ownerId`, `date`)",
  "CREATE INDEX IF NOT EXISTS `index_nutrition_diary_ownerId_foodId` ON `nutrition_diary` (`ownerId`, `foodId`)",
  "CREATE TABLE IF NOT EXISTS `nutrition_bundles` (`id` TEXT NOT NULL PRIMARY KEY, `ownerId` TEXT NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `servings` REAL NOT NULL, `instructions` TEXT NOT NULL, $META)",
  "CREATE INDEX IF NOT EXISTS `index_nutrition_bundles_ownerId_kind` ON `nutrition_bundles` (`ownerId`, `kind`)",
  "CREATE TABLE IF NOT EXISTS `nutrition_bundle_items` (`id` TEXT NOT NULL PRIMARY KEY, `bundleId` TEXT NOT NULL, `snapshot` TEXT NOT NULL, `quantity` REAL NOT NULL, `unit` TEXT NOT NULL, `canonicalPerUnit` REAL NOT NULL, FOREIGN KEY(`bundleId`) REFERENCES `nutrition_bundles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
  "CREATE INDEX IF NOT EXISTS `index_nutrition_bundle_items_bundleId` ON `nutrition_bundle_items` (`bundleId`)",
  "CREATE TABLE IF NOT EXISTS `nutrition_goals` (`id` TEXT NOT NULL PRIMARY KEY, `ownerId` TEXT NOT NULL, `effectiveDate` TEXT NOT NULL, ${nutrientSql("daily_")}, ${nutrientSql("rest_")}, `separateRest` INTEGER NOT NULL, `purpose` TEXT NOT NULL, `weightUnit` TEXT NOT NULL, `foodUnit` TEXT NOT NULL, `waterGoalMl` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL)",
  "CREATE UNIQUE INDEX IF NOT EXISTS `index_nutrition_goals_ownerId_effectiveDate` ON `nutrition_goals` (`ownerId`, `effectiveDate`)",
  "CREATE TABLE IF NOT EXISTS `nutrition_weights` (`id` TEXT NOT NULL PRIMARY KEY, `ownerId` TEXT NOT NULL, `date` TEXT NOT NULL, `kg` REAL NOT NULL, $META)",
  "CREATE UNIQUE INDEX IF NOT EXISTS `index_nutrition_weights_ownerId_date` ON `nutrition_weights` (`ownerId`, `date`)",
  "CREATE TABLE IF NOT EXISTS `nutrition_water` (`id` TEXT NOT NULL PRIMARY KEY, `ownerId` TEXT NOT NULL, `date` TEXT NOT NULL, `ml` REAL NOT NULL, $META)",
  "CREATE INDEX IF NOT EXISTS `index_nutrition_water_ownerId_date` ON `nutrition_water` (`ownerId`, `date`)"
)
