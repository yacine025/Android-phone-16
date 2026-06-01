package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BootingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GuestOsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vm.SystemUiScreen
import com.example.ui.vm.VmViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: VmViewModel = viewModel()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0D1117)
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (viewModel.activeScreen) {
                            SystemUiScreen.HOST_DASHBOARD -> {
                                DashboardScreen(viewModel = viewModel)
                            }
                            SystemUiScreen.BOOT_SCREEN -> {
                                BootingScreen(viewModel = viewModel)
                            }
                            SystemUiScreen.GUEST_DESKTOP -> {
                                GuestOsScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // Magisk / Superuser Core Interceptor Dialog Popup
                    if (viewModel.showRootPromptDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.respondToRootPrompt(false) },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛡️ ", fontSize = 24.sp)
                                    Text("تأكيد صلاحية الروت | Magisk Root", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            },
                            text = {
                                Column {
                                    Text(
                                        text = "طلب صلاحيات الروت الخارقة:",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.LightGray,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "يرغب أحد التطبيقات بالدخول إلى الملفات الافتراضية العميقة وتعديل بيئة التشغيل.",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                                    Text(
                                        text = "التطبيق المستدعي: Terminal Console\nالوظيفة المطلوبة: System su binary escalation",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFF00FFCC)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.respondToRootPrompt(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC), contentColor = Color.Black)
                                ) {
                                    Text("السماح بالدخول", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { viewModel.respondToRootPrompt(false) }
                                ) {
                                    Text("رفض الطلب وحجب الإجراء", color = Color(0xFFEF476F))
                                }
                            },
                            containerColor = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }
    }
}
