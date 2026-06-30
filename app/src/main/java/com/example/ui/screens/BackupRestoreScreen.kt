package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val materialsCount by viewModel.materialsCount.collectAsState()
    val cabinetsCount by viewModel.cabinetsCount.collectAsState()
    val projectsCount by viewModel.projects.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var generatedBackupText by remember { mutableStateOf("") }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreInputText by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
    ) {
        TopAppBar(
            title = { Text("Settings & Data Management", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Active User Accounts & Security Roles
            Text(
                text = "User Accounts & Security Roles",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyBlue
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Active Role",
                        fontSize = 11.sp,
                        color = TextMediumGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(HighlightLightBlue, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (currentUserRole) {
                                        "Administrator" -> Icons.Default.AdminPanelSettings
                                        "Store Keeper" -> Icons.Default.Storefront
                                        else -> Icons.Default.Engineering
                                    },
                                    contentDescription = null,
                                    tint = SkyBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUserRole,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextCharcoal
                                )
                                Text(
                                    text = when (currentUserRole) {
                                        "Administrator" -> "Full write, edit, delete, and database reset authorization."
                                        "Store Keeper" -> "Warehouse intake and basic updates. No deletion permissions."
                                        else -> "General manager, reports overview, and material issues. No deletion."
                                    },
                                    fontSize = 12.sp,
                                    color = TextMediumGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = SurfaceLightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Switch Role to Test Controls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextCharcoal,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Role switch buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.userRoles.forEach { role ->
                            val isSelected = currentUserRole == role
                            Button(
                                onClick = {
                                    viewModel.setUserRole(role)
                                    Toast.makeText(context, "Role switched to $role", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SkyBlue else SurfaceLightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("role_switch_to_${role.lowercase().replace(" ", "_")}")
                            ) {
                                Text(
                                    text = role,
                                    color = if (isSelected) Color.White else TextCharcoal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Data Backup & Restore
            Text(
                text = "Offline Database Backup & Restore",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyBlue
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Glorious Homes runs fully offline to ensure perfect performance on-site. Backups are exported as fully serialized offline JSON strings which can be securely stored or restored on any device.",
                        fontSize = 12.sp,
                        color = TextMediumGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Export Database Button
                        Button(
                            onClick = {
                                viewModel.generateBackupJson { backup ->
                                    if (backup != null) {
                                        generatedBackupText = backup
                                        showBackupDialog = true
                                    } else {
                                        Toast.makeText(context, "Failed to generate database backup.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Export Backup", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Import Database Button
                        Button(
                            onClick = {
                                restoreInputText = ""
                                showRestoreDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_backup_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Import Backup", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 3: Future Scalability Roadmap
            Text(
                text = "Enterprise Scalability Roadmap",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyBlue
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "This application is designed to support thousands of inventory rows and cabinets. The database layer is prepared for direct sync with enterprise web portals and future expansions.",
                        fontSize = 12.sp,
                        color = TextMediumGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    RoadmapItem("Employee & Shift Tracking", "Pre-wired for secure storekeeper logs and shift verification.")
                    RoadmapItem("Equipment & Vehicle Fleet", "Modular support to check-out construction machinery directly to sites.")
                    RoadmapItem("Accounting, Invoices, & Expenses", "Prepared schemas to bind material movements directly to client accounts.")
                }
            }
        }
    }

    // Export Dialog showing complete DB
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Database Backup Ready", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Copy this backup string. Store it safely to restore later:", fontSize = 12.sp, color = TextMediumGray)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(SurfaceLightGray, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = generatedBackupText,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Glorious Homes Inventory Backup", generatedBackupText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Copy Backup", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close", color = SkyBlue)
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, tint = SkyBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Database", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste your exported JSON database backup below:", fontSize = 12.sp, color = TextMediumGray)
                    OutlinedTextField(
                        value = restoreInputText,
                        onValueChange = { restoreInputText = it },
                        placeholder = { Text("Paste JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SkyBlue)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreInputText.isBlank()) {
                            Toast.makeText(context, "Please paste valid JSON", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.restoreBackupJson(restoreInputText) { success ->
                            if (success) {
                                Toast.makeText(context, "Database restored successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid database format. Restore failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Restore", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel", color = SkyBlue)
                }
            }
        )
    }
}

@Composable
fun RoadmapItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Circle, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(6.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextCharcoal)
        }
        Text(text = description, fontSize = 11.sp, color = TextMediumGray, modifier = Modifier.padding(start = 14.dp))
    }
}
