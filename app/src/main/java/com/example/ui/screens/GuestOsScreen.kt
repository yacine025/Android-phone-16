package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VirtualApp
import com.example.data.model.VirtualFile
import com.example.data.model.VmConfig
import com.example.ui.vm.VmViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestOsScreen(
    viewModel: VmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.vmConfig.collectAsState()
    val files by viewModel.allFiles.collectAsState()
    val apps by viewModel.allApps.collectAsState()
    
    val currentConfig = config ?: VmConfig()

    // Screen State
    var showAppStoreMenu by remember { mutableStateOf(false) }
    var screenshotFlash by remember { mutableStateOf(false) }

    // Desktop Styling Specs
    val phoneBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )
    val taskbarBg = Color(0xAE1A2230)
    val appCardBg = Color(0xFF1E293B)
    val borderCyan = Color(0xFF00FFCC)

    // Handle screenshot camera flash effect
    if (screenshotFlash) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {}
        )
        LaunchedEffect(Unit) {
            delay(150)
            screenshotFlash = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // OUTER DEVICE SHELL LAYOUT MODEL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070B11))
                    .padding(8.dp)
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(phoneBg)
            ) {
                // PHONE CONTAINER BODY
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Sleek Virtual System Status Bar
                    GuestStatusBar(currentConfig, viewModel)

                    // 2. Active Screen Content Area (App window OR Desktop Grid Panel)
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxWidth()
                    ) {
                        if (viewModel.openAppPackage == null) {
                            // SHOW DESKTOP LAUNCHER
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "بوابة البيئة المعزولة | VirtuOS Space",
                                    fontSize = 12.sp,
                                    color = borderCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                val installedApps = apps.filter { it.isInstalled }
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(installedApps) { app ->
                                        GuestAppIcon(app = app, onClick = {
                                            viewModel.openGuestApp(app.packageName)
                                        })
                                    }

                                    // Custom visual App store button
                                    item {
                                        GuestMarketShortcut(onClick = { showAppStoreMenu = true })
                                    }
                                }

                                // Quick Instructions Tips
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💡",
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = "اسحب الزر العائم لفتح قائمة لوحة التحكم المصغرة السريعة، أو اضغط رمز المتجر لتثبيت المزيد من الأدوات المتقدمة.",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        } else {
                            // SHOW OPEN APP CONTAINER WINDOW
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F172A))
                            ) {
                                when (viewModel.openAppPackage) {
                                    "com.vmos.terminal" -> TerminalAppContent(viewModel)
                                    "com.vmos.files" -> FileManagerAppContent(viewModel, files)
                                    "com.vmos.magisk" -> SuperuserAppContent(viewModel)
                                    "com.vmos.settings" -> GuestSettingsAppContent(viewModel, currentConfig)
                                    "com.vmos.browser" -> GuestWebBrowserAppContent(viewModel)
                                    "com.vmos.editor" -> GuestDevEditorAppContent(viewModel)
                                    "com.vmos.monitor" -> GuestMonitorAppContent(viewModel)
                                    "com.vmos.calc" -> GuestCalculatorAppContent(viewModel)
                                    else -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("App package not initialized: ${viewModel.openAppPackage}", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. App Store Overlay Sheet
                if (showAppStoreMenu) {
                    GuestAppStoreTray(
                        viewModel = viewModel,
                        apps = apps,
                        onClose = { showAppStoreMenu = false }
                    )
                }

                // 4. Floating Action Ball Menu (Draggable Widget Overlay)
                FloatingActionBallMenu(
                    viewModel = viewModel,
                    onCaptureScreenshot = {
                        screenshotFlash = true
                        Toast.makeText(context, "تم حفظ لقطة الشاشة في مجلد /sdcard/downloads !", Toast.LENGTH_SHORT).show()
                        viewModel.createTextFileInExplorer(
                            name = "screenshot_${System.currentTimeMillis()}.png.meta",
                            content = "SCREENSHOT METADATA\nCaptured in VirtuOS Guest Screen\nTime: ${Date()}\nResolution: ${currentConfig.resolution}"
                        )
                    }
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// SMARTPHONE DECORATIVE BAR
// ----------------------------------------------------------------------------
@Composable
fun GuestStatusBar(config: VmConfig, viewModel: VmViewModel) {
    val textMuted = Color(0xFFBAC2DE)
    val greenGlow = Color(0xFF00FF66)
    
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val currentTime = timeFormatter.format(Date())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Status Indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = currentTime,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            if (config.rootEnabled) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(greenGlow.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "# ROOT",
                        color = greenGlow,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Center: Android Version
        Text(
            text = config.androidVersion,
            color = textMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold
        )

        // Right Side: Network & Simulated Resources status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Simulated net", tint = Color.White, modifier = Modifier.size(10.dp))
            Text(
                text = "LTE",
                fontSize = 9.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.Info, contentDescription = "Battery", tint = Color.White, modifier = Modifier.size(11.dp))
        }
    }
}

// ----------------------------------------------------------------------------
// GUEST APP SHORTCUT GRAPHIC
// ----------------------------------------------------------------------------
@Composable
fun GuestAppIcon(app: VirtualApp, onClick: () -> Unit) {
    val emoji = when (app.iconName) {
        "terminal" -> "💻"
        "folder" -> "📁"
        "shield" -> "🛡️"
        "settings" -> "⚙️"
        "web" -> "🌐"
        "edit" -> "📝"
        "monitor" -> "📊"
        "calc" -> "🧮"
        else -> "🤖"
    }

    val iconBg = Brush.radialGradient(
        colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
    )

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.appName,
            color = Color.White,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GuestMarketShortcut(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(listOf(Color(0xFF0369A1), Color(0xFF0F172A))))
                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🎁", fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "المتجر | Store",
            color = Color(0xFF38BDF8),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

// ----------------------------------------------------------------------------
// INTERACTIVE APP CONTENT: TERMINAL CONSOLE
// ----------------------------------------------------------------------------
@Composable
fun TerminalAppContent(viewModel: VmViewModel) {
    val history = viewModel.terminalHistory
    var currentInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        // App Titlebar
        AppHeader(title = "Terminal Emulator (Shell)", onClose = { viewModel.closeGuestApp() })

        // Shell Outputs list
        LazyColumn(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(history) { line ->
                Text(
                    text = line,
                    color = if (line.startsWith("#") || line.startsWith("$")) Color(0xFF58A6FF) else Color(0xFF39D353),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // Quick Input Shortcut Utility commands row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val quickKeys = listOf("su", "ls", "cd /sdcard", "cd ..", "clear", "help", "status")
            quickKeys.forEach { key ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF30363D))
                        .clickable {
                            if (key == "clear") {
                                history.clear()
                            } else {
                                viewModel.executeConsoleCommand(key)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = key,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Real text field typing layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1117))
                .border(1.dp, Color(0xFF30363D))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (viewModel.hasRootAccessInTerminal) "root@virt_os:~# " else "guest@virt_os:~$ ",
                color = if (viewModel.hasRootAccessInTerminal) Color.Red else Color(0xFF58A6FF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            TextField(
                value = currentInput,
                onValueChange = { currentInput = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 12.sp, 
                    fontFamily = FontFamily.Monospace
                ),
                singleLine = true,
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        viewModel.executeConsoleCommand(currentInput)
                        currentInput = ""
                        keyboardController?.hide()
                    }
                )
            )

            Icon(
                Icons.Default.Send,
                contentDescription = "Send",
                tint = Color(0xFF39D353),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        viewModel.executeConsoleCommand(currentInput)
                        currentInput = ""
                        keyboardController?.hide()
                    }
            )
        }
    }
}

// ----------------------------------------------------------------------------
// INTERACTIVE APP CONTENT: FILE EXPLORER
// ----------------------------------------------------------------------------
@Composable
fun FileManagerAppContent(viewModel: VmViewModel, files: List<VirtualFile>) {
    val currentPath = viewModel.explorerCurrentPath
    val folderFiles = files.filter { f -> f.path == currentPath || (f.path == "/" && currentPath == "/") }

    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var fileContentToRead by remember { mutableStateOf<VirtualFile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
    ) {
        AppHeader(title = "File Explorer (Isolated ROM)", onClose = { viewModel.closeGuestApp() })

        // Explorer Path Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📍 Path: ",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentPath,
                    color = Color(0xFF00FFCC),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            if (currentPath != "/") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF334155))
                        .clickable {
                            val parent = if (currentPath.substringBeforeLast('/').isEmpty()) "/" else currentPath.substringBeforeLast('/')
                            viewModel.explorerCurrentPath = parent
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("رجوع ⤴", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Action controls (New folder, New file)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showCreateFileDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58A6FF)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New File", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ملف جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showCreateFolderDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Folder", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("مجلد جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Directory Content List
        if (folderFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("المجلد فارغ", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val displayList = if (currentPath == "/") {
                    folderFiles.filter { it.path == "/" }
                } else {
                    folderFiles.filter { it.path == currentPath && it.name != currentPath.substringAfterLast("/") }
                }

                items(displayList) { f ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                            .clickable {
                                if (f.isFolder) {
                                    val newP = if (currentPath == "/") "/${f.name}" else "$currentPath/${f.name}"
                                    viewModel.explorerCurrentPath = newP
                                } else {
                                    fileContentToRead = f
                                }
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (f.isFolder) "📁" else "📄", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = f.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (!f.isFolder) {
                                    Text(
                                        text = "${f.sizeBytes} Bytes",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF476F),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.deleteFileInExplorer(f) }
                        )
                    }
                }
            }
        }

        // Sub dialogues
        if (showCreateFileDialog) {
            FileGeneratorDialog(
                onDismiss = { showCreateFileDialog = false },
                onConfirm = { nameStr, contentStr ->
                    viewModel.createTextFileInExplorer(nameStr, contentStr)
                    showCreateFileDialog = false
                }
            )
        }

        if (showCreateFolderDialog) {
            FolderGeneratorDialog(
                onDismiss = { showCreateFolderDialog = false },
                onConfirm = { nameStr ->
                    viewModel.createFolderInExplorer(nameStr)
                    showCreateFolderDialog = false
                }
            )
        }

        // Reader dialog file reader
        fileContentToRead?.let { rf ->
            AlertDialog(
                onDismissRequest = { fileContentToRead = null },
                title = { Text(rf.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .background(Color.Black)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = rf.content,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF39D353)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { fileContentToRead = null }) {
                        Text("موافق")
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

// ----------------------------------------------------------------------------
// INTERACTIVE APP CONTENT: SUPERUSER REGISTER MANAGER (MAGISK CLONE)
// ----------------------------------------------------------------------------
@Composable
fun SuperuserAppContent(viewModel: VmViewModel) {
    val logs by viewModel.allLogs.collectAsState()
    val config by viewModel.vmConfig.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
    ) {
        AppHeader(title = "Magisk SuperUser (Root Engine)", onClose = { viewModel.closeGuestApp() })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF252538))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("الحالة الأساسية للنواة:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (config?.rootEnabled == true) "إمكانية الاستلام مُمكّنة" else "الجذر معطل تماماً",
                    color = if (config?.rootEnabled == true) Color(0xFF39D353) else Color(0xFFEF476F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Switch(
                checked = config?.rootEnabled == true,
                onCheckedChange = { viewModel.toggleRootGlobally(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "طلبات التراخيص والعمليات:",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (logs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد سجلات مصادقة روت.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                items(logs) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E2E44))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(log.appName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(log.command, color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Text(
                            text = if (log.granted) "تم السماح ✅" else "مرفوض ❌",
                            color = if (log.granted) Color(0xFF39D353) else Color(0xFFEF476F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GUEST APP CONTENT: SETTINGS
// ----------------------------------------------------------------------------
@Composable
fun GuestSettingsAppContent(viewModel: VmViewModel, config: VmConfig) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        AppHeader(title = "VirtuOS System Settings", onClose = { viewModel.closeGuestApp() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("معلومات النظام الافتراضي", color = Color(0xFF00FFCC), fontSize = 13.sp, fontWeight = FontWeight.Bold)

            SettingsLabelRow("الإصدار البرمجي الأساسي", config.androidVersion)
            SettingsLabelRow("محرك النواة", "Linux virtualization kernel v6.1-A32")
            SettingsLabelRow("الذاكرة العشوائية (RAM)", "${config.ramSizeGb} GB RAM Alloc")
            SettingsLabelRow("المساحة التخزينية (ROM)", "${config.storageSizeGb} GB Volume SSD")
            SettingsLabelRow("دقة شاشة الع عرض", config.resolution)
            SettingsLabelRow("شبكة الاتصال الافتراضية", "Virtual Bridge wlan0")
            SettingsLabelRow("عنوان خادم الروت", "Local su daemon [Magisk v26]")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(viewModel.getApplication(), "النظام مستقر. لا توجد تحديثات جديدة حالياً.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC), contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("التحقق من وجود تحديثات بالنظام", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GUEST APP CONTENT: WEB BROWSER
// ----------------------------------------------------------------------------
@Composable
fun GuestWebBrowserAppContent(viewModel: VmViewModel) {
    var webUrl by remember { mutableStateOf(viewModel.browserAddress) }

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(title = "VirtuOS Net Web Browser", onClose = { viewModel.closeGuestApp() })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = webUrl,
                onValueChange = { webUrl = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 11.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFCC),
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color(0xFF0D1117),
                    unfocusedContainerColor = Color(0xFF0D1117)
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { viewModel.openSimulatedWebsite(webUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC), contentColor = Color.Black),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("الانتقال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Divider(color = Color.Gray.copy(alpha = 0.2f))

        // Virtual content page loader
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("الموقع: $webUrl", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(10.dp))

            if (webUrl.contains("developer.android.com")) {
                Text("Android Developer Base Sandbox", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This virtual space allows developers to test compiled apps without risk. Superuser su binaries are mounted automatically when the 'su' kernel instruction is parsed.",
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            } else if (webUrl.contains("magisk")) {
                Text("Magisk Root Installer Site", color = Color(0xFFEF476F), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Welcome to Magisk Virtual Portal. Magisk is the suite for routing guest permissions without modifying parent host operating layers directly.",
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            } else {
                Text("تصفح الويب الافتراضي الآمن", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "أنت تستعرض الويب من خلال قناة اتصال محلية مشفرة. توفر VirtuOS حماية كاملة ضد تتبع النشاط والمواقع الضارة.",
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GUEST APP CONTENT: DEV CODE EDITOR IDE
// ----------------------------------------------------------------------------
@Composable
fun GuestDevEditorAppContent(viewModel: VmViewModel) {
    var codeText by remember {
        mutableStateOf(
            "// VirtuOS Java/Kotlin Sandbox compiler v1.1\n" +
            "fun main() {\n" +
            "    println(\"Starting isolated sandbox threat test...\")\n" +
            "    val kernel = System.getProperty(\"os.arch\")\n" +
            "    println(\"Running on Virtual Architecture: \$kernel\")\n" +
            "    println(\"Checking mount files... /sdcard/welcome_virtuos.txt\")\n" +
            "    println(\"TEST SUCCESSFUL! VM IS SECURE.\")\n" +
            "}"
        )
    }

    var consoleLogsStr by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(title = "Dev Code Editor Platform", onClose = { viewModel.closeGuestApp() })

        OutlinedTextField(
            value = codeText,
            onValueChange = { codeText = it },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF39D353)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFCC),
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    consoleLogsStr = "Compiling VirtuOS Virtual Assembly class...\n" +
                            "Starting Java Virtual System core engine thread...\n" +
                            "--------------------------------------------------\n" +
                            "Starting isolated sandbox threat test...\n" +
                            "Running on Virtual Architecture: aarch64\n" +
                            "Checking mount files... /sdcard/welcome_virtuos.txt (Found: OK)\n" +
                            "TEST SUCCESSFUL! VM IS SECURE.\n" +
                            "--------------------------------------------------\n" +
                            "Process terminated with exit code 0."
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39D353), contentColor = Color.Black),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("تشغيل الكود البرمجي RUN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = { consoleLogsStr = "" }) {
                Text("مسح الكونسول", color = Color.White, fontSize = 11.sp)
            }
        }

        if (consoleLogsStr.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.Black)
                    .border(1.dp, Color.DarkGray)
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = consoleLogsStr,
                            color = Color(0xFFFFD166),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GUEST APP CONTENT: CPU MONITOR
// ----------------------------------------------------------------------------
@Composable
fun GuestMonitorAppContent(viewModel: VmViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(title = "VirtuOS X-Ray Hardware-Logger", onClose = { viewModel.closeGuestApp() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("إحصائيات الاتصال ونواة المعالجة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pathWidth = size.width
                    val pathHeight = size.height
                    val graphPoints = 30
                    
                    for (i in 1..4) {
                        val y = pathHeight * (i.toFloat() / 5)
                        drawLine(
                            color = Color.DarkGray.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(pathWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    for (i in 0..graphPoints) {
                        val x = pathWidth * (i.toFloat() / graphPoints)
                        val frequency = 0.2f
                        val phase = waveOffset * 0.15f
                        val heightMultiplier = 35f
                        val yOffset = pathHeight / 2 + (Math.sin((i * frequency + phase).toDouble()) * heightMultiplier).toFloat()
                        
                        drawCircle(
                            color = Color(0xFF00FFCC),
                            radius = 3f,
                            center = Offset(x, yOffset)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "مغذي الطاقة (VM)",
                    value = "5.0 Volts",
                    details = "إدخال مستمر",
                    accent = Color(0xFF00FFCC),
                    cardBg = Color(0xFF0F172A),
                    borderGray = Color.DarkGray,
                    textWhite = Color.White,
                    textMuted = Color.Gray,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "تردد النواة",
                    value = "2.84 GHz",
                    details = "معالج ARM64",
                    accent = Color(0xFFFFD166),
                    cardBg = Color(0xFF0F172A),
                    borderGray = Color.DarkGray,
                    textWhite = Color.White,
                    textMuted = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("الشبكة وحزم الاتصال:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("- Bridge Adapter (wlan0): 192.168.10.2", color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("- DNS Virt server: 8.8.8.8 (Google secure)", color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("- Active listening ADB ports: 5555", color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GUEST APP CONTENT: CALCULATOR CORE
// ----------------------------------------------------------------------------
@Composable
fun GuestCalculatorAppContent(viewModel: VmViewModel) {
    var displayStr by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
    ) {
        AppHeader(title = "VirtuOS Assembly Calculator", onClose = { viewModel.closeGuestApp() })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.Black)
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = if (displayStr.isEmpty()) "0" else displayStr,
                color = Color(0xFF00FFCC),
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(color = Color.DarkGray)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val buttons = listOf(
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "C", "0", "=", "+"
            )

            items(buttons) { b ->
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF334155))
                        .clickable {
                            when (b) {
                                "C" -> displayStr = ""
                                "=" -> {
                                    if (displayStr.isNotEmpty()) {
                                        try {
                                            val parts = displayStr.split("(?<=[-+*/])|(?=[-+*/])".toRegex())
                                            if (parts.size >= 3) {
                                                val n1 = parts[0].trim().toDouble()
                                                val op = parts[1].trim()
                                                val n2 = parts[2].trim().toDouble()
                                                val resultValue = when (op) {
                                                    "+" -> n1 + n2
                                                    "-" -> n1 - n2
                                                    "*" -> n1 * n2
                                                    "/" -> n1 / n2
                                                    else -> 0.0
                                                }
                                                displayStr = if (resultValue % 1.0 == 0.0) resultValue.toInt().toString() else resultValue.toString()
                                            }
                                        } catch (e: Exception) {
                                            displayStr = "ERR"
                                        }
                                    }
                                }
                                else -> {
                                    if (displayStr == "ERR") displayStr = ""
                                    displayStr += b
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = b,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// MISSING HELPER COMPOSABLES SECTION
// ----------------------------------------------------------------------------
@Composable
fun GuestAppStoreTray(
    viewModel: VmViewModel,
    apps: List<VirtualApp>,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "متجر حزم البرمجيات الافتراضية | OS App Market",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(apps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (app.iconName) {
                                    "terminal" -> "💻"
                                    "folder" -> "📁"
                                    "shield" -> "🛡️"
                                    "settings" -> "⚙️"
                                    "web" -> "🌐"
                                    "edit" -> "📝"
                                    "monitor" -> "📊"
                                    "calc" -> "🧮"
                                    else -> "🤖"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(app.appName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(app.packageName, color = Color.Gray, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleAppInstallationState(app) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (app.isInstalled) Color(0xFFEF476F) else Color(0xFF00FFCC),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (app.isInstalled) "إلغاء التثبيت" else "تثبيت حزمة ROM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingActionBallMenu(
    viewModel: VmViewModel,
    onCaptureScreenshot: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(400f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF00FFCC), Color(0xFF0D9488))))
                .border(2.dp, Color.White, CircleShape)
                .clickable { expanded = !expanded }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Floating Ball", tint = Color.Black, modifier = Modifier.size(24.dp))
        }

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { expanded = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .width(280.dp)
                        .padding(16.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لوحة التحكم السريعة | Quick Menu",
                            color = Color(0xFF00FFCC),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Divider(color = Color.DarkGray)

                        FloatingMenuItem(
                            icon = Icons.Default.Send,
                            title = "شاشة لقطة كاميرا (Screenshot)",
                            onClick = {
                                expanded = false
                                onCaptureScreenshot()
                            }
                        )

                        if (viewModel.openAppPackage != null) {
                            FloatingMenuItem(
                                icon = Icons.Default.ExitToApp,
                                title = "إغلاق التطبيق النشط الحالي",
                                onClick = {
                                    viewModel.closeGuestApp()
                                    expanded = false
                                }
                            )
                        }

                        FloatingMenuItem(
                            icon = Icons.Default.Home,
                            title = "الخروج للقائمة المضيفة (Host)",
                            onClick = {
                                viewModel.activeScreen = com.example.ui.vm.SystemUiScreen.HOST_DASHBOARD
                                expanded = false
                            }
                        )

                        FloatingMenuItem(
                            icon = Icons.Default.Refresh,
                            title = "إعادة تشغيل النظام الوهمي",
                            onClick = {
                                viewModel.shutdownVirtualMachine()
                                viewModel.bootVirtualMachine()
                                expanded = false
                            }
                        )

                        FloatingMenuItem(
                            icon = Icons.Default.Close,
                            title = "إيقاف تشغيل النظام (Shutdown VM)",
                            colors = Color(0xFFEF476F),
                            onClick = {
                                viewModel.shutdownVirtualMachine()
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    colors: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = colors, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = colors, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AppHeader(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            Icons.Default.Close,
            contentDescription = "Close VM application tab",
            tint = Color(0xFFEF476F),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onClose)
        )
    }
}

@Composable
fun SettingsLabelRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.LightGray, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}




@Composable
fun FileGeneratorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء ملف جديد", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الملف (e.g. key.txt)") },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp)
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("محتوى الملف النصي") },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotEmpty()) onConfirm(name, content) }) {
                Text("إنشاء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}

@Composable
fun FolderGeneratorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء مجلد جديد", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم المجلد (e.g. sys_bin)") },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp)
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotEmpty()) onConfirm(name) }) {
                Text("انشاء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
