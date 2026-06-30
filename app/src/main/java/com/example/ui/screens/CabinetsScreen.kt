package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.entity.Material
import com.example.ui.theme.*
import com.example.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabinetsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val cabinets by viewModel.cabinets.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedCabinetForEdit by remember { mutableStateOf<Cabinet?>(null) }

    // Dialog form state
    var cabinetCode by remember { mutableStateOf("") }
    var cabinetDesc by remember { mutableStateOf("") }
    var cabinetLoc by remember { mutableStateOf("") }

    // View cabinet items sub-panel
    var selectedCabinetToViewItems by remember { mutableStateOf<Cabinet?>(null) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
    ) {
        // Toolbar
        TopAppBar(
            title = { Text("Storage Cabinets & Racks", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
            actions = {
                Button(
                    onClick = {
                        selectedCabinetForEdit = null
                        cabinetCode = ""
                        cabinetDesc = ""
                        cabinetLoc = ""
                        showAddEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_cabinet_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Cabinet", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Cabinet", color = Color.White)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (cabinets.isEmpty()) {
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
                                Icons.Default.AllInbox,
                                contentDescription = "No Cabinets",
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No Cabinets Created Yet", fontWeight = FontWeight.Bold, color = TextCharcoal)
                            Text("Click 'Add Cabinet' to register your first storage unit.", color = TextMediumGray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(cabinets) { cabinet ->
                    val materialsInThisCabinet = materials.filter { it.cabinetId == cabinet.id }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCabinetToViewItems = if (selectedCabinetToViewItems?.id == cabinet.id) null else cabinet
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(HighlightLightBlue, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AllInbox, contentDescription = null, tint = SkyBlue)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = cabinet.code,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextCharcoal
                                        )
                                        Text(
                                            text = cabinet.location,
                                            fontSize = 12.sp,
                                            color = TextMediumGray
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = {
                                        selectedCabinetForEdit = cabinet
                                        cabinetCode = cabinet.code
                                        cabinetDesc = cabinet.description
                                        cabinetLoc = cabinet.location
                                        showAddEditDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Cabinet", tint = SkyBlue)
                                    }

                                    IconButton(onClick = {
                                        if (currentUserRole == "Administrator") {
                                            viewModel.deleteCabinet(cabinet)
                                        } else {
                                            Toast.makeText(context, "Access Denied: Only Administrators can delete cabinets.", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Cabinet", tint = ErrorRed)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = cabinet.description,
                                fontSize = 13.sp,
                                color = TextCharcoal
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${materialsInThisCabinet.size} material categories inside",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = SkyBlue
                                )
                                Icon(
                                    imageVector = if (selectedCabinetToViewItems?.id == cabinet.id) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextMediumGray
                                )
                            }

                            // Expandable contents
                            AnimatedVisibility(visible = selectedCabinetToViewItems?.id == cabinet.id) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .background(SurfaceLightGray, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Stored Materials:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = NavyBlue,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    if (materialsInThisCabinet.isEmpty()) {
                                        Text("No materials stored in this cabinet yet.", fontSize = 12.sp, color = TextMediumGray)
                                    } else {
                                        materialsInThisCabinet.forEach { mat ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = mat.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextCharcoal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row {
                                                    Text(
                                                        text = "Shelf ${mat.shelfPosition}",
                                                        fontSize = 11.sp,
                                                        color = TextMediumGray,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                    Text(
                                                        text = "${mat.quantityInStock} ${mat.unit}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (mat.quantityInStock == 0.0) ErrorRed else SuccessGreen
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Cabinet Dialog
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (selectedCabinetForEdit == null) "Add Storage Cabinet" else "Edit Cabinet",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = cabinetCode,
                        onValueChange = { cabinetCode = it },
                        label = { Text("Cabinet Code (e.g., Cabinet A1)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cabinetLoc,
                        onValueChange = { cabinetLoc = it },
                        label = { Text("Location (e.g., Warehouse Section A)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cabinetDesc,
                        onValueChange = { cabinetDesc = it },
                        label = { Text("Cabinet Description") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cabinetCode.isBlank() || cabinetLoc.isBlank()) {
                            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedCabinetForEdit == null) {
                            viewModel.addCabinet(
                                code = cabinetCode.trim(),
                                description = cabinetDesc.trim(),
                                location = cabinetLoc.trim()
                            )
                        } else {
                            viewModel.editCabinet(
                                id = selectedCabinetForEdit!!.id,
                                code = cabinetCode.trim(),
                                description = cabinetDesc.trim(),
                                location = cabinetLoc.trim()
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
}
