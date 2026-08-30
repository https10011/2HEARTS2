package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SchemaMigration entity — tracks database migration versions.
 * Matches legacy schema_migrations table (migration 001).
 */
@Entity(tableName = "schema_migrations")
data class SchemaMigration(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "version")
    val version: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "applied_at")
    val appliedAt: String = java.time.Instant.now().toString()
)
