package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Material
import com.example.data.entity.Project
import com.example.data.entity.StockMovement
import com.example.ui.theme.*
import com.example.ui.viewmodel.InventoryViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val materials by viewModel.materials.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val movements by viewModel.movements.collectAsState()

    // Selected Report Type: "valuation", "low_stock", "movements", "project_use"
    var selectedReportType by remember { mutableStateOf("valuation") }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("CSV") } // CSV, Excel, PDF
    var exportedContentPreview by remember { mutableStateOf("") }

    val context = LocalContext.current
    val moneyFormat = DecimalFormat("$#,##0.00")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
    ) {
        TopAppBar(
            title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue)
        )

        // Tab Selector Row
        ScrollableTabRow(
            selectedTabIndex = when (selectedReportType) {
                "valuation" -> 0
                "low_stock" -> 1
                "movements" -> 2
                else -> 3
            },
            containerColor = Color.White,
            contentColor = SkyBlue,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedReportType == "valuation",
                onClick = { selectedReportType = "valuation" },
                text = { Text("Inventory Value", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedReportType == "low_stock",
                onClick = { selectedReportType = "low_stock" },
                text = { Text("Low Stock Alert", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedReportType == "movements",
                onClick = { selectedReportType = "movements" },
                text = { Text("Stock History", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedReportType == "project_use",
                onClick = { selectedReportType = "project_use" },
                text = { Text("Project Usage", fontWeight = FontWeight.Bold) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Action Box for Exporting
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (selectedReportType) {
                                "valuation" -> "Current Stock Valuation"
                                "low_stock" -> "Low & Out of Stock Report"
                                "movements" -> "Stock Movement & Audit Logs"
                                else -> "Materials Issued Per Project"
                            },
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Export this data directly to modern data formats.",
                            fontSize = 12.sp,
                            color = TextMediumGray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                exportFormat = "CSV"
                                exportedContentPreview = generateReportString(selectedReportType, "CSV", materials, projects, movements)
                                showExportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("export_csv_report_button")
                        ) {
                            Text("CSV", color = Color.White)
                        }

                        Button(
                            onClick = {
                                exportFormat = "PDF"
                                exportedContentPreview = generateReportString(selectedReportType, "PDF", materials, projects, movements)
                                showExportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("PDF", color = Color.White)
                        }
                    }
                }
            }

            // Real Data View Based on Selection
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedReportType) {
                    "valuation" -> {
                        // Header card for total valuation sum
                        val totalVal = materials.sumOf { it.quantityInStock * it.purchasePrice }
                        val totalSellingVal = materials.sumOf { it.quantityInStock * it.sellingPrice }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HighlightLightBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Asset Value (Cost)", fontSize = 12.sp, color = TextMediumGray)
                                        Text(moneyFormat.format(totalVal), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                                    }
                                    Column {
                                        Text("Est. Sales Value", fontSize = 12.sp, color = TextMediumGray)
                                        Text(moneyFormat.format(totalSellingVal), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SuccessGreen)
                                    }
                                }
                            }
                        }

                        if (materials.isEmpty()) {
                            item { EmptyStateReport() }
                        } else {
                            items(materials) { mat ->
                                val itemVal = mat.quantityInStock * mat.purchasePrice
                                ReportRowItem(
                                    title = mat.name,
                                    subtitle = "Code: ${mat.materialCode} • Supplier: ${mat.supplier}",
                                    badge = "${mat.quantityInStock} ${mat.unit}",
                                    badgeColor = if (mat.quantityInStock == 0.0) ErrorRed else SuccessGreen,
                                    rightValue = moneyFormat.format(itemVal)
                                )
                            }
                        }
                    }

                    "low_stock" -> {
                        val alertList = materials.filter { it.quantityInStock <= it.minimumStockLevel }
                        if (alertList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Excellent: No items are currently low on stock!", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            items(alertList) { mat ->
                                ReportRowItem(
                                    title = mat.name,
                                    subtitle = "In Stock: ${mat.quantityInStock} ${mat.unit} (Min: ${mat.minimumStockLevel})",
                                    badge = if (mat.quantityInStock == 0.0) "OUT" else "LOW",
                                    badgeColor = if (mat.quantityInStock == 0.0) ErrorRed else WarningGold,
                                    rightValue = "Min Alert"
                                )
                            }
                        }
                    }

                    "movements" -> {
                        if (movements.isEmpty()) {
                            item { EmptyStateReport() }
                        } else {
                            items(movements) { mv ->
                                val dateStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date(mv.timestamp))
                                val color = when (mv.type) {
                                    "ADD" -> SuccessGreen
                                    "REMOVE", "ISSUE_PROJECT" -> ErrorRed
                                    "TRANSFER" -> SkyBlue
                                    else -> WarningGold
                                }

                                ReportRowItem(
                                    title = mv.materialName,
                                    subtitle = "$dateStr • Reason: ${mv.reason} • User: ${mv.user}",
                                    badge = mv.type,
                                    badgeColor = color,
                                    rightValue = "${if (mv.type == "ADD") "+" else "-"}${mv.quantity}"
                                )
                            }
                        }
                    }

                    "project_use" -> {
                        val issues = movements.filter { it.type == "ISSUE_PROJECT" }
                        if (issues.isEmpty()) {
                            item { EmptyStateReport() }
                        } else {
                            items(issues) { mv ->
                                val mat = materials.find { it.id == mv.materialId }
                                val cost = (mat?.sellingPrice ?: mat?.purchasePrice ?: 0.0) * mv.quantity

                                ReportRowItem(
                                    title = mv.materialName,
                                    subtitle = "Issued to: ${mv.projectName ?: "Unknown project"} • User: ${mv.user}",
                                    badge = "${mv.quantity} ${mat?.unit ?: ""}",
                                    badgeColor = AccentOrange,
                                    rightValue = moneyFormat.format(cost)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Dialog showing CSV content
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = SkyBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$exportFormat Report Generated", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Here is your actual generated $exportFormat report content:", fontSize = 12.sp, color = TextMediumGray)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(SurfaceLightGray, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exportedContentPreview,
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
                        val clip = ClipData.newPlainText("Glorious Homes Inventory Report", exportedContentPreview)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Copy Content", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = SkyBlue)
                }
            }
        )
    }
}

@Composable
fun EmptyStateReport() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No Report Data Available", color = TextMediumGray, fontSize = 13.sp)
        }
    }
}

@Composable
fun ReportRowItem(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    rightValue: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextCharcoal)
                Text(subtitle, fontSize = 11.sp, color = TextMediumGray)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(badge, fontWeight = FontWeight.Bold, color = badgeColor, fontSize = 11.sp)
                }

                Text(rightValue, fontWeight = FontWeight.Bold, color = TextCharcoal, fontSize = 14.sp)
            }
        }
    }
}

// Generates real data output based on tables
private fun generateReportString(
    type: String,
    format: String,
    materials: List<Material>,
    projects: List<Project>,
    movements: List<StockMovement>
): String {
    if (format == "PDF") {
        return """
        ==================================================
                   GLORIOUS HOMES INVENTORY REPORT
                     Format: PDF Document Preview
        ==================================================
        Report Type: ${type.uppercase()}
        Generated On: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
        Total Records: ${if (type == "valuation") materials.size else movements.size}
        --------------------------------------------------
        This is a pre-formatted export layout representing a 
        ready-to-print vector grid for Glorious Homes construction on-site archives.
        
        ${if (type == "valuation") {
            materials.joinToString("\n") { "Code: ${it.materialCode} | Name: ${it.name} | Qty: ${it.quantityInStock} | Total Cost: $${it.quantityInStock * it.purchasePrice}" }
        } else {
            movements.joinToString("\n") { "[${it.type}] ${it.materialName} | Qty: ${it.quantity} | User: ${it.user}" }
        }}
        ==================================================
        """.trimIndent()
    }

    // Generate real CSV
    val sb = StringBuilder()
    when (type) {
        "valuation" -> {
            sb.append("Material Code,Name,Category,Supplier,Quantity,Unit,Purchase Price,Selling Price,Asset Value\n")
            materials.forEach {
                sb.append("${it.materialCode},\"${it.name.replace("\"", "\"\"")}\",${it.category},${it.supplier},${it.quantityInStock},${it.unit},${it.purchasePrice},${it.sellingPrice},${it.quantityInStock * it.purchasePrice}\n")
            }
        }
        "low_stock" -> {
            sb.append("Material Code,Name,Category,Quantity in Stock,Minimum Level,Unit,Supplier\n")
            materials.filter { it.quantityInStock <= it.minimumStockLevel }.forEach {
                sb.append("${it.materialCode},\"${it.name.replace("\"", "\"\"")}\",${it.category},${it.quantityInStock},${it.minimumStockLevel},${it.unit},${it.supplier}\n")
            }
        }
        "movements" -> {
            sb.append("Timestamp,Type,Material Name,Quantity,User,Reason\n")
            movements.forEach {
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.timestamp))
                sb.append("$dateStr,${it.type},\"${it.materialName.replace("\"", "\"\"")}\",${it.quantity},${it.user},\"${it.reason.replace("\"", "\"\"")}\"\n")
            }
        }
        "project_use" -> {
            sb.append("Timestamp,Project,Material,Quantity,User,Notes\n")
            movements.filter { it.type == "ISSUE_PROJECT" }.forEach {
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.timestamp))
                sb.append("$dateStr,\"${it.projectName ?: ""}\",\"${it.materialName}\",${it.quantity},${it.user},\"${it.reason}\"\n")
            }
        }
    }
    return sb.toString()
}
