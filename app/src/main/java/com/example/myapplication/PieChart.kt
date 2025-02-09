package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartData
import co.yml.charts.ui.piechart.models.PieChartConfig
import androidx.compose.material.Text
import kotlinx.coroutines.delay


@Composable
fun PieChartScreen(selectedYear: Int, pieSlices: List<PieChartData.Slice>) {
    var showChart by remember { mutableStateOf(true) }

    LaunchedEffect(selectedYear) {
        showChart = false        // Hide the chart temporarily
        delay(150)                // Short delay to clear previous chart (adjust if needed)
        showChart = true         // Show new chart with animation
    }

    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = true,
        activeSliceAlpha = 0.7f,
        animationDuration = 600
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (pieSlices.isNotEmpty() && showChart) {
            PieChart(
                modifier = Modifier.size(250.dp),
                pieChartData = PieChartData(slices = pieSlices, plotType = PlotType.Pie),
                pieChartConfig = pieChartConfig
            )
        } else if (!showChart) {
            Spacer(modifier = Modifier.size(250.dp))
        } else {
            Text(text = "No data available for $selectedYear", color = Color.Gray, modifier = Modifier.padding(16.dp))
        }
    }
}
