package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SuperuserLog
import com.example.data.model.VmConfig
import com.example.ui.vm.VmViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    viewModel: VmViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.vmConfig.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val apps by viewModel.allApps.collectAsState()

    val currentConfig = config ?: VmConfig()

    // Host Google Material You Palette Style Constants
    val bgColor = Color(0xFF131314) // Google Charcoal Black
    val cardBg = Color(0xFF1E1F20) // Soft Google Card Dark Background
    val accentColor = Color(0xFF8AB4F8) // Pixel Blue
    val terminalGreen = Color(0xFF81C995) // Google Green
    val borderGray = Color(0xFF2E3134) // Soft Material You Borderline
    val textWhite = Color(0xFFE3E2E6) // Crisp white-ish text
    val textMuted = Color(0xFFC4C6D0) // Subdued gray text

    // User Settings interactive state
    var ramSize by remember(currentConfig) { mutableStateOf(currentConfig.ramSizeGb) }
    var storageSize by remember(currentConfig) { mutableStateOf(currentConfig.storageSizeGb) }
    var androidVersion by remember(currentConfig) { mutableStateOf(currentConfig.androidVersion) }
    var rootEnabled by remember(currentConfig) { mutableStateOf(currentConfig.rootEnabled) }
    var googleSvc by remember(currentConfig) { mutableStateOf(currentConfig.googleServicesEnabled) }
    var resolution by remember(currentConfig) { mutableStateOf(currentConfig.resolution) }

    // Dropdown UI triggers
    var showRamMenu by remember { mutableStateOf(false) }
    var showStorageMenu by remember { mutableStateOf(false) }
    var showAndroidMenu by remember { mutableStateOf(false) }
    var showResMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Elegant Google Pixel / Material You "At a Glance" & Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // At a Glance layout with date, weather, and status
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val currentDay = SimpleDateFormat("EEEE, d MMMM", Locale("ar")).format(Date())
                        Text(
                            text = currentDay,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textWhite
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🌤️ 22°C", color = textMuted, fontSize = 13.sp)
                            Box(modifier = Modifier.size(3.dp).background(textMuted, CircleShape))
                            Text("محرك المحاكي جاهز 🔒", color = Color(0xFF81C995), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Google Signature Colorful Dots Row
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFF4285F4), CircleShape))
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFFEA4335), CircleShape))
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFFFBBC05), CircleShape))
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFF34A853), CircleShape))
                    }
                }
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // Card for Main VirtuOS Brand Header with Google Vibe
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, borderGray, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333538)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "VirtuOS Emulator",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "بنمط بيئة Google Pixel و أندرويد الخام الأصيل",
                            fontSize = 11.sp,
                            color = textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 2. Interactive VM Orbit Visualizer with Pulse Controls
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(1.dp, borderGray, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Custom Orbit Animation
                    VmOrbitCanvas(accentColor, terminalGreen)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "الحالة الأساسية: مغلق",
                        color = Color(0xFFF25C54),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.bootVirtualMachine() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Boot")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تشغيل النظام الافتراضي | BOOT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 3. Simulated Hardware Metrics Dashboard Panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "مستشعر المعالج (CPU)",
                    value = "${"%.1f".format(viewModel.simulatedCpuLoad)} %",
                    details = "8 نوى افتراضية",
                    accent = terminalGreen,
                    cardBg = cardBg,
                    borderGray = borderGray,
                    textWhite = textWhite,
                    textMuted = textMuted,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "الذاكرة المؤقتة (RAM)",
                    value = "${ramSize} GB",
                    details = "مستقر (${"%.1f".format(viewModel.simulatedRamUsedGb)} GB مستخدم)",
                    accent = accentColor,
                    cardBg = cardBg,
                    borderGray = borderGray,
                    textWhite = textWhite,
                    textMuted = textMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "مساحة التخزين",
                    value = "${storageSize} GB",
                    details = "مجزأ (12.4GB نظام)",
                    accent = Color(0xFFFFD166),
                    cardBg = cardBg,
                    borderGray = borderGray,
                    textWhite = textWhite,
                    textMuted = textMuted,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "حرارة المحاكي",
                    value = "${"%.1f".format(viewModel.simulatedTemperature)} °C",
                    details = "تبريد ذاتي ذكي",
                    accent = Color(0xFFEF476F),
                    cardBg = cardBg,
                    borderGray = borderGray,
                    textWhite = textWhite,
                    textMuted = textMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Configuration Controls (Developer Panel Setup Menu)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, borderGray, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعدادات العتاد الافتراضي | VM Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textWhite
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle: Root / Superuser Toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text("صلاحيات الجذر (Superuser Root)", fontSize = 14.sp, color = textWhite, fontWeight = FontWeight.SemiBold)
                        Text("تمكين Magisk للتحكم في برامج الروت وحقن الأكواد", fontSize = 11.sp, color = textMuted)
                    }
                    Switch(
                        checked = rootEnabled,
                        onCheckedChange = {
                            rootEnabled = it
                            viewModel.toggleRootGlobally(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = terminalGreen,
                            checkedTrackColor = terminalGreen.copy(alpha = 0.4f)
                        )
                    )
                }

                Divider(color = borderGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 12.dp))

                // Toggle: Google Services Framework Integration simulation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text("خدمات Google Play الافتراضية", fontSize = 14.sp, color = textWhite, fontWeight = FontWeight.SemiBold)
                        Text("تثبيت حزمة Gapps مصغرة لتشغيل التطبيقات المعتمدة", fontSize = 11.sp, color = textMuted)
                    }
                    Switch(
                        checked = googleSvc,
                        onCheckedChange = {
                            googleSvc = it
                            viewModel.updateConfig(ramSize, storageSize, androidVersion, resolution, rootEnabled, it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = accentColor,
                            checkedTrackColor = accentColor.copy(alpha = 0.4f)
                        )
                    )
                }

                Divider(color = borderGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 12.dp))

                // Dropdowns: RAM Sizes Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("حجم الرام المخصص (Allocated RAM)", fontSize = 14.sp, color = textWhite)
                    Box {
                        Text(
                            text = "$ramSize GB ▾",
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showRamMenu = true }
                                .padding(6.dp)
                        )
                        DropdownMenu(
                            expanded = showRamMenu,
                            onDismissRequest = { showRamMenu = false },
                            modifier = Modifier.background(cardBg)
                        ) {
                            listOf(2, 4, 6, 8, 12).forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size GB", color = textWhite) },
                                    onClick = {
                                        ramSize = size
                                        viewModel.updateConfig(size, storageSize, androidVersion, resolution, rootEnabled, googleSvc)
                                        showRamMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dropdowns: ROM Select
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("التخزين الافتراضي (Virtual Storage)", fontSize = 14.sp, color = textWhite)
                    Box {
                        Text(
                            text = "$storageSize GB ▾",
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showStorageMenu = true }
                                .padding(6.dp)
                        )
                        DropdownMenu(
                            expanded = showStorageMenu,
                            onDismissRequest = { showStorageMenu = false },
                            modifier = Modifier.background(cardBg)
                        ) {
                            listOf(16, 32, 64, 128, 256, 512).forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size GB", color = textWhite) },
                                    onClick = {
                                        storageSize = size
                                        viewModel.updateConfig(ramSize, size, androidVersion, resolution, rootEnabled, googleSvc)
                                        showStorageMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dropdowns: Android Versions Config
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إصدار نظام التشغيل (OS Version)", fontSize = 14.sp, color = textWhite)
                    Box {
                        Text(
                            text = "$androidVersion ▾",
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showAndroidMenu = true }
                                .padding(6.dp)
                        )
                        DropdownMenu(
                            expanded = showAndroidMenu,
                            onDismissRequest = { showAndroidMenu = false },
                            modifier = Modifier.background(cardBg)
                        ) {
                            listOf(
                                "Android 14.0 (API 34)",
                                "Android 13.0 (API 33)",
                                "Android 12.0 (API 31)",
                                "Android 11.0 (API 30)"
                            ).forEach { ver ->
                                DropdownMenuItem(
                                    text = { Text(ver, color = textWhite) },
                                    onClick = {
                                        androidVersion = ver
                                        viewModel.updateConfig(ramSize, storageSize, ver, resolution, rootEnabled, googleSvc)
                                        showAndroidMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dropdowns: Virtual screen canvas layout resolution
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("دقة العرض (Virtual Resolution)", fontSize = 14.sp, color = textWhite)
                    Box {
                        Text(
                            text = "$resolution ▾",
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showResMenu = true }
                                .padding(6.dp)
                        )
                        DropdownMenu(
                            expanded = showResMenu,
                            onDismissRequest = { showResMenu = false },
                            modifier = Modifier.background(cardBg)
                        ) {
                            listOf(
                                "1080 x 2400 (FHD+)",
                                "1440 x 3200 (2K+)",
                                "720 x 1600 (HD)",
                                "480 x 800 (Compact)"
                            ).forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r, color = textWhite) },
                                    onClick = {
                                        resolution = r
                                        viewModel.updateConfig(ramSize, storageSize, androidVersion, r, rootEnabled, googleSvc)
                                        showResMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Root Logs Tracker (Superuser SQLite Logging Database Monitor)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, borderGray, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Logs",
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سجل صلاحيات الجذر | Superuser Logs",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textWhite
                        )
                    }

                    if (logs.isNotEmpty()) {
                        Text(
                            text = "مسأل الكل",
                            color = Color(0xFFEF476F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { viewModel.clearLogs() }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد سجلات تراخيص حتى الآن. تطلب التطبيقات صلاحية الروت وقت التشغيل.",
                            color = textMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val filteredLogs = when (viewModel.selectedLogFilter) {
                        "GRANTED" -> logs.filter { it.granted }
                        "DENIED" -> logs.filter { !it.granted }
                        else -> logs
                    }

                    // Selector row filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL" to "الكل", "GRANTED" to "المقبولة", "DENIED" to "المرفوضة").forEach { (key, label) ->
                            val isSelected = viewModel.selectedLogFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) accentColor else borderGray.copy(alpha = 0.5f))
                                    .clickable { viewModel.selectedLogFilter = key }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else textWhite
                                )
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        filteredLogs.take(5).forEach { log ->
                            SuperuserLogItem(log, textWhite, textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VmOrbitCanvas(accentColor: Color, centerColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbit")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(
        modifier = Modifier
            .size(160.dp)
            .padding(10.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val parentRadius = size.width / 2.3f

        // Draw central microprocessor core
        drawCircle(
            color = centerColor,
            radius = 16.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = centerColor.copy(alpha = 0.25f),
            radius = 24.dp.toPx(),
            center = center
        )

        // Draw inner spin loop
        drawCircle(
            color = Color.Gray.copy(alpha = 0.15f),
            radius = parentRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw electron dynamic particles
        val radAngle = Math.toRadians(angle.toDouble())
        val x1 = (center.x + parentRadius * cos(radAngle)).toFloat()
        val y1 = (center.y + parentRadius * sin(radAngle)).toFloat()

        drawCircle(
            color = accentColor,
            radius = 7.dp.toPx(),
            center = Offset(x1, y1)
        )

        val radAngleOpposite = Math.toRadians((angle + 180f).toDouble())
        val x2 = (center.x + parentRadius * cos(radAngleOpposite)).toFloat()
        val y2 = (center.y + parentRadius * sin(radAngleOpposite)).toFloat()

        drawCircle(
            color = Color(0xFFFFD166),
            radius = 5.dp.toPx(),
            center = Offset(x2, y2)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    details: String,
    accent: Color,
    cardBg: Color,
    borderGray: Color,
    textWhite: Color,
    textMuted: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, borderGray, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = textMuted,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textWhite,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = details,
            fontSize = 10.sp,
            color = textMuted
        )
    }
}

@Composable
fun SuperuserLogItem(
    log: SuperuserLog,
    textWhite: Color,
    textMuted: Color
) {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = formatter.format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = log.appName,
                fontSize = 13.sp,
                color = textWhite,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = log.command,
                fontSize = 11.sp,
                color = textMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (log.granted) Color(0xFF39D353).copy(alpha = 0.15f) else Color(0xFFEF476F).copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (log.granted) "تم السماح" else "تم الرفض",
                    color = if (log.granted) Color(0xFF39D353) else Color(0xFFEF476F),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedTime,
                fontSize = 9.sp,
                color = textMuted
            )
        }
    }
}
