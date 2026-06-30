package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cabinets")
data class Cabinet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String, // Unique e.g. "Cabinet A1", "Cabinet B2", "Rack C3"
    val description: String,
    val location: String,
    val photoUri: String? = null
)
