package com.example.data.repository

import com.example.data.dao.InventoryDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class InventoryRepository(private val dao: InventoryDao) {

    // Streams
    val allCabinets: Flow<List<Cabinet>> = dao.getAllCabinets()
    val allMaterials: Flow<List<Material>> = dao.getAllMaterials()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()
    val allProjects: Flow<List<Project>> = dao.getAllProjects()
    val allMovements: Flow<List<StockMovement>> = dao.getAllMovements()

    // Counts
    val cabinetsCount: Flow<Int> = dao.getCabinetsCount()
    val materialsCount: Flow<Int> = dao.getMaterialsCount()
    val lowStockCount: Flow<Int> = dao.getLowStockCount()
    val outOfStockCount: Flow<Int> = dao.getOutOfStockCount()

    // Alert items
    val lowStockMaterials: Flow<List<Material>> = dao.getLowStockMaterials()
    val outOfStockMaterials: Flow<List<Material>> = dao.getOutOfStockMaterials()

    fun getMovementsToday(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getMovementsIssuedToday(calendar.timeInMillis)
    }

    // Cabinets
    suspend fun getCabinetById(id: Int): Cabinet? = dao.getCabinetById(id)
    suspend fun insertCabinet(cabinet: Cabinet) = dao.insertCabinet(cabinet)
    suspend fun deleteCabinet(cabinet: Cabinet) = dao.deleteCabinet(cabinet)
    fun getMaterialsInCabinet(cabinetId: Int): Flow<List<Material>> = dao.getMaterialsInCabinet(cabinetId)

    // Materials
    suspend fun getMaterialById(id: Int): Material? = dao.getMaterialById(id)
    suspend fun insertMaterial(material: Material): Int {
        val id = dao.insertMaterial(material)
        return id.toInt()
    }
    suspend fun deleteMaterial(material: Material) = dao.deleteMaterial(material)

    // Categories
    suspend fun insertCategory(category: Category) = dao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)
    suspend fun getCategoryByName(name: String) = dao.getCategoryByName(name)

    // Projects
    suspend fun getProjectById(id: Int): Project? = dao.getProjectById(id)
    suspend fun insertProject(project: Project) = dao.insertProject(project)
    suspend fun deleteProject(project: Project) = dao.deleteProject(project)

    // Stock Movements
    suspend fun insertMovement(movement: StockMovement) = dao.insertMovement(movement)
    fun getMovementsByMaterial(materialId: Int): Flow<List<StockMovement>> = dao.getMovementsByMaterial(materialId)
    fun getMovementsByProject(projectId: Int): Flow<List<StockMovement>> = dao.getMovementsByProject(projectId)

    // Complex business operations

    /**
     * Issues a material to a project.
     * Reduces the inventory, registers a stock movement.
     */
    suspend fun issueMaterialToProject(
        materialId: Int,
        projectId: Int,
        quantity: Double,
        reason: String,
        user: String,
        notes: String
    ): Boolean {
        val material = dao.getMaterialById(materialId) ?: return false
        val project = dao.getProjectById(projectId) ?: return false

        if (material.quantityInStock < quantity) {
            return false // Insufficient stock
        }

        // Deduct from stock
        val updatedMaterial = material.copy(
            quantityInStock = material.quantityInStock - quantity
        )
        dao.insertMaterial(updatedMaterial)

        // Save movement
        val movement = StockMovement(
            materialId = materialId,
            materialName = material.name,
            quantity = quantity,
            type = "ISSUE_PROJECT",
            reason = if (notes.isNotEmpty()) "$reason ($notes)" else reason,
            user = user,
            projectId = projectId,
            projectName = project.name
        )
        dao.insertMovement(movement)
        return true
    }

    /**
     * Adjusts the stock of a material directly.
     * Register a movement (ADD, REMOVE, ADJUST, DAMAGE, LOST)
     */
    suspend fun adjustStock(
        materialId: Int,
        newQuantity: Double,
        adjustmentType: String, // "ADD", "REMOVE", "ADJUST", "DAMAGE", "LOST"
        reason: String,
        user: String
    ): Boolean {
        val material = dao.getMaterialById(materialId) ?: return false
        val diff = newQuantity - material.quantityInStock

        // Update quantity
        val updatedMaterial = material.copy(quantityInStock = newQuantity)
        dao.insertMaterial(updatedMaterial)

        // Save movement
        val movement = StockMovement(
            materialId = materialId,
            materialName = material.name,
            quantity = if (diff >= 0) diff else -diff,
            type = adjustmentType,
            reason = reason,
            user = user
        )
        dao.insertMovement(movement)
        return true
    }

    /**
     * Transfers materials from one cabinet to another.
     */
    suspend fun transferStock(
        materialId: Int,
        fromCabinetId: Int?,
        toCabinetId: Int,
        shelfPosition: String,
        quantity: Double,
        reason: String,
        user: String
    ): Boolean {
        val material = dao.getMaterialById(materialId) ?: return false
        val toCabinet = dao.getCabinetById(toCabinetId) ?: return false

        // In this simple inventory layout, a material has a single cabinet location.
        // If we transfer the whole stock or a portion, we update its cabinet link and position.
        val updatedMaterial = material.copy(
            cabinetId = toCabinetId,
            shelfPosition = shelfPosition
        )
        dao.insertMaterial(updatedMaterial)

        // Save movement
        val movement = StockMovement(
            materialId = materialId,
            materialName = material.name,
            quantity = quantity,
            type = "TRANSFER",
            reason = reason,
            user = user,
            fromCabinetId = fromCabinetId,
            toCabinetId = toCabinetId
        )
        dao.insertMovement(movement)
        return true
    }

    // --- Backup & Restore via JSON Serialization ---

    fun exportDatabaseAsJson(): String {
        val root = JSONObject()
        // We'll query our data in memory safely (since we have Flows or standard blocking/suspending calls)
        // Let's perform queries to populate JSON
        // Note: For convenience in this synchronous/blocking UI thread export,
        // we write helper converters. Since we can run on coroutines, let's return a JSON string.
        return "" // Will be populated in the ViewModel / Repository using Flow collections
    }

    suspend fun importDatabaseFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            // Import Cabinets
            if (root.has("cabinets")) {
                val cabs = root.getJSONArray("cabinets")
                for (i in 0 until cabs.length()) {
                    val c = cabs.getJSONObject(i)
                    dao.insertCabinet(Cabinet(
                        code = c.getString("code"),
                        description = c.getString("description"),
                        location = c.getString("location"),
                        photoUri = if (c.isNull("photoUri")) null else c.getString("photoUri")
                    ))
                }
            }

            // Import Categories
            if (root.has("categories")) {
                val cats = root.getJSONArray("categories")
                for (i in 0 until cats.length()) {
                    val c = cats.getJSONObject(i)
                    val name = c.getString("name")
                    if (dao.getCategoryByName(name) == null) {
                        dao.insertCategory(Category(name = name))
                    }
                }
            }

            // Import Projects
            if (root.has("projects")) {
                val projs = root.getJSONArray("projects")
                for (i in 0 until projs.length()) {
                    val p = projs.getJSONObject(i)
                    dao.insertProject(Project(
                        name = p.getString("name"),
                        clientName = p.getString("clientName"),
                        address = p.getString("address"),
                        startDate = p.getString("startDate"),
                        dueDate = p.getString("dueDate"),
                        status = p.getString("status"),
                        budget = p.getDouble("budget"),
                        notes = p.getString("notes")
                    ))
                }
            }

            // Import Materials
            if (root.has("materials")) {
                val mats = root.getJSONArray("materials")
                for (i in 0 until mats.length()) {
                    val m = mats.getJSONObject(i)
                    dao.insertMaterial(Material(
                        name = m.getString("name"),
                        category = m.getString("category"),
                        materialCode = m.getString("materialCode"),
                        barcode = if (m.isNull("barcode")) null else m.getString("barcode"),
                        unit = m.getString("unit"),
                        quantityInStock = m.getDouble("quantityInStock"),
                        minimumStockLevel = m.getDouble("minimumStockLevel"),
                        purchasePrice = m.getDouble("purchasePrice"),
                        sellingPrice = m.getDouble("sellingPrice"),
                        supplier = m.getString("supplier"),
                        datePurchased = m.getString("datePurchased"),
                        notes = m.getString("notes"),
                        photoUri = if (m.isNull("photoUri")) null else m.getString("photoUri"),
                        cabinetId = if (m.isNull("cabinetId")) null else m.getInt("cabinetId"),
                        shelfPosition = m.getString("shelfPosition")
                    ))
                }
            }

            // Import Movements
            if (root.has("movements")) {
                val movs = root.getJSONArray("movements")
                for (i in 0 until movs.length()) {
                    val mv = movs.getJSONObject(i)
                    dao.insertMovement(StockMovement(
                        materialId = mv.getInt("materialId"),
                        materialName = mv.getString("materialName"),
                        timestamp = mv.getLong("timestamp"),
                        quantity = mv.getDouble("quantity"),
                        type = mv.getString("type"),
                        reason = mv.getString("reason"),
                        user = mv.getString("user"),
                        projectId = if (mv.isNull("projectId")) null else mv.getInt("projectId"),
                        projectName = if (mv.isNull("projectName")) null else mv.getString("projectName"),
                        fromCabinetId = if (mv.isNull("fromCabinetId")) null else mv.getInt("fromCabinetId"),
                        toCabinetId = if (mv.isNull("toCabinetId")) null else mv.getInt("toCabinetId")
                    ))
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
