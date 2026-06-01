package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.vm.VmBootState
import com.example.ui.vm.VmViewModel

@Composable
fun BootingScreen(
    viewModel: VmViewModel,
    modifier: Modifier = Modifier
) {
    val logs = viewModel.biosLogs
    val isBooting = viewModel.bootState == VmBootState.BOOTING
    val titleText = if (isBooting) "جاري تشغيل النواة..." else "جاري إيقاف التشغيل المالي..."
    
    val listState = rememberLazyListState()

    // Automatically scroll to the latest log line as they appear
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    // High tech terminal colors
    val bgColor = Color(0xFF070B11)
    val textGreen = Color(0xFF39D353)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Bios Header Vibe
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "V_OS VIRTUAL TERMINAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF58A6FF),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = titleText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = textGreen,
                    strokeWidth = 2.5.dp
                )
            }
        }

        // Scrolling Log Box
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .padding(12.dp)
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = textGreen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                // Append blinking cursor at the bottom of the logs
                item {
                    BlinkingCursor(textGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Footer Status
        Text(
            text = "VirtuOS Hypervisor. All virtual processes are isolated inside private SQLite and SharedPreferences wrappers.",
            fontSize = 9.sp,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BlinkingCursor(cursorColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(14.dp)
                .background(cursorColor.copy(alpha = alpha))
        )
    }
}
