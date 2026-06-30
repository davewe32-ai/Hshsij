package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val clientName: String,
    val address: String,
    val startDate: String,
    val dueDate: String,
    val status: String, // "Planned", "In Progress", "Completed", "On Hold"
    val budget: Double,
    val notes: String
)
