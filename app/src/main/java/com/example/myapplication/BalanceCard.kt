package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call


@Composable
fun BalanceCard(
    navController: NavController,
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    startDate: Long? = null,
    endDate: Long? = null
) {
    val context = LocalContext.current
    val availableCurrencies = listOf("EUR", "USD", "GBP", "JPY", "CHF")
    var expanded by remember { mutableStateOf(false) }

    var baseBalance by remember { mutableStateOf(0.0) }
    var displayedBalance by remember { mutableStateOf(baseBalance) }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val db = FirebaseFirestore.getInstance()


    // Function to fetch converted amount
    fun convertBalance(newCurrency: String) {
        if (newCurrency == "EUR") {
            displayedBalance = baseBalance
            return
        }
        val apiService = RetrofitClient.apiService
        val request = CurrencyConversionRequest(baseBalance, "EUR", newCurrency)

        apiService.convertCurrency(request).enqueue(object : retrofit2.Callback<CurrencyConversionResponse> {
            override fun onResponse(call: Call<CurrencyConversionResponse>, response: retrofit2.Response<CurrencyConversionResponse>) {
                if (response.isSuccessful) {
                    displayedBalance = response.body()?.converted_amount ?: baseBalance
                } else {
                    Log.e("BalanceCard", "Conversion failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<CurrencyConversionResponse>, t: Throwable) {
                Log.e("BalanceCard", "Currency conversion failed: ${t.message}")
            }
        })
    }

    fun fetchAndCalculateBalance() {
        if (userId != null) {
            var query = db.collection("expenses")
                .whereEqualTo("userId", userId)

            // Apply date filter if provided
            if (startDate != null && endDate != null) {
                query = query.whereGreaterThanOrEqualTo("timestamp", startDate)
                    .whereLessThanOrEqualTo("timestamp", endDate)
            }

            query.get().addOnSuccessListener { documents ->
                val total = documents.sumOf { it.getDouble("amount") ?: 0.0 }
                baseBalance = total // Update base balance
                convertBalance(selectedCurrency) // Convert to selected currency
            }.addOnFailureListener { e ->
                Log.e("BalanceCard", "Error fetching transactions", e)
            }
        }
    }
    // Trigger conversion when currency changes
    LaunchedEffect(selectedCurrency, startDate, endDate) {
        fetchAndCalculateBalance()
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Balance", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))

                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Select Currency")
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableCurrencies.forEach { currency ->
                        DropdownMenuItem(onClick = {
                            onCurrencyChange(currency) // Update selected currency globally
                            expanded = false
                        }) {
                            Text(text = currency)
                        }
                    }
                }
            }
            Text(
                text = String.format("Total Spent: %.2f %s", displayedBalance, selectedCurrency),
                style = MaterialTheme.typography.h6
            )
        }
    }
}