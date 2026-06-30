package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
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
fun ProjectsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val movements by viewModel.movements.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    var activeProjectDetailsView by remember { mutableStateOf<Project?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedProjectForEdit by remember { mutableStateOf<Project?>(null) }

    // Forms
    var projName by remember { mutableStateOf("") }
    var projClient by remember { mutableStateOf("") }
    var projAddr by remember { mutableStateOf("") }
    var projStart by remember { mutableStateOf("") }
    var projDue by remember { mutableStateOf("") }
    var projStatus by remember { mutableStateOf("In Progress") }
    var projBudget by remember { mutableStateOf("") }
    var projNotes by remember { mutableStateOf("") }

    val context = LocalContext.current
    val moneyFormat = DecimalFormat("$#,##0.00")

    if (activeProjectDetailsView != null) {
        // Detailed project screen
        ProjectDetailView(
            project = activeProjectDetailsView!!,
            materials = materials,
            movements = movements,
            viewModel = viewModel,
            moneyFormat = moneyFormat,
            onBack = { activeProjectDetailsView = null }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceLightGray)
        ) {
            TopAppBar(
                title = { Text("Construction Projects", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
                actions = {
                    Button(
                        onClick = {
                            selectedProjectForEdit = null
                            projName = ""
                            projClient = ""
                            projAddr = ""
                            projStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            projDue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            projStatus = "Planned"
                            projBudget = ""
                            projNotes = ""
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_project_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Project", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Project", color = Color.White)
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (projects.isEmpty()) {
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
                                    Icons.Default.Engineering,
                                    contentDescription = "No Projects",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No Active Projects", fontWeight = FontWeight.Bold, color = TextCharcoal)
                                Text("Create projects to issue materials from inventory.", color = TextMediumGray, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(projects) { project ->
                        // Calculate stats for this project
                        val projectMovements = movements.filter { it.projectId == project.id && it.type == "ISSUE_PROJECT" }
                        val materialsUsedCount = projectMovements.size
                        var totalMaterialCost = 0.0

                        projectMovements.forEach { mv ->
                            val materialObj = materials.find { it.id == mv.materialId }
                            val unitPrice = materialObj?.sellingPrice ?: materialObj?.purchasePrice ?: 0.0
                            totalMaterialCost += (unitPrice * mv.quantity)
                        }

                        val remainingBudget = project.budget - totalMaterialCost

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeProjectDetailsView = project }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = project.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextCharcoal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Client: ${project.clientName}",
                                            fontSize = 12.sp,
                                            color = TextMediumGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                when (project.status) {
                                                    "Planned" -> HighlightLightBlue
                                                    "In Progress" -> SuccessGreen.copy(alpha = 0.15f)
                                                    "Completed" -> NavyBlue.copy(alpha = 0.1f)
                                                    else -> WarningGold.copy(alpha = 0.15f)
                                                },
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = project.status,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when (project.status) {
                                                "Planned" -> SkyBlue
                                                "In Progress" -> SuccessGreen
                                                "Completed" -> NavyBlue
                                                else -> WarningGold
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = project.address,
                                    fontSize = 12.sp,
                                    color = TextMediumGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = SurfaceLightGray)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Mini Metrics Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Budget", fontSize = 11.sp, color = TextMediumGray)
                                        Text(moneyFormat.format(project.budget), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextCharcoal)
                                    }
                                    Column {
                                        Text("Material Cost", fontSize = 11.sp, color = TextMediumGray)
                                        Text(moneyFormat.format(totalMaterialCost), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentOrange)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Bal. Budget", fontSize = 11.sp, color = TextMediumGray)
                                        Text(
                                            text = moneyFormat.format(remainingBudget),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (remainingBudget >= 0) SuccessGreen else ErrorRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$materialsUsedCount materials issued",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = SkyBlue
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(onClick = {
                                            selectedProjectForEdit = project
                                            projName = project.name
                                            projClient = project.clientName
                                            projAddr = project.address
                                            projStart = project.startDate
                                            projDue = project.dueDate
                                            projStatus = project.status
                                            projBudget = project.budget.toString()
                                            projNotes = project.notes
                                            showAddEditDialog = true
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Project", tint = SkyBlue, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(onClick = {
                                            if (currentUserRole == "Administrator") {
                                                viewModel.deleteProject(project)
                                            } else {
                                                Toast.makeText(context, "Access Denied: Only Administrators can delete projects.", Toast.LENGTH_SHORT).show()
                                            }
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Project", tint = ErrorRed, modifier = Modifier.size(18.dp))
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

    // Add / Edit Project Dialog
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (selectedProjectForEdit == null) "Add Construction Project" else "Edit Project", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        label = { Text("Project Name") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = projClient,
                        onValueChange = { projClient = it },
                        label = { Text("Client Name") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = projAddr,
                        onValueChange = { projAddr = it },
                        label = { Text("Site Address") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = projStart,
                            onValueChange = { projStart = it },
                            label = { Text("Start Date") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = projDue,
                            onValueChange = { projDue = it },
                            label = { Text("Due Date") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = projBudget,
                        onValueChange = { projBudget = it },
                        label = { Text("Budget Amount ($)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Status Dropdown selector
                    var expandedStatus by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = projStatus,
                            onValueChange = {},
                            label = { Text("Project Status") },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedStatus = !expandedStatus }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Planned", "In Progress", "Completed", "On Hold").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        projStatus = status
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = projNotes,
                        onValueChange = { projNotes = it },
                        label = { Text("Project Notes") },
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val budgetDouble = projBudget.toDoubleOrNull() ?: 0.0
                        if (projName.isBlank() || projClient.isBlank()) {
                            Toast.makeText(context, "Please fill out required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedProjectForEdit == null) {
                            viewModel.addProject(
                                name = projName.trim(),
                                clientName = projClient.trim(),
                                address = projAddr.trim(),
                                startDate = projStart.trim(),
                                dueDate = projDue.trim(),
                                status = projStatus,
                                budget = budgetDouble,
                                notes = projNotes.trim()
                            )
                        } else {
                            viewModel.editProject(
                                id = selectedProjectForEdit!!.id,
                                name = projName.trim(),
                                clientName = projClient.trim(),
                                address = projAddr.trim(),
                                startDate = projStart.trim(),
                                dueDate = projDue.trim(),
                                status = projStatus,
                                budget = budgetDouble,
                                notes = projNotes.trim()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailView(
    project: Project,
    materials: List<Material>,
    movements: List<StockMovement>,
    viewModel: InventoryViewModel,
    moneyFormat: DecimalFormat,
    onBack: () -> Unit
) {
    var showIssueDialog by remember { mutableStateOf(false) }

    // Issue Dialog fields
    var selectedMaterialForIssue by remember { mutableStateOf<Material?>(null) }
    var quantityToIssue by remember { mutableStateOf("") }
    var issueNotes by remember { mutableStateOf("") }

    val projectMovements = movements.filter { it.projectId == project.id && it.type == "ISSUE_PROJECT" }
    var totalMaterialCost = 0.0
    projectMovements.forEach { mv ->
        val mat = materials.find { it.id == mv.materialId }
        val price = mat?.sellingPrice ?: mat?.purchasePrice ?: 0.0
        totalMaterialCost += (price * mv.quantity)
    }

    val remainingBudget = project.budget - totalMaterialCost
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
    ) {
        // Top bar
        TopAppBar(
            title = { Text(project.name, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
            actions = {
                Button(
                    onClick = {
                        selectedMaterialForIssue = null
                        quantityToIssue = ""
                        issueNotes = ""
                        showIssueDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("issue_material_dialog_trigger")
                ) {
                    Icon(Icons.Default.AssignmentReturned, contentDescription = "Issue Material", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Issue Material", color = Color.White)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Project Information", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                            Box(
                                modifier = Modifier
                                    .background(HighlightLightBlue, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(project.status, fontWeight = FontWeight.Bold, color = SkyBlue, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        DetailInfoRow("Client Name", project.clientName)
                        DetailInfoRow("Site Address", project.address)
                        DetailInfoRow("Timeline", "${project.startDate} to ${project.dueDate}")
                        DetailInfoRow("Notes", project.notes.ifEmpty { "No notes specified." })
                    }
                }
            }

            // Financial Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Financial Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Project Budget", fontSize = 12.sp, color = TextMediumGray)
                                Text(moneyFormat.format(project.budget), fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextCharcoal)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HighlightLightBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Material Cost", fontSize = 11.sp, color = TextMediumGray)
                                    Text(moneyFormat.format(totalMaterialCost), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccentOrange)
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (remainingBudget >= 0) HighlightLightBlue else ErrorRed.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Remaining Bal.", fontSize = 11.sp, color = TextMediumGray)
                                    Text(
                                        text = moneyFormat.format(remainingBudget),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (remainingBudget >= 0) SuccessGreen else ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Material Usage History Header
            item {
                Text(
                    text = "Material Usage Logs (${projectMovements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
            }

            if (projectMovements.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No materials have been issued to this project yet.", color = TextMediumGray, fontSize = 13.sp)
                    }
                }
            } else {
                items(projectMovements) { mv ->
                    val mat = materials.find { it.id == mv.materialId }
                    val price = mat?.sellingPrice ?: mat?.purchasePrice ?: 0.0
                    val totalCost = price * mv.quantity
                    val dateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(mv.timestamp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AccentOrange.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mv.materialName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextCharcoal)
                                Text("Qty: ${mv.quantity} ${mat?.unit ?: ""} • Unit Price: ${moneyFormat.format(price)}", fontSize = 12.sp, color = TextMediumGray)
                                Text("Issued: $dateStr • By ${mv.user}", fontSize = 10.sp, color = Color.Gray)
                                if (mv.reason.isNotEmpty()) {
                                    Text("Note: ${mv.reason}", fontSize = 11.sp, color = TextMediumGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Text(
                                text = moneyFormat.format(totalCost),
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Material Issuing Dialog
    if (showIssueDialog) {
        val selectableMaterials = materials.filter { it.quantityInStock > 0 }
        AlertDialog(
            onDismissRequest = { showIssueDialog = false },
            title = { Text("Issue Materials to Project", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select a material in stock to issue:", fontSize = 12.sp, color = TextMediumGray)

                    // Material Dropdown selector
                    var expandedMaterialDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedMaterialForIssue?.name ?: "Tap to choose material",
                            onValueChange = {},
                            label = { Text("Select Material") },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedMaterialDropdown = !expandedMaterialDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedMaterialDropdown = true }
                        )
                        DropdownMenu(
                            expanded = expandedMaterialDropdown,
                            onDismissRequest = { expandedMaterialDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (selectableMaterials.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No materials currently in stock!") },
                                    onClick = {}
                                )
                            } else {
                                selectableMaterials.forEach { material ->
                                    DropdownMenuItem(
                                        text = { Text("${material.name} (Stock: ${material.quantityInStock} ${material.unit})") },
                                        onClick = {
                                            selectedMaterialForIssue = material
                                            expandedMaterialDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedMaterialForIssue != null) {
                        Text(
                            text = "Available Stock: ${selectedMaterialForIssue!!.quantityInStock} ${selectedMaterialForIssue!!.unit}",
                            fontSize = 12.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = quantityToIssue,
                        onValueChange = { quantityToIssue = it },
                        label = { Text("Quantity to Issue") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = issueNotes,
                        onValueChange = { issueNotes = it },
                        label = { Text("Issue Notes / Specific Spot (Optional)") },
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mat = selectedMaterialForIssue
                        val qty = quantityToIssue.toDoubleOrNull()
                        if (mat == null) {
                            Toast.makeText(context, "Please select a material", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (qty == null || qty <= 0.0) {
                            Toast.makeText(context, "Please enter a valid positive quantity", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (qty > mat.quantityInStock) {
                            Toast.makeText(context, "Insufficient Stock! Only ${mat.quantityInStock} available.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.issueMaterial(
                            materialId = mat.id,
                            projectId = project.id,
                            quantity = qty,
                            notes = issueNotes.trim()
                        )
                        showIssueDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Issue", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIssueDialog = false }) {
                    Text("Cancel", color = SkyBlue)
                }
            }
        )
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextMediumGray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 13.sp, color = TextCharcoal)
    }
}
