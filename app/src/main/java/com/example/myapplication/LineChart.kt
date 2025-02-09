package com.example.myapplication

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import co.yml.charts.common.model.Point
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.max

@SuppressLint("SimpleDateFormat")
@Composable
fun LineChartScreen(selectedYear: Int) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var chartData by remember { mutableStateOf<List<Point>>(emptyList()) }
    val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    LaunchedEffect(userId, selectedYear) {
        if (userId == null) {
            Log.e("Firebase", "User ID is null. Not fetching data.")
            return@LaunchedEffect
        }

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val monthlyTotals = FloatArray(12) { 0f }

                result.documents.forEach { document ->
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f
                    val dateString = document.getString("date") ?: ""
                    val dateParts = dateString.split(" ")

                    if (dateParts.size >= 4) {
                        val year = dateParts[3].toIntOrNull()
                        val month = convertMonthToNumber(dateParts[2])

                        if (year == selectedYear && month in 1..12) {
                            monthlyTotals[month - 1] += amount
                        }
                    }
                }

                chartData = monthlyTotals.mapIndexed { index, total ->
                    Point(x = index.toFloat(), y = total)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting documents: ", exception)
            }
    }

    val yValues = chartData.map { it.y }
    val maxY = yValues.maxOrNull() ?: 100f
    val minY = yValues.minOrNull() ?: 0f
    val yStepSize = max(1f, (maxY - minY) / 5)
    val steps = if (maxY > 0) ((maxY - minY) / yStepSize).toInt() else 5

    val xAxisData = AxisData.Builder()
        .axisStepSize(35.dp) // Tighter spacing between months
        .backgroundColor(Color.Transparent)
        .steps(11)
        .labelData { i -> monthLabels.getOrNull(i.toInt()) ?: "" }
        .axisLineColor(MaterialTheme.colorScheme.primary)
        .axisLabelColor(MaterialTheme.colorScheme.primary)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(Color.Transparent)
        .labelData { i -> (minY + i * yStepSize).toInt().toString() }
        .axisLineColor(MaterialTheme.colorScheme.primary)
        .axisLabelColor(MaterialTheme.colorScheme.primary)
        .build()

    // ✅ No external scroll modifier, scroll handled inside LineChart
    Box(modifier = Modifier.fillMaxWidth()) {
        if (chartData.isNotEmpty()) {
            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                lineChartData = LineChartData(
                    linePlotData = LinePlotData(
                        lines = listOf(
                            Line(
                                dataPoints = chartData,
                                lineStyle = LineStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    lineType = LineType.SmoothCurve()
                                ),
                                intersectionPoint = IntersectionPoint(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                selectionHighlightPoint = SelectionHighlightPoint(),
                                shadowUnderLine = ShadowUnderLine(
                                    alpha = 0.3f,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                selectionHighlightPopUp = SelectionHighlightPopUp()
                            )
                        )
                    ),
                    backgroundColor = Color.Transparent,
                    xAxisData = xAxisData,
                    yAxisData = yAxisData,
                    isZoomAllowed = true, // ✅ Enable pinch-to-zoom and scroll gestures
                    paddingRight = 0.dp,
                    containerPaddingEnd = 0.dp
                )
            )
        } else {
            Text(
                text = "Loading chart data...",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

fun convertMonthToNumber(month: String): Int {
    return when (month.lowercase()) {
        "gen" -> 1
        "feb" -> 2
        "mar" -> 3
        "apr" -> 4
        "mag" -> 5
        "giu" -> 6
        "lug" -> 7
        "ago" -> 8
        "set" -> 9
        "ott" -> 10
        "nov" -> 11
        "dic" -> 12
        else -> -1
    }
}
