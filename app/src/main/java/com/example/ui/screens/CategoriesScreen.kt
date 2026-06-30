package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Category
import com.example.ui.theme.*
import com.example.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
    ) {
        // Toolbar
        TopAppBar(
            title = { Text("Material Categories", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
            actions = {
                Button(
                    onClick = {
                        newCategoryName = ""
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_category_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Category", color = Color.White)
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Unlimited categories are supported to classify materials dynamically.",
                color = TextMediumGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = "No Categories",
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No Categories Available", fontWeight = FontWeight.Bold, color = TextCharcoal)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categories) { category ->
                        val count = materials.count { it.category.equals(category.name, ignoreCase = true) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(HighlightLightBlue, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Category, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            if (currentUserRole == "Administrator") {
                                                viewModel.deleteCategory(category)
                                            } else {
                                                Toast.makeText(context, "Access Denied: Only Administrators can delete categories.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = category.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$count materials",
                                    fontSize = 12.sp,
                                    color = TextMediumGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Category", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name (e.g. Concrete, Piping)") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SkyBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isBlank()) {
                            Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addCategory(newCategoryName.trim())
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = SkyBlue)
                }
            }
        )
    }
}
