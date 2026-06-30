package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.entity.Cabinet
import com.example.data.entity.Material
import com.example.data.entity.StockMovement
import com.example.ui.theme.*
import com.example.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val materialsCount by viewModel.materialsCount.collectAsState()
    val cabinetsCount by viewModel.cabinetsCount.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val outOfStockCount by viewModel.outOfStockCount.collectAsState()
    val movementsTodayCount by viewModel.movementsTodayCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentMovements by viewModel.movements.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val cabinets by viewModel.cabinets.collectAsState()

    var showLowStockDialog by remember { mutableStateOf(false) }
    var showOutOfStockDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glorious Homes Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("branding_header_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Image loaded via Coil
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://lh3.googleusercontent.com/a-/ALV-UjU2bIF-O5oGus0TFlNlBxmkZzx9w2IHYH1okSs-l1o-FJ-8h7w=w600")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Glorious Homes Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp),
                        error = coil.compose.rememberAsyncImagePainter(
                            model = Icons.Filled.HomeRepairService
                        )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "GLORIOUS HOMES",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = SkyBlue,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            text = "Inventory Manager",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Search Bar Section
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by material, cabinet, supplier, project...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input")
            )
        }

        // Search Results Quick View if search is active
        if (searchQuery.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    TextButton(onClick = { viewModel.setScreen("inventory") }) {
                        Text("View All Inventory", color = SkyBlue)
                    }
                }
            }

            val filteredList = viewModel.filteredMaterials.value.take(4)
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching materials found", color = TextMediumGray)
                    }
                }
            } else {
                items(filteredList) { material ->
                    MaterialCompactItemRow(material, cabinets) {
                        viewModel.setSearchQuery("")
                        viewModel.setScreen("inventory")
                    }
                }
            }
        }

        // Summary Stats Grid
        item {
            Text(
                text = "Overview Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextCharcoal
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Total Materials",
                    count = materialsCount.toString(),
                    icon = Icons.Default.Inventory2,
                    color = SkyBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.setScreen("inventory")
                }
                StatCard(
                    title = "Cabinets / Racks",
                    count = cabinetsCount.toString(),
                    icon = Icons.Default.AllInbox,
                    color = NavyBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.setScreen("cabinets")
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Low Stock Alerts",
                    count = lowStockCount.toString(),
                    icon = Icons.Default.Warning,
                    color = WarningGold,
                    modifier = Modifier.weight(1f)
                ) {
                    showLowStockDialog = true
                }
                StatCard(
                    title = "Out of Stock",
                    count = outOfStockCount.toString(),
                    icon = Icons.Default.ErrorOutline,
                    color = ErrorRed,
                    modifier = Modifier.weight(1f)
                ) {
                    showOutOfStockDialog = true
                }
            }
        }

        // Notification center warning alert
        if (lowStockCount > 0 || outOfStockCount > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HighlightLightBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification Icon",
                            tint = AccentOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Attention Required",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "You have $outOfStockCount empty and $lowStockCount low-stock items. Tap statistic cards above to view.",
                                color = TextCharcoal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Section
        item {
            Text(
                text = "Issued Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextCharcoal
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HighlightLightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AssignmentReturned,
                                contentDescription = "Issued Icon",
                                tint = SkyBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Materials Issued Today",
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$movementsTodayCount movements registered today",
                                color = TextMediumGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.setScreen("projects") },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("issue_stock_dashboard_button")
                    ) {
                        Text("Issue Stock", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent Movements list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Stock Movements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
                TextButton(onClick = { viewModel.setScreen("reports") }) {
                    Text("View Logs", color = SkyBlue)
                }
            }
        }

        val historyToShow = recentMovements.take(5)
        if (historyToShow.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "No History Icon",
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No recent stock movements recorded", color = TextMediumGray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(historyToShow) { movement ->
                MovementRow(movement)
            }
        }
    }

    // Low Stock Alert Dialog
    if (showLowStockDialog) {
        val lowStockItems by viewModel.lowStockMaterials.collectAsState()
        AlertDialog(
            onDismissRequest = { showLowStockDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Alert", tint = WarningGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Low Stock Items (${lowStockItems.size})", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (lowStockItems.isEmpty()) {
                    Text("No materials are currently running low on stock.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(lowStockItems) { material ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLightGray, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(material.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Code: ${material.materialCode}", color = TextMediumGray, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${material.quantityInStock} ${material.unit}", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Min: ${material.minimumStockLevel}", color = TextMediumGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLowStockDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Out of Stock Alert Dialog
    if (showOutOfStockDialog) {
        val outOfStockItems by viewModel.outOfStockMaterials.collectAsState()
        AlertDialog(
            onDismissRequest = { showOutOfStockDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Error, contentDescription = "Critical Alert", tint = ErrorRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Out of Stock Items (${outOfStockItems.size})", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (outOfStockItems.isEmpty()) {
                    Text("No materials are currently out of stock.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(outOfStockItems) { material ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLightGray, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(material.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Code: ${material.materialCode}", color = TextMediumGray, fontSize = 11.sp)
                                }
                                Text("EMPTY", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showOutOfStockDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMediumGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextCharcoal
            )
        }
    }
}

@Composable
fun MaterialCompactItemRow(
    material: Material,
    cabinets: List<Cabinet>,
    onClick: () -> Unit
) {
    val cabinetCode = cabinets.find { it.id == material.cabinetId }?.code ?: "No Cabinet"
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (material.quantityInStock == 0.0) ErrorRed.copy(alpha = 0.1f)
                        else if (material.quantityInStock <= material.minimumStockLevel) WarningGold.copy(
                            alpha = 0.1f
                        )
                        else SuccessGreen.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = if (material.quantityInStock == 0.0) ErrorRed
                    else if (material.quantityInStock <= material.minimumStockLevel) WarningGold
                    else SuccessGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$cabinetCode → ${material.shelfPosition}",
                    color = TextMediumGray,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${material.quantityInStock} ${material.unit}",
                    fontWeight = FontWeight.Bold,
                    color = if (material.quantityInStock == 0.0) ErrorRed
                    else if (material.quantityInStock <= material.minimumStockLevel) WarningGold
                    else SuccessGreen,
                    fontSize = 14.sp
                )
                Text(
                    text = material.materialCode,
                    color = TextMediumGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun MovementRow(movement: StockMovement) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(movement.timestamp))

    val (icon, color, label) = when (movement.type) {
        "ADD" -> Triple(Icons.Default.Add, SuccessGreen, "Stock Added")
        "REMOVE" -> Triple(Icons.Default.Remove, ErrorRed, "Stock Removed")
        "ADJUST" -> Triple(Icons.Default.Tune, WarningGold, "Adjusted")
        "TRANSFER" -> Triple(Icons.Default.SwapHoriz, SkyBlue, "Transferred")
        "ISSUE_PROJECT" -> Triple(Icons.Default.Launch, AccentOrange, "Issued")
        "DAMAGE" -> Triple(Icons.Default.BrokenImage, ErrorRed, "Damaged")
        "LOST" -> Triple(Icons.Default.QuestionMark, TextCharcoal, "Lost")
        else -> Triple(Icons.Default.History, TextMediumGray, "Movement")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movement.materialName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${movement.reason} • By ${movement.user}",
                    fontSize = 11.sp,
                    color = TextMediumGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val sign = if (movement.type == "ADD") "+" else if (movement.type == "REMOVE" || movement.type == "ISSUE_PROJECT") "-" else ""
                Text(
                    text = "$sign${movement.quantity}",
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 14.sp
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
