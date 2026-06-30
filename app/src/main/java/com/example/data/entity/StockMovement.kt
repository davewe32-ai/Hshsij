package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materialId: Int,
    val materialName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quantity: Double,
    val type: String, // "ADD", "REMOVE", "ADJUST", "TRANSFER", "ISSUE_PROJECT", "DAMAGE", "LOST"
    val reason: String,
    val user: String, // "Administrator", "Store Keeper", "Manager"
    val projectId: Int? = null,
    val projectName: String? = null,
    val fromCabinetId: Int? = null,
    val toCabinetId: Int? = null
)
