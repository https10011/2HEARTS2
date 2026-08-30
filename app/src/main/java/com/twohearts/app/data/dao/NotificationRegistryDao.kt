package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.NotificationRegistry

/**
 * Data Access Object for NotificationRegistry entity.
 * Matches legacy NotificationRegistryRepository functionality.
 */
@Dao
interface NotificationRegistryDao {
    @Query("SELECT * FROM notification_registry WHERE deleted_at IS NULL")
    suspend fun getAll(): List<NotificationRegistry>

    @Query("SELECT * FROM notification_registry WHERE owner_ref = :ownerRef AND deleted_at IS NULL")
    suspend fun getByOwnerRef(ownerRef: String): NotificationRegistry?

    @Query("SELECT * FROM notification_registry WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): NotificationRegistry?

    @Insert
    suspend fun insert(registry: NotificationRegistry)

    @Update
    suspend fun update(registry: NotificationRegistry)

    @Query("UPDATE notification_registry SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("DELETE FROM notification_registry WHERE deleted_at IS NOT NULL")
    suspend fun pruneDeleted()
}
