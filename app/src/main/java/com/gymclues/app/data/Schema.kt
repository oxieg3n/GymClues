package com.gymclues.app.data

/** Generated from app/schemas/.../2.json (Room export) — CREATE statements for tables added in db v2. */
val SCHEMA_V2_CREATE = listOf(
  "CREATE TABLE IF NOT EXISTS `workouts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `split` TEXT NOT NULL, `ownerId` TEXT NOT NULL, `visibility` TEXT NOT NULL, `sourceTemplateId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE TABLE IF NOT EXISTS `workout_items` (`id` TEXT NOT NULL, `workoutId` TEXT NOT NULL, `position` INTEGER NOT NULL, `exerciseId` TEXT NOT NULL, `sets` INTEGER NOT NULL, `reps` TEXT NOT NULL, `restSec` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE INDEX IF NOT EXISTS `index_workout_items_workoutId` ON `workout_items` (`workoutId`)",
  "CREATE TABLE IF NOT EXISTS `gyms` (`id` TEXT NOT NULL, `provider` TEXT NOT NULL, `providerPlaceId` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `lat` REAL NOT NULL, `lng` REAL NOT NULL, `status` TEXT NOT NULL, `createdBy` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE UNIQUE INDEX IF NOT EXISTS `index_gyms_provider_providerPlaceId` ON `gyms` (`provider`, `providerPlaceId`)",
  "CREATE TABLE IF NOT EXISTS `gym_equipment` (`id` TEXT NOT NULL, `gymId` TEXT NOT NULL, `catalogEquipmentId` TEXT NOT NULL, `name` TEXT NOT NULL, `manufacturer` TEXT NOT NULL, `model` TEXT NOT NULL, `category` TEXT NOT NULL, `alternateNames` TEXT NOT NULL, `primaryMuscles` TEXT NOT NULL, `howToUse` TEXT NOT NULL, `qrCode` TEXT NOT NULL, `videoUrl` TEXT NOT NULL, `notes` TEXT NOT NULL, `photoUris` TEXT NOT NULL, `fieldSources` TEXT NOT NULL, `status` TEXT NOT NULL, `reviewNote` TEXT NOT NULL, `submittedBy` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE INDEX IF NOT EXISTS `index_gym_equipment_gymId` ON `gym_equipment` (`gymId`)",
  "CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `displayName` TEXT NOT NULL, `email` TEXT NOT NULL, `role` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))",
  "CREATE TABLE IF NOT EXISTS `audit_log` (`id` TEXT NOT NULL, `actorId` TEXT NOT NULL, `action` TEXT NOT NULL, `targetType` TEXT NOT NULL, `targetId` TEXT NOT NULL, `detail` TEXT NOT NULL, `at` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE TABLE IF NOT EXISTS `outbox` (`id` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `op` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `attempts` INTEGER NOT NULL, PRIMARY KEY(`id`))"
)

/** Generated from app/schemas/.../3.json (Room export) -- CREATE statements for tables added in db v3. */
val SCHEMA_V3_CREATE = listOf(
  "CREATE TABLE IF NOT EXISTS `program_assignments` (`id` TEXT NOT NULL, `programId` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `ownerId` TEXT NOT NULL, `visibility` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE INDEX IF NOT EXISTS `index_program_assignments_ownerId` ON `program_assignments` (`ownerId`)",
  "CREATE TABLE IF NOT EXISTS `custom_exercises` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `equipmentIds` TEXT NOT NULL, `primaryMuscles` TEXT NOT NULL, `movement` TEXT NOT NULL, `steps` TEXT NOT NULL, `safety` TEXT NOT NULL, `ownerId` TEXT NOT NULL, `visibility` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE INDEX IF NOT EXISTS `index_custom_exercises_ownerId` ON `custom_exercises` (`ownerId`)"
)

/** CREATE statements for tables added in db v4 (from schemas/4.json). */
val SCHEMA_V4_CREATE = listOf(
  "CREATE TABLE IF NOT EXISTS `rest_days` (`id` TEXT NOT NULL, `ownerId` TEXT NOT NULL, `weekday` INTEGER NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE INDEX IF NOT EXISTS `index_rest_days_ownerId` ON `rest_days` (`ownerId`)"
)

/** CREATE statements for tables added in db v5 (from schemas/5.json). */
val SCHEMA_V5_CREATE = listOf(
  "CREATE TABLE IF NOT EXISTS `completed_sessions` (`id` TEXT NOT NULL, `ownerId` TEXT NOT NULL, `name` TEXT NOT NULL, `gymId` TEXT NOT NULL, `completedAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `deleted` INTEGER NOT NULL, PRIMARY KEY(`id`))",
  "CREATE INDEX IF NOT EXISTS `index_completed_sessions_ownerId` ON `completed_sessions` (`ownerId`)"
)
