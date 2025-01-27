package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransactionFilterTabs() {
    val tabItems = listOf("Today", "Week", "Month", "Year")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        backgroundColor = Color.Transparent, // Make TabRow transparent
        contentColor = Color.Black,
        indicator = {}, // Remove indicator for cleaner look
        divider = {} // Remove default divider for a cleaner look
    ) {
        tabItems.forEachIndexed { index, text ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = {
                    Text(
                        text,
                        fontSize = 12.sp,
                        color = if (selectedTabIndex == index) Color.White else Color.Black
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 4.dp) // Space between tabs
                    .background(
                        if (selectedTabIndex == index) Color(0xFF00A89D) else Color(0xFFF2F3F2),
                        shape = RoundedCornerShape(16.dp) // Rounded corners for each tab
                    )
                    .padding(horizontal = 1.dp, vertical = 4.dp) // Adjust spacing inside tabs
                    .height(32.dp) // Set a fixed height for compact tabs
            )
        }
    }
}
