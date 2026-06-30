package com.example.ui.viewmodel

import android.app.Application
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    
    // User accounts and security roles
    val userRoles = listOf("Administrator", "Store Keeper", "Manager")
    private val _currentUserRole = MutableStateFlow("Administrator")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    // Active Navigation Screen: "dashboard", "inventory", "cabinets", "projects", "categories", "reports", "settings"
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Global Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Flows from repository
    val cabinets: StateFlow<List<Cabinet>>
    val materials: StateFlow<List<Material>>
    val categories: StateFlow<List<Category>>
    val projects: StateFlow<List<Project>>
    val movements: StateFlow<List<StockMovement>>

    // Metrics
    val cabinetsCount: StateFlow<Int>
    val materialsCount: StateFlow<Int>
    val lowStockCount: StateFlow<Int>
    val outOfStockCount: StateFlow<Int>
    val movementsTodayCount: StateFlow<Int>

    // Alert lists
    val lowStockMaterials: StateFlow<List<Material>>
    val outOfStockMaterials: StateFlow<List<Material>>

    // Search filter lists
    val filteredMaterials: StateFlow<List<Material>>

    // Status Message for Actions (e.g. Success, Error)
    private val _actionStatus = MutableStateFlow<String?>(null)
    val actionStatus: StateFlow<String?> = _actionStatus.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = InventoryRepository(database.inventoryDao())

        cabinets = repository.allCabinets.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        materials = repository.allMaterials.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        projects = repository.allProjects.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        movements = repository.allMovements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        cabinetsCount = repository.cabinetsCount.stateIn(viewModelScope, SharingStarted.Lazily, 0)
        materialsCount = repository.materialsCount.stateIn(viewModelScope, SharingStarted.Lazily, 0)
        lowStockCount = repository.lowStockCount.stateIn(viewModelScope, SharingStarted.Lazily, 0)
        outOfStockCount = repository.outOfStockCount.stateIn(viewModelScope, SharingStarted.Lazily, 0)
        movementsTodayCount = repository.getMovementsToday().stateIn(viewModelScope, SharingStarted.Lazily, 0)

        lowStockMaterials = repository.lowStockMaterials.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        outOfStockMaterials = repository.outOfStockMaterials.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Search logic across name, code, barcode, category, cabinet, supplier
        filteredMaterials = combine(materials, searchQuery, cabinets) { mats, query, cabs ->
            if (query.isBlank()) {
                mats
            } else {
                val q = query.lowercase().trim()
                mats.filter { mat ->
                    val cabinetCode = cabs.find { it.id == mat.cabinetId }?.code?.lowercase() ?: ""
                    mat.name.lowercase().contains(q) ||
                            mat.materialCode.lowercase().contains(q) ||
                            (mat.barcode?.lowercase()?.contains(q) ?: false) ||
                            mat.category.lowercase().contains(q) ||
                            mat.supplier.lowercase().contains(q) ||
                            cabinetCode.contains(q) ||
                            mat.shelfPosition.lowercase().contains(q)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun setUserRole(role: String) {
        _currentUserRole.value = role
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearStatus() {
        _actionStatus.value = null
    }

    // Role safety utility
    private fun isAdministrator(): Boolean {
        return _currentUserRole.value == "Administrator"
    }

    // --- Cabinets Actions ---

    fun addCabinet(code: String, description: String, location: String, photoUri: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val cabinet = Cabinet(code = code, description = description, location = location, photoUri = photoUri)
            repository.insertCabinet(cabinet)
            _actionStatus.value = "Cabinet '$code' created successfully."
        }
    }

    fun editCabinet(id: Int, code: String, description: String, location: String, photoUri: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val cabinet = Cabinet(id = id, code = code, description = description, location = location, photoUri = photoUri)
            repository.insertCabinet(cabinet)
            _actionStatus.value = "Cabinet '$code' updated successfully."
        }
    }

    fun deleteCabinet(cabinet: Cabinet) {
        if (!isAdministrator()) {
            _actionStatus.value = "Access Denied: Only Administrators can delete records."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCabinet(cabinet)
            _actionStatus.value = "Cabinet '${cabinet.code}' deleted successfully."
        }
    }


    // --- Materials Actions ---

    fun addMaterial(
        name: String,
        category: String,
        barcode: String?,
        unit: String,
        quantity: Double,
        minStock: Double,
        purchasePrice: Double,
        sellingPrice: Double,
        supplier: String,
        notes: String,
        photoUri: String?,
        cabinetId: Int?,
        shelfPosition: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Generate material code based on timestamp or sequential
            val codeSuffix = System.currentTimeMillis().toString().takeLast(4)
            val generatedCode = "MAT-$codeSuffix"

            val material = Material(
                name = name,
                category = category,
                materialCode = generatedCode,
                barcode = barcode,
                unit = unit,
                quantityInStock = quantity,
                minimumStockLevel = minStock,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                supplier = supplier,
                datePurchased = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                notes = notes,
                photoUri = photoUri,
                cabinetId = cabinetId,
                shelfPosition = shelfPosition
            )
            val id = repository.insertMaterial(material)

            // Register initial stock addition movement
            val movement = StockMovement(
                materialId = id,
                materialName = name,
                quantity = quantity,
                type = "ADD",
                reason = "Initial inventory registration",
                user = _currentUserRole.value
            )
            repository.insertMovement(movement)
            _actionStatus.value = "Material '$name' created successfully as $generatedCode."
        }
    }

    fun editMaterial(
        id: Int,
        name: String,
        category: String,
        materialCode: String,
        barcode: String?,
        unit: String,
        quantity: Double,
        minStock: Double,
        purchasePrice: Double,
        sellingPrice: Double,
        supplier: String,
        datePurchased: String,
        notes: String,
        photoUri: String?,
        cabinetId: Int?,
        shelfPosition: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldMaterial = repository.getMaterialById(id)
            val currentQty = oldMaterial?.quantityInStock ?: 0.0

            val material = Material(
                id = id,
                name = name,
                category = category,
                materialCode = materialCode,
                barcode = barcode,
                unit = unit,
                quantityInStock = quantity,
                minimumStockLevel = minStock,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                supplier = supplier,
                datePurchased = datePurchased,
                notes = notes,
                photoUri = photoUri,
                cabinetId = cabinetId,
                shelfPosition = shelfPosition
            )
            repository.insertMaterial(material)

            // Register adjustment if quantity changed
            if (currentQty != quantity) {
                val diff = quantity - currentQty
                val movement = StockMovement(
                    materialId = id,
                    materialName = name,
                    quantity = if (diff >= 0) diff else -diff,
                    type = "ADJUST",
                    reason = "Manual stock quantity adjustment",
                    user = _currentUserRole.value
                )
                repository.insertMovement(movement)
            }
            _actionStatus.value = "Material '$name' updated successfully."
        }
    }

    fun deleteMaterial(material: Material) {
        if (!isAdministrator()) {
            _actionStatus.value = "Access Denied: Only Administrators can delete records."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMaterial(material)
            _actionStatus.value = "Material '${material.name}' deleted successfully."
        }
    }


    // --- Categories Actions ---

    fun addCategory(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanName = name.trim()
            if (cleanName.isEmpty()) return@launch
            if (repository.getCategoryByName(cleanName) != null) {
                _actionStatus.value = "Category '$cleanName' already exists."
                return@launch
            }
            repository.insertCategory(Category(name = cleanName))
            _actionStatus.value = "Category '$cleanName' created successfully."
        }
    }

    fun deleteCategory(category: Category) {
        if (!isAdministrator()) {
            _actionStatus.value = "Access Denied: Only Administrators can delete records."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
            _actionStatus.value = "Category '${category.name}' deleted successfully."
        }
    }


    // --- Projects Actions ---

    fun addProject(
        name: String,
        clientName: String,
        address: String,
        startDate: String,
        dueDate: String,
        status: String,
        budget: Double,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = Project(
                name = name,
                clientName = clientName,
                address = address,
                startDate = startDate,
                dueDate = dueDate,
                status = status,
                budget = budget,
                notes = notes
            )
            repository.insertProject(project)
            _actionStatus.value = "Project '$name' created successfully."
        }
    }

    fun editProject(
        id: Int,
        name: String,
        clientName: String,
        address: String,
        startDate: String,
        dueDate: String,
        status: String,
        budget: Double,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = Project(
                id = id,
                name = name,
                clientName = clientName,
                address = address,
                startDate = startDate,
                dueDate = dueDate,
                status = status,
                budget = budget,
                notes = notes
            )
            repository.insertProject(project)
            _actionStatus.value = "Project '$name' updated successfully."
        }
    }

    fun deleteProject(project: Project) {
        if (!isAdministrator()) {
            _actionStatus.value = "Access Denied: Only Administrators can delete records."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProject(project)
            _actionStatus.value = "Project '${project.name}' deleted successfully."
        }
    }


    // --- Complex Stock Management Operations ---

    fun issueMaterial(materialId: Int, projectId: Int, quantity: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.issueMaterialToProject(
                materialId = materialId,
                projectId = projectId,
                quantity = quantity,
                reason = "Issued to Project",
                user = _currentUserRole.value,
                notes = notes
            )
            if (success) {
                _actionStatus.value = "Successfully issued $quantity of material to project."
            } else {
                _actionStatus.value = "Error issuing material: Check stock availability."
            }
        }
    }

    fun adjustStockQuantity(materialId: Int, newQuantity: Double, type: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.adjustStock(
                materialId = materialId,
                newQuantity = newQuantity,
                adjustmentType = type, // "ADD", "REMOVE", "ADJUST", "DAMAGE", "LOST"
                reason = reason,
                user = _currentUserRole.value
            )
            if (success) {
                _actionStatus.value = "Stock adjusted successfully."
            } else {
                _actionStatus.value = "Error adjusting stock."
            }
        }
    }

    fun transferMaterial(materialId: Int, toCabinetId: Int, shelfPosition: String, quantity: Double, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val material = repository.getMaterialById(materialId)
            val success = repository.transferStock(
                materialId = materialId,
                fromCabinetId = material?.cabinetId,
                toCabinetId = toCabinetId,
                shelfPosition = shelfPosition,
                quantity = quantity,
                reason = reason,
                user = _currentUserRole.value
            )
            if (success) {
                _actionStatus.value = "Material transferred successfully."
            } else {
                _actionStatus.value = "Error transferring material."
            }
        }
    }

    // --- JSON Backup / Restore ---

    fun generateBackupJson(onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = JSONObject()

                // Cabinets
                val cabsArray = JSONArray()
                cabinets.value.forEach {
                    val jobj = JSONObject()
                    jobj.put("code", it.code)
                    jobj.put("description", it.description)
                    jobj.put("location", it.location)
                    jobj.put("photoUri", it.photoUri ?: JSONObject.NULL)
                    cabsArray.put(jobj)
                }
                root.put("cabinets", cabsArray)

                // Categories
                val catsArray = JSONArray()
                categories.value.forEach {
                    val jobj = JSONObject()
                    jobj.put("name", it.name)
                    catsArray.put(jobj)
                }
                root.put("categories", catsArray)

                // Projects
                val projsArray = JSONArray()
                projects.value.forEach {
                    val jobj = JSONObject()
                    jobj.put("name", it.name)
                    jobj.put("clientName", it.clientName)
                    jobj.put("address", it.address)
                    jobj.put("startDate", it.startDate)
                    jobj.put("dueDate", it.dueDate)
                    jobj.put("status", it.status)
                    jobj.put("budget", it.budget)
                    jobj.put("notes", it.notes)
                    projsArray.put(jobj)
                }
                root.put("projects", projsArray)

                // Materials
                val matsArray = JSONArray()
                materials.value.forEach {
                    val jobj = JSONObject()
                    jobj.put("name", it.name)
                    jobj.put("category", it.category)
                    jobj.put("materialCode", it.materialCode)
                    jobj.put("barcode", it.barcode ?: JSONObject.NULL)
                    jobj.put("unit", it.unit)
                    jobj.put("quantityInStock", it.quantityInStock)
                    jobj.put("minimumStockLevel", it.minimumStockLevel)
                    jobj.put("purchasePrice", it.purchasePrice)
                    jobj.put("sellingPrice", it.sellingPrice)
                    jobj.put("supplier", it.supplier)
                    jobj.put("datePurchased", it.datePurchased)
                    jobj.put("notes", it.notes)
                    jobj.put("photoUri", it.photoUri ?: JSONObject.NULL)
                    jobj.put("cabinetId", it.cabinetId ?: JSONObject.NULL)
                    jobj.put("shelfPosition", it.shelfPosition)
                    matsArray.put(jobj)
                }
                root.put("materials", matsArray)

                // Movements
                val movsArray = JSONArray()
                movements.value.forEach {
                    val jobj = JSONObject()
                    jobj.put("materialId", it.materialId)
                    jobj.put("materialName", it.materialName)
                    jobj.put("timestamp", it.timestamp)
                    jobj.put("quantity", it.quantity)
                    jobj.put("type", it.type)
                    jobj.put("reason", it.reason)
                    jobj.put("user", it.user)
                    jobj.put("projectId", it.projectId ?: JSONObject.NULL)
                    jobj.put("projectName", it.projectName ?: JSONObject.NULL)
                    jobj.put("fromCabinetId", it.fromCabinetId ?: JSONObject.NULL)
                    jobj.put("toCabinetId", it.toCabinetId ?: JSONObject.NULL)
                    movsArray.put(jobj)
                }
                root.put("movements", movsArray)

                onResult(root.toString(4))
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun restoreBackupJson(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.importDatabaseFromJson(jsonString)
            if (success) {
                _actionStatus.value = "Backup restored successfully."
            } else {
                _actionStatus.value = "Failed to restore backup: Invalid JSON format."
            }
            onResult(success)
        }
    }
}
