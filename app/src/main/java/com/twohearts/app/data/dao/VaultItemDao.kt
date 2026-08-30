package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.VaultItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for VaultItem entity.
 * Matches legacy VaultRepository functionality.
 */
@Dao
interface VaultItemDao {
    @Query("SELECT * FROM vault_items WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun getAll(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): VaultItem?

    @Query("SELECT * FROM vault_items WHERE content_type = :contentType AND deleted_at IS NULL ORDER BY created_at DESC")
    fun getByContentType(contentType: String): Flow<List<VaultItem>>

    @Insert
    suspend fun insert(vaultItem: VaultItem)

    @Update
    suspend fun update(vaultItem: VaultItem)

    @Query("UPDATE vault_items SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM vault_items WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
