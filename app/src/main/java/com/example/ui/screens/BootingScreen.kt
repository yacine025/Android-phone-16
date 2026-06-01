package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
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

        // Google Pixel Boot Loader / Pulse Block
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131314)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0xFF2E3134), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing Google Icon Animation
                val infiniteTransition = rememberInfiniteTransition(label = "boot_pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1F20))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF4285F4),
                            radius = size.width / 2.2f,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    Text(
                        text = "G",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "VirtuOS Google System",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = titleText,
                    fontSize = 13.sp,
                    color = Color(0xFFC4C6D0),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Multi-colored custom linear progress bar matching Google's logo Colors
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(4.dp)
                        .clip(CircleShape),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4285F4)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEA4335)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFBBC05)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF34A853)))
                }
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
