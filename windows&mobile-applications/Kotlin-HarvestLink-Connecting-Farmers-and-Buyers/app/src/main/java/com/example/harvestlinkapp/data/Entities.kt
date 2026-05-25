package com.example.harvestlinkapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produce")
data class ProduceEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serverId: String? = null,
    val name: String,
    val variety: String,
    val grade: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val harvestDate: String,
    val location: String,
    val imageUrl: String? = null,
    val isSynced: Boolean = false
)
@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val payloadJson: String,
    val targetEndpoint: String
)
