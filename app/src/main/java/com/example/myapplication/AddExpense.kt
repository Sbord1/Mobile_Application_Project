package com.example.myapplication

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
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

@Composable
fun AddExpenseScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf("Entertainment") }
    var amount by remember { mutableStateOf(TextFieldValue("$0.00")) }
    var selectedDate by remember { mutableStateOf("Tue, 22 Feb 2022") }
    var expanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Curved background
            Image(
                painter = painterResource(id = R.drawable.ic_tophome),
                contentDescription = "Top Background",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Title
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Main Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Category
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
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

                        // Amount
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { newValue ->
                                // Ensure the value is numeric or has a valid decimal format
                                val regex = Regex("^\\$?\\d*\\.?\\d{0,2}$") // Matches valid numeric values
                                if (regex.matches(newValue.text) || newValue.text.isEmpty()) {
                                    amount = newValue
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number // Restrict keyboard to numbers
                            )
                        )



                        Spacer(modifier = Modifier.height(5.dp))

                        // Date
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .clickable { showDatePickerDialog = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = selectedDate)
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar"
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Add Button
                        Button(
                            onClick = { /* Handle Add */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2B796B)
                            )
                        ) {
                            Text("Add")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B796B)
                    )
                ) {
                    Text("Scan A Receipt")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(onDateSelected: (String) -> Unit, onDismissRequest: () -> Unit) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val selectedDate = remember { mutableStateOf(dateFormat.format(calendar.time)) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Date") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(
                    state = datePickerState
                )
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
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
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

@Preview(showBackground = true)
@Composable
fun PreviewExpense() {
    val dummyNavController = rememberNavController()
    AddExpenseScreen(navController = dummyNavController)
}