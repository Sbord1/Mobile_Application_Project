package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // Combine the top bar and content in the same Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                ProfileTopBar()
            }

            // Profile Picture
            Surface(
                shape = CircleShape,
                color = Color.LightGray,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 150.dp)
                    .zIndex(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Content Below the Profile Picture
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 250.dp), // Push content below the profile picture
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mario Rossi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "mariorossi@gmail.com",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                ProfileMenuOption(icon = R.drawable.ic_profile, label = "Account info",onClick={})
                ProfileMenuOption(icon = R.drawable.ic_settings, label = "Settings",onClick={})
                ProfileMenuOption(icon = R.drawable.ic_wallet, label = "Add Credit",onClick={})
                ProfileMenuOption(icon = R.drawable.ic_logout, label = "Logout", isDestructive = true,onClick={navController.navigate("login")})
            }
        }
    }
}


@Composable
fun ProfileMenuOption(
    icon: Int,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit // Pass a lambda to handle the click
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(horizontal = 12.dp)
            .offset(y = (-20).dp)
            .clickable { onClick() }, // Add the clickable modifier
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = if (isDestructive) Color.Red else Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (isDestructive) Color.Red else Color.Black
        )
    }
}



@Composable
fun ProfileTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .zIndex(0f)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_tophome),
            contentDescription = "AppBar Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Overlaying Content (Text)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=780dp,dpi=408", // Approximate dimensions for Huawei P20 Pro
    showSystemUi = true // Shows the status and navigation bars in preview
)
@Composable
fun PreviewProfileScreen() {
    val dummyNavController = rememberNavController()
    ProfileScreen(navController = dummyNavController)
}
