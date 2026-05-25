package com.example.harvestlinkapp.data

import androidx.room.*

@Dao
interface ProduceDao {

    @Query("SELECT * FROM produce ORDER BY name ASC")
    suspend fun getAll(): List<ProduceEntity>

    @Query("DELETE FROM produce WHERE isSynced = 1")
    suspend fun clearSynced()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProduceEntity): Long

    @Update
    suspend fun update(entity: ProduceEntity)

    @Query("DELETE FROM produce")
    suspend fun clear()

    @Query("UPDATE produce SET isSynced = 1 WHERE localId = :id")
    suspend fun markSynced(id: Long)

    @Query("DELETE FROM produce WHERE localId = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface PendingActionDao {

    @Query("SELECT * FROM pending_actions")
    suspend fun getAll(): List<PendingActionEntity>

    @Insert
    suspend fun insert(action: PendingActionEntity): Long

    @Delete
    suspend fun delete(action: PendingActionEntity)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
