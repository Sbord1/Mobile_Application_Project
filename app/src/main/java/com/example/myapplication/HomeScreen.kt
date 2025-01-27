package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun HomeScreen(navController: androidx.navigation.NavController) {
    Scaffold(
        topBar = { HomeAppBar() },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.systemBars.asPaddingValues())// Add padding from Scaffold
                .fillMaxSize()
        ){

            // Show only first 4 transactions
            TransactionHistory(limit = 4,
                onSeeAllClick = {
                    navController.navigate("transactions_screen")
                }
            )
        }
    }
}

@Composable
fun HomeAppBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)


    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_tophome), // Replace with your image resource
            contentDescription = "AppBar Background",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Overlaying Content (Text + BalanceCard)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp), // Padding to prevent overlap with the top edge
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // Center items vertically within the Box
        ) {
            // Centered Text
            Text(
                text = "Hi, Francesco!",
                style = MaterialTheme.typography.h4, // Larger text for emphasis
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            )


            // BalanceCard Below Text
            BalanceCard()
        }
    }
}

@Composable
fun TransactionHistory(
    limit: Int = 4,
    onSeeAllClick: () -> Unit = {}
) {
    val transactions = listOf(
        Transaction("Upwork", "Today", "+ $850.00", Color.Green),
        Transaction("Transfer", "Yesterday", "- $85.00", Color.Red),
        Transaction("Paypal", "Jan 30, 2022", "+ $1,406.00", Color.Green),
        Transaction("Youtube", "Jan 16, 2022", "- $11.99", Color.Red)
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions History",
                style = MaterialTheme.typography.h6
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(transactions.take(limit)) { transaction ->
                TransactionItem(
                    title = transaction.title,
                    date = transaction.date,
                    amount = transaction.amount,
                    color = transaction.color
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    date: String,
    amount: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = date, color = Color.Gray, fontSize = 12.sp)
        }
        Text(text = amount, color = color, fontWeight = FontWeight.Bold)
    }
}

data class Transaction(
    val title: String,
    val date: String,
    val amount: String,
    val color: Color
)

@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=780dp,dpi=408", // Approximate dimensions for Huawei P20 Pro
    showSystemUi = true // Shows the status and navigation bars in preview
)
@Composable
fun PreviewHomeScreen() {
    val dummyNavController = rememberNavController()
    HomeScreen(navController = dummyNavController)
}
