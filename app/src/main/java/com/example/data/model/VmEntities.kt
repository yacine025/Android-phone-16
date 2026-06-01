package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vm_config")
data class VmConfig(
    @PrimaryKey val id: Int = 1,
    val ramSizeGb: Int = 6,
    val storageSizeGb: Int = 128,
    val androidVersion: String = "Android 13.0 (API 33)",
    val resolution: String = "1080 x 2400 (FHD+)",
    val rootEnabled: Boolean = false,
    val googleServicesEnabled: Boolean = true
)

@Entity(tableName = "virtual_files")
data class VirtualFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val content: String = "",
    val path: String, // e.g., "/" (root) or "/sdcard", "/system/bin"
    val isFolder: Boolean,
    val sizeBytes: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "superuser_logs")
data class SuperuserLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val command: String,
    val timestamp: Long = System.currentTimeMillis(),
    val granted: Boolean
)

@Entity(tableName = "virtual_apps")
data class VirtualApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val isInstalled: Boolean = false,
    val iconName: String,
    val cpuUsageSimulated: Double = 0.0
)
