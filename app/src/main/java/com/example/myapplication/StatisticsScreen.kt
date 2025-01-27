package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun StatisticsScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    var chartType by remember { mutableStateOf("Line Chart") }

    Scaffold(
        topBar = { CustomTopBar() },
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            TransactionFilterTabs()

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown Button Below Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                ChartTypeSelector(
                    onChartTypeSelected = { selectedType ->
                        chartType = selectedType
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Section - No Box, directly use the chart composable
            when (chartType) {
                "Line Chart" -> LineChartScreen()
                "Pie Chart" -> PieChartScreen()
                "Bar Chart" -> BarChartScreen()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spending Categories
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                listOf(
                    "Shopping" to "-5120",
                    "Subscription" to "-1280",
                    "Food" to "-532"
                ).forEach { (category, amount) ->
                    SpendingCategoryCard(category = category, amount = amount)
                }
            }
        }
    }
}


@Composable
fun CustomTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // Optional background color
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
            // Centered Text
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.h5, // Larger text for emphasis
                color = Color.Black,
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .padding(top = 30.dp)
                    .padding(horizontal = 100.dp)

            )
            // Download Button
                IconButton(
                    onClick = { /* Handle download action */ }
                ) {
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
fun SpendingCategoryCard(category: String, amount: String) {
    Card(
        shape = RoundedCornerShape(16.dp), // Rounded corners
        backgroundColor = Color.White, // Background color
        elevation = 4.dp, // Elevation for shadow
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Space between cards
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Name
            Text(
                text = category,
                style = MaterialTheme.typography.body1
            )

            // Amount with Error Color for Negative Values
            Text(
                text = amount,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Composable
fun ChartTypeSelector(
    onChartTypeSelected: (String) -> Unit // Callback for when a chart type is selected
) {
    var expanded by remember { mutableStateOf(false) } // Track dropdown state
    var selectedChartType by remember { mutableStateOf("Line Chart") }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd) // Align dropdown at the top-right
    ) {
        // Button to trigger the dropdown
        OutlinedButton(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text(text = selectedChartType, color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown Icon",
                tint = Color.Black
            )
        }

        // Dropdown menu for chart type selection
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White) // Optional: background for the dropdown
        ) {
            val chartTypes = listOf("Line Chart", "Pie Chart", "Bar Chart")
            chartTypes.forEach { chartType ->
                DropdownMenuItem(
                    onClick = {
                        selectedChartType = chartType // Update selected chart type
                        expanded = false // Close dropdown
                        onChartTypeSelected(chartType) // Notify parent of selection
                    }
                ) {
                    Text(text = chartType, color=Color.Black)
                }
            }
        }
    }
}




@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=780dp,dpi=408",
    showSystemUi = true
)
@Composable
fun PreviewStatistics() {
    val dummyNavController = rememberNavController()
    StatisticsScreen(navController = dummyNavController)
}
