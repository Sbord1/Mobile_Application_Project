package com.example.myapplication


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartData
import co.yml.charts.ui.piechart.models.PieChartConfig
import androidx.compose.ui.Alignment



@Composable
fun PieChartScreen() {
    val pieChartData = PieChartData(
        slices = listOf(
            PieChartData.Slice("SciFi", 65f, Color(0xFF333333)),
            PieChartData.Slice("Comedy", 35f, Color(0xFF666a86)),
            PieChartData.Slice("Drama", 10f, Color(0xFF95B8D1)),
            PieChartData.Slice("Romance", 40f, Color(0xFFF53844))
        ),
        plotType = PlotType.Pie
    )
    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = false,
        activeSliceAlpha = 0.5f,
        animationDuration = 600
    )
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            modifier = Modifier
                .width(200.dp)
                .aspectRatio(1f),
            pieChartData = pieChartData,
            pieChartConfig = pieChartConfig
        )
    }
}
