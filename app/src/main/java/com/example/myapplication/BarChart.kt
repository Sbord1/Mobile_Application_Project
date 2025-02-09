package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.roundToInt

@Composable
fun BarChartScreen(
    selectedYear: Int,
    colors: Map<String, Color>
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var expenses by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }

    // 🔥 Fetch Data Dynamically from Firebase
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
                val totals = mutableMapOf<String, Float>()
                snapshot.documents.forEach { document ->
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f
                    val category = document.getString("category") ?: "Other"
                    val date = document.getString("date") ?: ""

                    val year = date.split(" ").lastOrNull()?.toIntOrNull()
                    if (year == selectedYear) {
                        totals[category] = (totals[category] ?: 0f) + amount
                    }
                }
                expenses = totals.toSortedMap()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching transactions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // **Render Bar Chart if Expenses Exist**
    if (expenses.isNotEmpty()) {
        val maxAmount = expenses.values.maxOrNull() ?: 1f

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Spending Breakdown ($selectedYear)", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                val chartPadding = 50f
                val yAxisLabels = 5 // Number of horizontal grid lines
                val barWidth = (size.width - chartPadding * 2) / (expenses.size * 2)
                val barSpacing = barWidth / 2
                val stepSize = maxAmount / yAxisLabels
                val yAxisStartX = chartPadding

                // Draw Grid Lines & Y-Axis Labels
                for (i in 0..yAxisLabels) {
                    val yPosition = size.height - ((i * stepSize / maxAmount) * size.height)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(chartPadding, yPosition),
                        end = Offset(size.width, yPosition),
                        strokeWidth = 1f
                    )

                    // Draw Y-axis label
                    drawIntoCanvas { canvas ->
                        val textPaint = android.graphics.Paint().apply {
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                        canvas.nativeCanvas.drawText(
                            "${(i * stepSize).roundToInt()}",
                            yAxisStartX - 10,
                            yPosition + 10,
                            textPaint
                        )
                    }
                }

                // Draw Y-Axis
                drawLine(
                    color = Color.Black,
                    start = Offset(chartPadding, 0f),
                    end = Offset(chartPadding, size.height),
                    strokeWidth = 3f
                )

                var xOffset = chartPadding + barSpacing

                // Draw Bars & Labels
                expenses.entries.forEach { (category, amount) ->
                    val barHeight = (amount / maxAmount) * size.height
                    val color = colors[category] ?: Color.Gray

                    // ✅ Draw Bar
                    drawRect(
                        color = color,
                        topLeft = Offset(xOffset, size.height - barHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )

                    // ✅ Draw Category Labels at an Angle
                    drawIntoCanvas { canvas ->
                        val textPaint = android.graphics.Paint().apply {
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        withTransform({
                            rotate(degrees = -45f, pivot = Offset(xOffset + barWidth / 2, size.height + 10))
                        }) {
                            drawContext.canvas.nativeCanvas.drawText(
                                category,
                                xOffset + barWidth / 2,
                                size.height + 40,
                                textPaint
                            )
                        }
                    }

                    xOffset += barWidth + barSpacing
                }
            }
        }
    } else {
        Text(text = "No data available for $selectedYear", modifier = Modifier.padding(16.dp))
    }
}
