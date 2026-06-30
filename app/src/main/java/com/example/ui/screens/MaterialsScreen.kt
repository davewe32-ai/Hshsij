package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Cabinet
import com.example.data.entity.Category
import com.example.data.entity.Material
import com.example.ui.theme.*
import com.example.ui.viewmodel.InventoryViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val materials by viewModel.filteredMaterials.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cabinets by viewModel.cabinets.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    var activeCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Dialog flags
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedMaterialForEdit by remember { mutableStateOf<Material?>(null) }
    var showAdjustDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var selectedMaterialForAction by remember { mutableStateOf<Material?>(null) }

    // Forms State
    var matName by remember { mutableStateOf("") }
    var matCategory by remember { mutableStateOf("") }
    var matBarcode by remember { mutableStateOf("") }
    var matUnit by remember { mutableStateOf("pcs") }
    var matQty by remember { mutableStateOf("") }
    var matMinStock by remember { mutableStateOf("") }
    var matPurchasePrice by remember { mutableStateOf("") }
    var matSellingPrice by remember { mutableStateOf("") }
    var matSupplier by remember { mutableStateOf("") }
    var matNotes by remember { mutableStateOf("") }
    var matCabinetId by remember { mutableStateOf<Int?>(null) }
    var matShelf by remember { mutableStateOf("") }

    // Quick Adjustment forms
    var adjustQty by remember { mutableStateOf("") }
    var adjustType by remember { mutableStateOf("ADJUST") } // ADD, REMOVE, DAMAGE, LOST
    var adjustReason by remember { mutableStateOf("") }

    // Transfer Stock form
    var transferToCabinetId by remember { mutableStateOf<Int?>(null) }
    var transferShelf by remember { mutableStateOf("") }
    var transferReason by remember { mutableStateOf("Relocation") }

    val context = LocalContext.current
    val moneyFormat = DecimalFormat("$#,##0.00")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
    ) {
        // Toolbar
        TopAppBar(
            title = { Text("Material Inventory", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
            actions = {
                Button(
                    onClick = {
                        selectedMaterialForEdit = null
                        matName = ""
                        matCategory = if (categories.isNotEmpty()) categories.first().name else "Cement"
                        matBarcode = ""
                        matUnit = "pcs"
                        matQty = ""
                        matMinStock = ""
                        matPurchasePrice = ""
                        matSellingPrice = ""
                        matSupplier = ""
                        matNotes = ""
                        matCabinetId = if (cabinets.isNotEmpty()) cabinets.first().id else null
                        matShelf = "Shelf 1"
                        showAddEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_material_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Material", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Material", color = Color.White)
                }
            }
        )

        // Category scrollable filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeCategoryFilter == null,
                onClick = { activeCategoryFilter = null },
                label = { Text("All Categories") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SkyBlue,
                    selectedLabelColor = Color.White
                )
            )

            categories.forEach { category ->
                FilterChip(
                    selected = activeCategoryFilter == category.name,
                    onClick = { activeCategoryFilter = category.name },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SkyBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // List
        val filteredList = if (activeCategoryFilter == null) {
            materials
        } else {
            materials.filter { it.category.equals(activeCategoryFilter, ignoreCase = true) }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = "No Materials",
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No Materials Found", fontWeight = FontWeight.Bold, color = TextCharcoal)
                            Text("Click 'Add Material' or clear your filters.", color = TextMediumGray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(filteredList) { material ->
                    val cabinetObj = cabinets.find { it.id == material.cabinetId }
                    val cabinetCode = cabinetObj?.code ?: "No Cabinet"

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = material.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextCharcoal
                                    )
                                    Text(
                                        text = "Code: ${material.materialCode} • Barcode: ${material.barcode ?: "None"}",
                                        fontSize = 11.sp,
                                        color = TextMediumGray
                                    )
                                }

                                // Stock badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when {
                                                material.quantityInStock == 0.0 -> ErrorRed.copy(alpha = 0.15f)
                                                material.quantityInStock <= material.minimumStockLevel -> WarningGold.copy(alpha = 0.15f)
                                                else -> SuccessGreen.copy(alpha = 0.15f)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${material.quantityInStock} ${material.unit}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = when {
                                            material.quantityInStock == 0.0 -> ErrorRed
                                            material.quantityInStock <= material.minimumStockLevel -> WarningGold
                                            else -> SuccessGreen
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = SurfaceLightGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Storage Info Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AllInbox, contentDescription = "Cabinet", tint = SkyBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$cabinetCode → ${material.shelfPosition}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextCharcoal
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(HighlightLightBlue, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(material.category, fontSize = 11.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Pricing Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Cost: ${moneyFormat.format(material.purchasePrice)}", fontSize = 12.sp, color = TextMediumGray)
                                Text("Value: ${moneyFormat.format(material.sellingPrice)}", fontSize = 12.sp, color = TextMediumGray)
                                Text("Supplier: ${material.supplier}", fontSize = 12.sp, color = TextMediumGray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            }

                            if (material.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Notes: ${material.notes}", fontSize = 11.sp, color = TextMediumGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = SurfaceLightGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Adjust Stock Button
                                    IconButton(
                                        onClick = {
                                            selectedMaterialForAction = material
                                            adjustQty = material.quantityInStock.toString()
                                            adjustType = "ADJUST"
                                            adjustReason = "Manual inventory adjustment"
                                            showAdjustDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = "Adjust Stock", tint = AccentOrange, modifier = Modifier.size(18.dp))
                                    }

                                    // Transfer Cabinet Button
                                    IconButton(
                                        onClick = {
                                            selectedMaterialForAction = material
                                            transferToCabinetId = material.cabinetId ?: (if (cabinets.isNotEmpty()) cabinets.first().id else null)
                                            transferShelf = material.shelfPosition
                                            transferReason = "Relocation"
                                            showTransferDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer Cabinet", tint = SkyBlue, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            selectedMaterialForEdit = material
                                            matName = material.name
                                            matCategory = material.category
                                            matBarcode = material.barcode ?: ""
                                            matUnit = material.unit
                                            matQty = material.quantityInStock.toString()
                                            matMinStock = material.minimumStockLevel.toString()
                                            matPurchasePrice = material.purchasePrice.toString()
                                            matSellingPrice = material.sellingPrice.toString()
                                            matSupplier = material.supplier
                                            matNotes = material.notes
                                            matCabinetId = material.cabinetId
                                            matShelf = material.shelfPosition
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyBlue, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            if (currentUserRole == "Administrator") {
                                                viewModel.deleteMaterial(material)
                                            } else {
                                                Toast.makeText(context, "Access Denied: Only Administrators can delete materials.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Material Dialog
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (selectedMaterialForEdit == null) "Add Material Category" else "Edit Material", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = matName,
                        onValueChange = { matName = it },
                        label = { Text("Material Name (e.g. Nails 2-inch)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Dropdown
                    var expandedCat by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = matCategory,
                            onValueChange = {},
                            label = { Text("Category") },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedCat = !expandedCat }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCat = true }
                        )
                        DropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        matCategory = cat.name
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }

                    // Barcode Support with Simulated Camera button
                    OutlinedTextField(
                        value = matBarcode,
                        onValueChange = { matBarcode = it },
                        label = { Text("Barcode / QR Code String") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                // Simulate QR/Barcode scan
                                val simulatedCode = "SCAN-" + System.currentTimeMillis().toString().takeLast(6)
                                matBarcode = simulatedCode
                                Toast.makeText(context, "QR Code Scanned successfully!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR Code", tint = SkyBlue)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = matUnit,
                            onValueChange = { matUnit = it },
                            label = { Text("Unit (pcs, bags, kg)") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = matQty,
                            onValueChange = { matQty = it },
                            label = { Text("Initial Stock") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = matMinStock,
                            onValueChange = { matMinStock = it },
                            label = { Text("Min Stock Alarm") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = matPurchasePrice,
                            onValueChange = { matPurchasePrice = it },
                            label = { Text("Purchase Price ($)") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = matSellingPrice,
                            onValueChange = { matSellingPrice = it },
                            label = { Text("Selling Price ($)") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = matSupplier,
                            onValueChange = { matSupplier = it },
                            label = { Text("Supplier Name") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Cabinet Select Dropdown
                    var expandedCab by remember { mutableStateOf(false) }
                    val currentCabinetCode = cabinets.find { it.id == matCabinetId }?.code ?: "No Cabinet Assigned"
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentCabinetCode,
                            onValueChange = {},
                            label = { Text("Storage Cabinet") },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedCab = !expandedCab }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCab = true }
                        )
                        DropdownMenu(
                            expanded = expandedCab,
                            onDismissRequest = { expandedCab = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            cabinets.forEach { cab ->
                                DropdownMenuItem(
                                    text = { Text("${cab.code} (${cab.location})") },
                                    onClick = {
                                        matCabinetId = cab.id
                                        expandedCab = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = matShelf,
                        onValueChange = { matShelf = it },
                        label = { Text("Shelf / Position inside Cabinet") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = matNotes,
                        onValueChange = { matNotes = it },
                        label = { Text("Notes / Handling Guidelines") },
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qtyVal = matQty.toDoubleOrNull() ?: 0.0
                        val minVal = matMinStock.toDoubleOrNull() ?: 0.0
                        val buyPrice = matPurchasePrice.toDoubleOrNull() ?: 0.0
                        val sellPrice = matSellingPrice.toDoubleOrNull() ?: 0.0

                        if (matName.isBlank()) {
                            Toast.makeText(context, "Please enter material name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedMaterialForEdit == null) {
                            viewModel.addMaterial(
                                name = matName.trim(),
                                category = matCategory,
                                barcode = matBarcode.ifBlank { null },
                                unit = matUnit,
                                quantity = qtyVal,
                                minStock = minVal,
                                purchasePrice = buyPrice,
                                sellingPrice = sellPrice,
                                supplier = matSupplier.trim(),
                                notes = matNotes.trim(),
                                photoUri = null,
                                cabinetId = matCabinetId,
                                shelfPosition = matShelf.trim()
                            )
                        } else {
                            viewModel.editMaterial(
                                id = selectedMaterialForEdit!!.id,
                                name = matName.trim(),
                                category = matCategory,
                                materialCode = selectedMaterialForEdit!!.materialCode,
                                barcode = matBarcode.ifBlank { null },
                                unit = matUnit,
                                quantity = qtyVal,
                                minStock = minVal,
                                purchasePrice = buyPrice,
                                sellingPrice = sellPrice,
                                supplier = matSupplier.trim(),
                                datePurchased = selectedMaterialForEdit!!.datePurchased,
                                notes = matNotes.trim(),
                                photoUri = selectedMaterialForEdit!!.photoUri,
                                cabinetId = matCabinetId,
                                shelfPosition = matShelf.trim()
                            )
                        }
                        showAddEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancel", color = SkyBlue)
                }
            }
        )
    }

    // Stock Adjust Dialog
    if (showAdjustDialog && selectedMaterialForAction != null) {
        val mat = selectedMaterialForAction!!
        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = { Text("Adjust Stock: ${mat.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Current stock quantity: ${mat.quantityInStock} ${mat.unit}", fontSize = 13.sp, color = TextMediumGray)

                    OutlinedTextField(
                        value = adjustQty,
                        onValueChange = { adjustQty = it },
                        label = { Text("New Stock Quantity") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type dropdown
                    var expandedType by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = adjustType,
                            onValueChange = {},
                            label = { Text("Adjustment Type") },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedType = !expandedType }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedType = true }
                        )
                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("ADJUST", "ADD", "REMOVE", "DAMAGE", "LOST").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        adjustType = type
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = adjustReason,
                        onValueChange = { adjustReason = it },
                        label = { Text("Adjustment Reason") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qtyDouble = adjustQty.toDoubleOrNull()
                        if (qtyDouble == null || qtyDouble < 0.0) {
                            Toast.makeText(context, "Please enter valid stock quantity", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (adjustReason.isBlank()) {
                            Toast.makeText(context, "Please specify an adjustment reason", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.adjustStockQuantity(
                            materialId = mat.id,
                            newQuantity = qtyDouble,
                            type = adjustType,
                            reason = adjustReason.trim()
                        )
                        showAdjustDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Adjust", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = false }) {
                    Text("Cancel", color = SkyBlue)
                }
            }
        )
    }

    // Cabinet Transfer Dialog
    if (showTransferDialog && selectedMaterialForAction != null) {
        val mat = selectedMaterialForAction!!
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Transfer Cabinet: ${mat.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Relocate this material category to another rack/cabinet.", fontSize = 12.sp, color = TextMediumGray)

                    // Target Cabinet dropdown
                    var expandedTargetCab by remember { mutableStateOf(false) }
                    val targetCabCode = cabinets.find { it.id == transferToCabinetId }?.code ?: "Choose Cabinet"
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetCabCode,
                            onValueChange = {},
                            label = { Text("Transfer To Cabinet") },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedTargetCab = !expandedTargetCab }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedTargetCab = true }
                        )
                        DropdownMenu(
                            expanded = expandedTargetCab,
                            onDismissRequest = { expandedTargetCab = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            cabinets.forEach { cab ->
                                DropdownMenuItem(
                                    text = { Text("${cab.code} (${cab.location})") },
                                    onClick = {
                                        transferToCabinetId = cab.id
                                        expandedTargetCab = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = transferShelf,
                        onValueChange = { transferShelf = it },
                        label = { Text("New Shelf Position") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = transferReason,
                        onValueChange = { transferReason = it },
                        label = { Text("Relocation Reason") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cabId = transferToCabinetId
                        if (cabId == null) {
                            Toast.makeText(context, "Please select target cabinet", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (transferShelf.isBlank()) {
                            Toast.makeText(context, "Please enter shelf position", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.transferMaterial(
                            materialId = mat.id,
                            toCabinetId = cabId,
                            shelfPosition = transferShelf.trim(),
                            quantity = mat.quantityInStock, // transferring entire existing batch
                            reason = transferReason.trim()
                        )
                        showTransferDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Transfer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("Cancel", color = SkyBlue)
                }
            }
        )
    }
}
