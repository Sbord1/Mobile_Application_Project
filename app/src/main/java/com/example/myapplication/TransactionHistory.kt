package com.example.myapplication

import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import android.widget.Toast
import androidx.compose.material.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import android.content.Context
import org.json.JSONObject
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.*
import kotlinx.coroutines.launch
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@Composable
fun AllTransactionsScreen(navController: NavController) {
    val transactions = remember { mutableStateListOf<Transaction>() }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("EUR") } // Currency selector state
    var expanded by remember { mutableStateOf(false) }
    val availableCurrencies = listOf("EUR", "USD", "GBP", "JPY", "CHF")
    var isLoading by remember { mutableStateOf(false) } // Loading State


    fun deleteTransaction(transaction: Transaction) {
        db.collection("expenses")
            .document(transaction.id)
            .delete()
            .addOnSuccessListener {
                transaction.isVisible.value = false // ✅ Hide the transaction
                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error deleting transaction", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = { TransactionTopBar(
            navController = navController,
            selectedCurrency = selectedCurrency,
            onCurrencyChange = { newCurrency ->
                selectedCurrency = newCurrency
            },
            startDate = startDate,
            endDate = endDate
        )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // Reduced padding for more space
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f) // Allow flexible resizing
                ) {
                    Text(
                        text = startDate?.let { formatDate(it) } ?: "Start Date",
                        maxLines = 1 // Ensure text stays on one line
                    )
                }

                Spacer(modifier = Modifier.width(4.dp)) // Small gap

                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = endDate?.let { formatDate(it) } ?: "End Date",
                        maxLines = 1
                    )
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableCurrencies.forEach { currency ->
                        DropdownMenuItem(onClick = {
                            expanded = false
                            isLoading = true// 🌀 Start loading
                            transactions.clear() // Clear transactions on currency change

                            convertAllTransactions(
                                context,
                                transactions,
                                selectedCurrency,
                                currency,
                                onStart = { transactions.clear() }, // Clear transactions on start
                                onComplete = {
                                    selectedCurrency = currency
                                    isLoading = false
                                }
                            )
                        }) {
                            Text(text = currency)
                        }
                    }
                }
            }

            if (showStartDatePicker) {
                DatePickerDialog(
                    context = context,
                    onDateSelected = { date -> startDate = date },
                    onDismissRequest = { showStartDatePicker = false }
                )
            }

            if (showEndDatePicker) {
                DatePickerDialog(
                    context = context,
                    onDateSelected = { date -> endDate = date },
                    onDismissRequest = { showEndDatePicker = false }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🌀 Show Loading Indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // 🔄 **Fetch Transactions & Convert on Currency Change**
            LaunchedEffect(startDate, endDate, selectedCurrency) {
                val user = auth.currentUser
                if (user == null) {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@LaunchedEffect
                }

                isLoading = true

                val userId = user.uid
                var query = db.collection("expenses").whereEqualTo("userId", userId)

                if (startDate != null) query = query.whereGreaterThanOrEqualTo("timestamp", startDate!!)
                if (endDate != null) query = query.whereLessThanOrEqualTo("timestamp", endDate!!)

                query.orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        transactions.clear()
                        snapshot.documents.forEach { document ->
                            val baseAmount = document.getDouble("amount") ?: 0.0
                            val category = document.getString("category") ?: "Unknown"
                            val date = document.getString("date") ?: "Unknown"

                            val transaction = Transaction(
                                id = document.id,
                                title = category,
                                date = date,
                                baseAmount = baseAmount,
                                amount = mutableStateOf(baseAmount),
                                color = Color.Red
                            )

                            if (selectedCurrency != "EUR") {
                                convertAmount(context, baseAmount, "EUR", selectedCurrency) { convertedAmount ->
                                    transaction.amount.value = convertedAmount
                                }
                            }
                            transactions.add(transaction)
                        }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error fetching transactions", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Show Transactions
            if (transactions.isEmpty() && !isLoading) {
                Text(
                    text = "No transactions found",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 5.dp)
                ) {
                    items(transactions) { transaction ->
                        FullTransactionItem(
                            transaction = transaction,
                            selectedCurrency = selectedCurrency,
                            onDelete = { deleteTransaction(it) } // ✅ Pass delete callback
                        )
                    }
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FullTransactionItem(
    transaction: Transaction,
    selectedCurrency: String,
    onDelete: (Transaction) -> Unit // ✅ Callback for deleting
) {

    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                onDelete(transaction) // ✅ Trigger delete callback
                true
            } else {
                false
            }
        }
    )

    AnimatedVisibility(
        visible = transaction.isVisible.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart), // ✅ Swipe Left
            background = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }
            },
            dismissContent = {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White,
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(transaction.title, fontWeight = FontWeight.Bold)
                            Text(transaction.date, color = Color.Gray)
                        }
                        Text(
                            text = String.format(
                                "%.2f %s",
                                transaction.amount.value,
                                selectedCurrency
                            ),
                            color = transaction.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}

fun convertAllTransactions(
    context: Context,
    transactions: MutableList<Transaction>,  // Changed to MutableList
    fromCurrency: String,
    toCurrency: String,
    onStart: () -> Unit,  // Added to trigger UI changes when loading starts
    onComplete: () -> Unit
) {
    if (fromCurrency == toCurrency) {
        onComplete()
        return
    }

    onStart()  // Clear transactions & trigger loading

    val conversionTasks = transactions.map { transaction ->
        kotlinx.coroutines.CompletableDeferred<Unit>().apply {
            convertAmount(context, transaction.baseAmount, fromCurrency, toCurrency) { convertedAmount ->
                transaction.amount.value = convertedAmount
                complete(Unit)
            }
        }
    }

    kotlinx.coroutines.GlobalScope.launch {
        conversionTasks.forEach { it.await() }
        onComplete()
    }
}



@Composable
fun TransactionTopBar(
    navController: NavController,
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    startDate: Long? = null,
    endDate: Long? = null
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)

    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_tophome),
            contentDescription = "AppBar Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Overlaying Content (Title)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Title
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.h4,
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            BalanceCard(
                navController,
                selectedCurrency,
                onCurrencyChange = onCurrencyChange,
                startDate = startDate,
                endDate = endDate
            )
        }
    }
}

fun convertAmount(
    context: Context,
    amount: Double,
    fromCurrency: String,
    toCurrency: String,
    onResult: (Double) -> Unit
) {
    if (fromCurrency == toCurrency) {
        onResult(amount)
        return
    }

    val url = "https://platinum-tract-449212-d7.ew.r.appspot.com/api/convert"

    val requestBody = JSONObject().apply {
        put("amount", amount)
        put("from_currency", fromCurrency)
        put("to_currency", toCurrency)
    }

    val request = JsonObjectRequest(
        Request.Method.POST, url, requestBody,
        { response ->
            val convertedAmount = response.getDouble("converted_amount")
            onResult(convertedAmount)
        },
        { error ->
            Log.e("CurrencyConversion", "Error converting currency: ${error.message}")
            onResult(amount)
        }
    )

    Volley.newRequestQueue(context).add(request)
}

