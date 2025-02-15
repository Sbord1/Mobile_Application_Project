package com.example.myapplication

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun TransactionFilterSelector(
    context: Context,
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit
) {
    var selectedDateText by remember { mutableStateOf("Select Date") }
    val calendar = Calendar.getInstance()

    fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val timestamp = calendar.timeInMillis
                selectedDateText = "$day/${month + 1}/$year"
                onDateSelected(timestamp)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = { showDatePicker() }) {
            Text(text = selectedDateText)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onDateSelected(null); selectedDateText = "Select Date" }) {
            Text(text = "Reset")
        }
    }
}
