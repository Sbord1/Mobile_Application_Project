package com.example.myapplication

import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.firestore.Query

@Composable
fun HomeScreen(navController: androidx.navigation.NavController) {
    var username by remember { mutableStateOf("User") }
    var selectedCurrency by rememberSaveable { mutableStateOf("EUR") }

    // Ensure Firebase authentication is initialized
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    LaunchedEffect(userId) {
        if (userId != null) {
            fetchUsername { fetchedUsername ->
                username = fetchedUsername
            }
        }
    }

    Scaffold(
        topBar = { HomeAppBar(navController, username, selectedCurrency, onCurrencyChange = { newCurrency ->
            selectedCurrency = newCurrency // 🔹 Update state in HomeScreen
        }) },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.systemBars.asPaddingValues())
                .fillMaxSize()
        ) {
            TransactionHistory(
                limit = 4,
                selectedCurrency = selectedCurrency,
                onSeeAllClick = { navController.navigate("transactions_screen") }
            )
        }
    }
}

@Composable
fun HomeAppBar(
    navController: androidx.navigation.NavController,
    username: String,
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit // 🔹 Accept function to update currency
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_tophome),
            contentDescription = "AppBar Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hi, $username!",
                style = MaterialTheme.typography.h4,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

        }
    }
}


@Composable
fun TransactionHistory(
    limit: Int = 4,
    selectedCurrency: String,
    onSeeAllClick: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }  // ✅ Loading state

    LaunchedEffect(userId, selectedCurrency) {
        if (userId == null) {
            transactions = emptyList()
            return@LaunchedEffect
        }

        isLoading = true  // ✅ Start loading

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                val fetchedTransactions = snapshot.documents.mapNotNull { document ->
                    try {
                        val id = document.id
                        val title = document.getString("category") ?: "Others"
                        val date = document.getString("date") ?: "Unknown"
                        val baseAmount = document.getDouble("amount") ?: 0.0

                        Transaction(id=id, title, date, baseAmount, mutableStateOf(baseAmount), Color.Red)
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing document: ${e.message}")
                        null
                    }
                }

                if (selectedCurrency != "EUR") {
                    transactions = emptyList() // ✅ Clear transactions before conversion

                    // ✅ Convert all transactions asynchronously
                    val convertedTransactions = mutableListOf<Transaction>()
                    fetchedTransactions.forEach { transaction ->
                        convertAmount(context, transaction.baseAmount, "EUR", selectedCurrency) { convertedAmount ->
                            convertedTransactions.add(transaction.copy(amount = mutableStateOf(convertedAmount)))

                            // ✅ Update transactions only when all conversions are done
                            if (convertedTransactions.size == fetchedTransactions.size) {
                                transactions = convertedTransactions
                                isLoading = false  // ✅ Stop loading after conversion
                            }
                        }
                    }
                } else {
                    // ✅ If currency is EUR, no conversion needed
                    transactions = fetchedTransactions
                    isLoading = false  // ✅ Stop loading immediately
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching transactions: ${e.message}")
                transactions = emptyList()
                isLoading = false  // ✅ Stop loading on failure
            }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.h6
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Show loading spinner when data is being fetched or converted
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // ✅ Show transactions only when loading is complete
        if (transactions.isNotEmpty() && !isLoading) {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(
                        title = transaction.title,
                        date = transaction.date,
                        amount = String.format("%.2f %s", transaction.amount.value, selectedCurrency),
                        color = transaction.color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // ✅ Show "No transactions found" if list is empty and not loading
        if (transactions.isEmpty() && !isLoading) {
            Text(
                text = "No transactions found",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
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
                .background(Color.Transparent, shape = RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = getCategoryImage(title)),
                contentDescription = "$title Icon",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = date, color = Color.Gray, fontSize = 12.sp)
        }

        Text(text = amount, color = color, fontWeight = FontWeight.Bold)
    }
}

data class Transaction(
    val id: String,
    val title: String,
    val date: String,
    val baseAmount: Double,  // Always store the original EUR amount
    var amount: MutableState<Double> = mutableStateOf(baseAmount),     // Displayed amount (converted if needed)
    val color: Color,
    var isVisible: MutableState<Boolean> = mutableStateOf(true)
)

fun fetchUsername(onUsernameFetched: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return

    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    userRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val username = document.getString("username") ?: "User"
                onUsernameFetched(username)
            } else {
                Log.e("Firestore", "User document does not exist")
                onUsernameFetched("User")
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching username: ${e.message}")
            onUsernameFetched("User")
        }
}

fun getCategoryImage(category: String): Int {
    return try {
        when (category.lowercase()) {
            "entertainment" -> R.drawable.cinema
            "food" -> R.drawable.ic_lunch
            "transport" -> R.drawable.ic_train
            else -> R.drawable.others
        }
    } catch (e: Exception) {
        Log.e("CategoryImage", "Error loading category image for $category: ${e.message}")
        R.drawable.others
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=780dp,dpi=408",
    showSystemUi = true
)
@Composable
fun PreviewHomeScreen() {
    val dummyNavController = rememberNavController()
    HomeScreen(navController = dummyNavController)
}