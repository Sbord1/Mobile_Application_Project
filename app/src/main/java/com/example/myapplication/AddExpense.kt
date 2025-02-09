package com.example.myapplication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import java.util.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun AddExpenseScreen(navController: NavController, category: String) {
    var selectedCategory by remember { mutableStateOf(category) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var expanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDateMillis = parseDateToMillis(selectedDate)

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        AddExpenseTopBar()

        Spacer(modifier = Modifier.height(5.dp))

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Card with negative top margin to create overlap
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp) // Adjust this value to control overlap
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Category Selection
                    Text(text = "Category", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable { expanded = !expanded }
                            .padding(16.dp)
                    ) {
                        Text(text = selectedCategory)
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Entertainment", "Food", "Transport", "Others").forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Amount Input
                    Text(text = "Amount", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            val regex = Regex("^\\d*\\.?\\d{0,2}$")
                            if (regex.matches(newValue.text) || newValue.text.isEmpty()) {
                                amount = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Date Selection
                    Text(text = "Date", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .clickable { showDatePickerDialog = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = selectedDate)
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar")
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Add Expense Button
                    Button(
                        onClick = {
                            if (amount.text.isBlank()) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val expense = hashMapOf(
                                "userId" to userId,
                                "category" to selectedCategory,
                                "amount" to (amount.text.toDoubleOrNull() ?: 0.0),
                                "date" to selectedDate,
                                "timestamp" to selectedDateMillis
                            )

                            db.collection("expenses")
                                .add(expense)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Expense Added!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("transactions_screen") {
                                        // Clear the back stack up to transactions_screen
                                        popUpTo("transactions_screen") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B796B))
                    ) {
                        Text("Add Expense")
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Or text
                    Text(
                        text = "Or",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Scan Receipt button
                    Button(
                        onClick = { navController.navigate("scan_receipt") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B796B))
                    ) {
                        Text("Scan A Receipt")
                    }
                }
            }
        }

        if (showDatePickerDialog) {
            DatePickerDialog(
                onDateSelected = { selectedDate = it },
                onDismissRequest = { showDatePickerDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(onDateSelected: (String) -> Unit, onDismissRequest: () -> Unit) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Date") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DatePicker(state = datePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        onDateSelected(dateFormat.format(date))
                    }
                    onDismissRequest()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        }
    )
}

// Function to get current date in formatted form
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

fun parseDateToMillis(dateString: String): Long {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    return try {
        dateFormat.parse(dateString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis() // Fallback in case of error
    }
}


@Composable
fun AddExpenseTopBar() {
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
            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}


