package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController


@Composable
fun BottomNavigationBar(navController: NavController) {
    Box {
        // Background Image (Custom Bottom Bar Design)
        Image(
            painter = painterResource(id = R.drawable.ic_navigation_bar),
            contentDescription = "Custom Bottom Navigation Bar",
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 10.dp)
        )


        // Floating Action Button (FAB)
        FloatingActionButton(
            onClick = {
                // Navigate to ScanReceiptScreen
                navController.navigate("add_expense/Others")
            },
            backgroundColor = Color(0xFF7E57C2), // Use a color that fits your theme
            modifier = Modifier
                .align(Alignment.BottomCenter) // Place at the bottom center
                .offset(y = (-15).dp)
                .size(62.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        }

        // Bottom Navigation Items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp) // Adjust padding to fit the design
        ) {
            // Home
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
                label = { Text("Home") },
                selected = navController.currentDestination?.route == "home_screen",
                onClick = { navController.navigate("home_screen") }
            )

            // Spacer to add custom spacing
            Spacer(modifier = Modifier.width(0.dp)) // Add space between items

            // Transactions
            BottomNavigationItem(
                icon = { MonitoringIcon() },
                label = { Text("Logs",maxLines=1) },
                selected = navController.currentDestination?.route == "",
                onClick = { navController.navigate("transactions_screen")},
                modifier = Modifier.offset(x = (-15).dp)
            )

            // Spacer to add custom spacing
            Spacer(modifier = Modifier.width(30.dp)) // Add extra space for FAB

            // Statistics
            BottomNavigationItem(
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Statistics", tint = Color.White) },
                label = { Text("Statistics") },
                selected = navController.currentDestination?.route == "statistics_screen",
                onClick = { navController.navigate("statistics_screen") },
                modifier = Modifier.offset(x = (15).dp)
            )

            // Spacer to add custom spacing
            Spacer(modifier = Modifier.width(0.dp)) // Add space between items

            // Profile
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White) },
                label = { Text("Profile", maxLines=1 )},
                selected = navController.currentDestination?.route == "profile_screen",
                onClick = { navController.navigate("profile_screen") }
            )
        }
    }
}

@Composable
fun MonitoringIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_monitoring),
        contentDescription = "Monitoring Icon",
        modifier = modifier.size(24.dp)
    )
}



@Preview(showBackground = true)
@Composable
fun PreviewBottomNavigationBar() {
    // Create a dummy NavController for preview purposes
    val dummyNavController = rememberNavController()

    // Render the BottomNavigationBar with the dummy NavController
    BottomNavigationBar(navController = dummyNavController)
}

