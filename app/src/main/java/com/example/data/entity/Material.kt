package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val materialCode: String, // auto-generated e.g. MAT-1001
    val barcode: String? = null,
    val unit: String, // "pcs", "bags", "kg", "feet", "meters", "liters", etc.
    val quantityInStock: Double,
    val minimumStockLevel: Double,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val supplier: String,
    val datePurchased: String,
    val notes: String,
    val photoUri: String? = null,
    val cabinetId: Int? = null, // cabinet reference
    val shelfPosition: String = "" // exact position inside cabinet
)
