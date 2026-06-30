package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.InventoryDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Cabinet::class, Material::class, Category::class, Project::class, StockMovement::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glorious_homes_inventory_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.inventoryDao()
                    // Prepopulate default categories
                    val defaultCategories = listOf(
                        "Cement",
                        "Steel",
                        "Timber",
                        "Hardware",
                        "Plumbing",
                        "Electrical",
                        "Paint",
                        "Roofing",
                        "Masonry",
                        "Finishing",
                        "Tools",
                        "Safety Equipment"
                    )
                    defaultCategories.forEach { categoryName ->
                        dao.insertCategory(Category(name = categoryName))
                    }
                    
                    // Prepopulate some initial Cabinets
                    dao.insertCabinet(Cabinet(code = "Cabinet A1", description = "Main cabinet near entry", location = "Warehouse Section A"))
                    dao.insertCabinet(Cabinet(code = "Cabinet B2", description = "Hardware items rack", location = "Warehouse Section B"))
                    dao.insertCabinet(Cabinet(code = "Rack C3", description = "Heavy metal and pipe storage", location = "Warehouse Section C"))

                    // Prepopulate some initial Projects
                    dao.insertProject(Project(
                        name = "Orchid Towers Phase 1",
                        clientName = "Prime Developers Ltd",
                        address = "45 Orchid Way, Colombo 03",
                        startDate = "2026-07-01",
                        dueDate = "2027-12-31",
                        status = "Planned",
                        budget = 150000.0,
                        notes = "High-rise construction project with strict structural compliance."
                    ))
                    dao.insertProject(Project(
                        name = "Emerald Villas",
                        clientName = "Mr. & Mrs. Silva",
                        address = "12 Lake Road, Kandy",
                        startDate = "2026-05-15",
                        dueDate = "2026-11-30",
                        status = "In Progress",
                        budget = 75000.0,
                        notes = "Luxury housing project."
                    ))

                    // Prepopulate some initial Materials
                    dao.insertMaterial(Material(
                        name = "Premium Portland Cement",
                        category = "Cement",
                        materialCode = "MAT-0001",
                        barcode = "5012345678901",
                        unit = "bags",
                        quantityInStock = 120.0,
                        minimumStockLevel = 20.0,
                        purchasePrice = 12.50,
                        sellingPrice = 15.00,
                        supplier = "UltraTech Cement",
                        datePurchased = "2026-06-20",
                        notes = "Store in dry place. Do not stack more than 10 bags high.",
                        cabinetId = 1,
                        shelfPosition = "Pallet 1"
                    ))
                    dao.insertMaterial(Material(
                        name = "2-Inch Galvanized Nails",
                        category = "Hardware",
                        materialCode = "MAT-0002",
                        barcode = "5012345678912",
                        unit = "pcs",
                        quantityInStock = 450.0,
                        minimumStockLevel = 50.0,
                        purchasePrice = 0.05,
                        sellingPrice = 0.08,
                        supplier = "Local Hardware Supplies",
                        datePurchased = "2026-06-25",
                        notes = "High durability steel nails.",
                        cabinetId = 2,
                        shelfPosition = "Shelf 3"
                    ))
                    dao.insertMaterial(Material(
                        name = "Steel Reinforcement Bar 12mm",
                        category = "Steel",
                        materialCode = "MAT-0003",
                        barcode = "5012345678923",
                        unit = "meters",
                        quantityInStock = 15.0,
                        minimumStockLevel = 50.0, // Low stock on purpose for testing UI
                        purchasePrice = 3.50,
                        sellingPrice = 4.50,
                        supplier = "Lanka Steel Corp",
                        datePurchased = "2026-06-22",
                        notes = "Structural grade.",
                        cabinetId = 3,
                        shelfPosition = "Bay 2"
                    ))
                    dao.insertMaterial(Material(
                        name = "Matte White Acrylic Paint 10L",
                        category = "Paint",
                        materialCode = "MAT-0004",
                        barcode = "5012345678934",
                        unit = "liters",
                        quantityInStock = 0.0, // Out of stock on purpose for testing UI
                        minimumStockLevel = 10.0,
                        purchasePrice = 45.00,
                        sellingPrice = 55.00,
                        supplier = "Dulux Paints",
                        datePurchased = "2026-06-18",
                        notes = "White wall paint.",
                        cabinetId = 2,
                        shelfPosition = "Shelf 1"
                    ))
                }
            }
        }
    }
}
