package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import co.yml.charts.ui.piechart.models.PieChartData
import java.util.Calendar
import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun StatisticsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var chartType by remember { mutableStateOf("Pie Chart") }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var categoryTotals by remember { mutableStateOf(mapOf<String, Double>()) }
    val chartTypes = listOf("Line Chart", "Pie Chart", "Bar Chart")

    // 🎨 **Color Palette for Categories**
    val colors = listOf(
        Color(0xFF333333), Color(0xFF666a86), Color(0xFF95B8D1),
        Color(0xFFF53844), Color(0xFF3C91E6), Color(0xFFFFA07A)
    )

    var categoryColorMap by remember { mutableStateOf(mapOf<String, Color>()) }

    Scaffold(
        topBar = { CustomTopBar(categoryTotals) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                //  Chart Type Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var expanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = chartType)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            chartTypes.forEach { type ->
                                DropdownMenuItem(onClick = {
                                    chartType = type
                                    expanded = false
                                }) {
                                    Text(text = type)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                YearSelector(selectedYear) { newYear ->
                    selectedYear = newYear
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fetch Transactions When Dates Are Selected
                LaunchedEffect(selectedYear) {
                    val user = auth.currentUser
                    if (user == null) {
                        Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                        return@LaunchedEffect
                    }

                    val userId = user.uid

                    db.collection("expenses")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val totals = mutableMapOf<String, Double>()
                            snapshot.documents.forEach { document ->
                                val amount = document.getDouble("amount") ?: 0.0
                                val category = document.getString("category") ?: "Other"
                                val date = document.getString("date") ?: ""

                                // Extract year from date string
                                val year = date.split(" ").lastOrNull()?.toIntOrNull()
                                if (year == selectedYear) {
                                    totals[category] = (totals[category] ?: 0.0) + amount
                                }
                            }

                            // Assign consistent colors to categories
                            val sortedCategories = totals.keys.sorted()  // Sort for consistency
                            val colorMap = sortedCategories.mapIndexed { index, category ->
                                category to colors[index % colors.size]
                            }.toMap()

                            categoryTotals = totals
                            categoryColorMap = colorMap
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error fetching transactions: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                val totalAmount = categoryTotals.values.sum().toFloat()

                val pieSlices = categoryTotals.entries.mapIndexed { index, entry ->
                    PieChartData.Slice(
                        label = entry.key,
                        value = if (totalAmount > 0) (entry.value.toFloat() / totalAmount) * 100 else 0f,
                        color = colors[index % colors.size]
                    )
                }

                // Chart Section
                when (chartType) {
                    "Line Chart" -> LineChartScreen(selectedYear)
                    "Pie Chart" -> PieChartScreen(selectedYear, pieSlices)
                    "Bar Chart" -> BarChartScreen(
                        selectedYear = selectedYear,
                        colors = categoryColorMap
                    )


                }

                Spacer(modifier = Modifier.height(10.dp))
            }


            items(categoryTotals.entries.toList()) { entry ->
                val category = entry.key
                val total = entry.value
                val index = categoryTotals.keys.indexOf(category)
                SpendingCategoryCard(
                    category = category,
                    amount = String.format("%.2f", total),
                    color = colors[index % colors.size]
                )
            }
        }
    }
}

@Composable
fun CustomTopBar(categoryTotals: Map<String, Double>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.h5,
            color = Color.Black,
            modifier = Modifier
                .padding(bottom = 30.dp)
                .padding(top = 30.dp)
                .padding(horizontal = 100.dp)
        )
        val context = LocalContext.current
        IconButton(onClick = { exportToCSV(categoryTotals, context) }) {
            DownloadButton()
        }
    }
}

@Composable
fun DownloadButton(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_download),
        contentDescription = "Download Icon",
        modifier = modifier.size(24.dp)
    )
}

@Composable
fun SpendingCategoryCard(category: String, amount: String, color: Color) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color Dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = category,
                    style = MaterialTheme.typography.body1
                )
            }

            Text(
                text = "€$amount",
                style = MaterialTheme.typography.body1,
                color = if (amount.toDoubleOrNull() ?: 0.0 < 0) MaterialTheme.colors.error else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun exportToCSV(expenses: Map<String, Double>, context: Context) {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val fileName = "Expenses_$currentDate.csv"


        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir?.exists() == false) {
            downloadsDir.mkdirs() // Create the directory if it doesn't exist
        }

        val file = File(downloadsDir, fileName)
        val writer = FileWriter(file)

        // CSV Headers
        writer.append("Category,Amount (€)\n")

        // Write expense data
        expenses.forEach { (category, amount) ->
            writer.append("$category,${"%.2f".format(amount)}\n")
        }

        writer.flush()
        writer.close()

        Toast.makeText(context, "CSV Exported: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewStatistics() {
    val dummyNavController = rememberNavController()
    StatisticsScreen(navController = dummyNavController)
}
