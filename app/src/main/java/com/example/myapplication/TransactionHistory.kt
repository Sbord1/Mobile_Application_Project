package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.Card
import androidx.compose.ui.layout.ContentScale


@Composable
fun AllTransactionsScreen(navController: androidx.navigation.NavController) {
    val transactions = listOf(
        Transaction("Upwork", "Today", "+ $850.00", Color.Green),
        Transaction("Transfer", "Yesterday", "- $85.00", Color.Red),
        Transaction("Paypal", "Jan 30, 2022", "+ $1,406.00", Color.Green),
        Transaction("Youtube", "Jan 16, 2022", "- $11.99", Color.Red),
        Transaction("Upwork", "Today", "+ $850.00", Color.Green),
        Transaction("Upwork", "Today", "+ $850.00", Color.Green),
        Transaction("Upwork", "Today", "+ $850.00", Color.Green),

    )

    Scaffold(
        topBar = { TransactionTopBar() },
        bottomBar = {
            BottomNavigationBar(navController)
        },

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding (bottom=20.dp)// Adjust padding to avoid overlapping the bottom bar
                .padding(horizontal = 5.dp) // Add padding for overall content
                .padding (vertical=25.dp)
                .offset(y = (-40).dp) // Adjust vertical offset to reduce extra space
        ) {
            items(transactions) { transaction ->
                FullTransactionItem(transaction)
            }
        }
    }
}

@Composable
fun FullTransactionItem(transaction: Transaction) {
    Card(
        shape = RoundedCornerShape(16.dp), // Rounded corners for each item
        backgroundColor = Color.White, // Background color for the card
        elevation = 4.dp, // Elevation for shadow effect
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Add space between items
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding inside each card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(transaction.title, fontWeight = FontWeight.Bold)
                Text(transaction.date, color = Color.Gray)
            }
            Text(transaction.amount, color = transaction.color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .offset(y = (-25).dp)

    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_tophome),
            contentDescription = "AppBar Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Overlaying Content (Text + Filtering Tabs)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp), // Padding to prevent overlap with the top edge
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // Center items vertically within the Box
        ) {
            // Centered Text
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.h4, // Larger text for emphasis
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 15.dp)
            )
            TransactionFilterTabs()

        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=780dp,dpi=408", // Approximate dimensions for Huawei P20 Pro
    showSystemUi = true // Shows the status and navigation bars in preview
)
@Composable
fun PreviewTransaction() {
    val dummyNavController = rememberNavController()
    AllTransactionsScreen(navController = dummyNavController)
}