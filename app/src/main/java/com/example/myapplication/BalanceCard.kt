package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.text.font.FontWeight

@Composable
fun BalanceCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0xFFFFFFFF),
        elevation = 4.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .offset(y = (-20).dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Total Balance", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = "¥ 459.22",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = Color.Gray
            )
        }
    }
}
