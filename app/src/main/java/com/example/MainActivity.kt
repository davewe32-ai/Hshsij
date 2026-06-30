package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: InventoryViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val actionStatus by viewModel.actionStatus.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val context = LocalContext.current

    // Display action toasts from ViewModel
    LaunchedEffect(actionStatus) {
        actionStatus?.let { status ->
            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = NavyBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("app_bottom_nav_bar")
            ) {
                // Tab 1: Dashboard
                NavigationBarItem(
                    selected = currentScreen == "dashboard",
                    onClick = { viewModel.setScreen("dashboard") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBlue,
                        selectedTextColor = SkyBlue,
                        indicatorColor = SkyBlue,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_dashboard_tab")
                )

                // Tab 2: Inventory
                NavigationBarItem(
                    selected = currentScreen == "inventory",
                    onClick = { viewModel.setScreen("inventory") },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBlue,
                        selectedTextColor = SkyBlue,
                        indicatorColor = SkyBlue,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_inventory_tab")
                )

                // Tab 3: Cabinets
                NavigationBarItem(
                    selected = currentScreen == "cabinets",
                    onClick = { viewModel.setScreen("cabinets") },
                    icon = { Icon(Icons.Default.AllInbox, contentDescription = "Cabinets") },
                    label = { Text("Cabinets", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBlue,
                        selectedTextColor = SkyBlue,
                        indicatorColor = SkyBlue,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_cabinets_tab")
                )

                // Tab 4: Projects
                NavigationBarItem(
                    selected = currentScreen == "projects",
                    onClick = { viewModel.setScreen("projects") },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = "Projects") },
                    label = { Text("Projects", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBlue,
                        selectedTextColor = SkyBlue,
                        indicatorColor = SkyBlue,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_projects_tab")
                )

                // Tab 5: Data Hub / Settings
                NavigationBarItem(
                    selected = currentScreen in listOf("settings", "categories", "reports"),
                    onClick = { viewModel.setScreen("settings") },
                    icon = { Icon(Icons.Default.FolderZip, contentDescription = "Data Center") },
                    label = { Text("Data Center", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBlue,
                        selectedTextColor = SkyBlue,
                        indicatorColor = SkyBlue,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Role Indicator Bar on Top of Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBlue)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Online",
                        tint = SuccessGreen,
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Glorious Homes On-Site Offline Mode",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .background(SkyBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Role: $currentUserRole",
                        color = SkyBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main body router
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (currentScreen) {
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "inventory" -> MaterialsScreen(viewModel = viewModel)
                    "cabinets" -> CabinetsScreen(viewModel = viewModel)
                    "projects" -> ProjectsScreen(viewModel = viewModel)
                    "categories" -> CategoriesScreen(viewModel = viewModel)
                    "reports" -> ReportsScreen(viewModel = viewModel)
                    "settings" -> DataHubSubMenu(viewModel = viewModel)
                    "settings_screen_backup" -> BackupRestoreScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DataHubSubMenu(viewModel: InventoryViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLightGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Glorious Homes Data Hub",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    text = "Configure categories, generate reports, or manage offline databases.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        // Sub-menus
        DataHubMenuItem(
            title = "Reports & Analytics",
            desc = "Generate inventory valuations, low-stock lists, project history, and export to CSV/PDF.",
            icon = Icons.Default.Assessment,
            color = SuccessGreen
        ) {
            viewModel.setScreen("reports")
        }

        DataHubMenuItem(
            title = "Material Categories",
            desc = "Add, manage, and edit classifications for building materials.",
            icon = Icons.Default.Category,
            color = SkyBlue
        ) {
            viewModel.setScreen("categories")
        }

        DataHubMenuItem(
            title = "Backup, Restore & Accounts",
            desc = "Export JSON archives, restore from past copies, or switch user accounts.",
            icon = Icons.Default.Settings,
            color = AccentOrange
        ) {
            // We load the settings view (BackupRestoreScreen)
            viewModel.setScreen("settings_screen_backup")
        }
    }
}

// Sub routing for settings inside Layout
@Composable
fun DataHubMenuItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextCharcoal)
                Text(text = desc, fontSize = 12.sp, color = TextMediumGray)
            }

            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
