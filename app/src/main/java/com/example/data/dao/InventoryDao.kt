package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- Cabinets ---
    @Query("SELECT * FROM cabinets ORDER BY code ASC")
    fun getAllCabinets(): Flow<List<Cabinet>>

    @Query("SELECT * FROM cabinets WHERE id = :id")
    suspend fun getCabinetById(id: Int): Cabinet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCabinet(cabinet: Cabinet): Long

    @Delete
    suspend fun deleteCabinet(cabinet: Cabinet)

    @Query("SELECT COUNT(*) FROM cabinets")
    fun getCabinetsCount(): Flow<Int>


    // --- Materials ---
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<Material>>

    @Query("SELECT * FROM materials WHERE id = :id")
    suspend fun getMaterialById(id: Int): Material?

    @Query("SELECT * FROM materials WHERE cabinetId = :cabinetId")
    fun getMaterialsInCabinet(cabinetId: Int): Flow<List<Material>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: Material): Long

    @Delete
    suspend fun deleteMaterial(material: Material)

    @Query("SELECT COUNT(*) FROM materials")
    fun getMaterialsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM materials WHERE quantityInStock <= minimumStockLevel AND quantityInStock > 0")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM materials WHERE quantityInStock = 0")
    fun getOutOfStockCount(): Flow<Int>

    @Query("SELECT * FROM materials WHERE quantityInStock <= minimumStockLevel AND quantityInStock > 0")
    fun getLowStockMaterials(): Flow<List<Material>>

    @Query("SELECT * FROM materials WHERE quantityInStock = 0")
    fun getOutOfStockMaterials(): Flow<List<Material>>


    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?


    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY name ASC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("SELECT COUNT(*) FROM projects")
    fun getProjectsCount(): Flow<Int>


    // --- Stock Movements ---
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE materialId = :materialId ORDER BY timestamp DESC")
    fun getMovementsByMaterial(materialId: Int): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getMovementsByProject(projectId: Int): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement): Long

    @Query("SELECT COUNT(*) FROM stock_movements WHERE timestamp >= :startOfDay")
    fun getMovementsIssuedToday(startOfDay: Long): Flow<Int>
}
