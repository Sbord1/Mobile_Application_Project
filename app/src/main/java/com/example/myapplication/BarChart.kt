package com.example.myapplication

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.barchart.models.BarChartType

@Composable
fun BarChartScreen() {
    val barsData = listOf(
        BarData(point = Point(0f, 20f), label = "Jan", color = Color.Blue),
        BarData(point = Point(1f, 45f), label = "Feb", color = Color.Red),
        BarData(point = Point(2f, 30f), label = "Mar", color = Color.Green),
        BarData(point = Point(3f, 55f), label = "Apr", color = Color.Blue),
        BarData(point = Point(4f, 40f), label = "May", color = Color.Red),
        BarData(point = Point(5f, 65f), label = "Jun", color = Color.Green),
        BarData(point = Point(6f, 50f), label = "Jul", color = Color.Blue),
        BarData(point = Point(7f, 75f), label = "Aug", color = Color.Red)
    )

    val xAxisData = AxisData.Builder()
        .axisStepSize(50.dp)
        .steps(barsData.size - 1)
        .labelData { index -> barsData[index].label }
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelData { index -> (index * 20).toString() }
        .build()

    val barChartData = BarChartData(
        chartData = barsData,
        xAxisData = xAxisData,
        yAxisData = yAxisData
    )

    BarChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        barChartData = barChartData
    )
}