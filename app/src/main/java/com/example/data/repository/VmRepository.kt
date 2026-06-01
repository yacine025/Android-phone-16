package com.example.data.repository

import com.example.data.database.VmDao
import com.example.data.model.SuperuserLog
import com.example.data.model.VirtualApp
import com.example.data.model.VirtualFile
import com.example.data.model.VmConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VmRepository(private val vmDao: VmDao) {

    val vmConfig: Flow<VmConfig?> = vmDao.getVmConfigFlow()
    val allFiles: Flow<List<VirtualFile>> = vmDao.getAllFilesFlow()
    val allLogs: Flow<List<SuperuserLog>> = vmDao.getAllLogsFlow()
    val allApps: Flow<List<VirtualApp>> = vmDao.getAllAppsFlow()

    suspend fun getVmConfigDirect(): VmConfig? = withContext(Dispatchers.IO) {
        vmDao.getVmConfig()
    }

    suspend fun saveVmConfig(config: VmConfig) = withContext(Dispatchers.IO) {
        vmDao.insertVmConfig(config)
    }

    suspend fun createFile(file: VirtualFile): Long = withContext(Dispatchers.IO) {
        vmDao.insertFile(file)
    }

    suspend fun deleteFile(id: Int) = withContext(Dispatchers.IO) {
        vmDao.deleteFileById(id)
    }

    suspend fun deleteFileByPathAndName(path: String, name: String) = withContext(Dispatchers.IO) {
        vmDao.deleteFileByPathAndName(path, name)
    }

    suspend fun addLog(log: SuperuserLog) = withContext(Dispatchers.IO) {
        vmDao.insertLog(log)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        vmDao.clearLogs()
    }

    suspend fun updateAppInstallation(packageName: String, installed: Boolean) = withContext(Dispatchers.IO) {
        vmDao.updateAppInstallationStatus(packageName, installed)
    }

    suspend fun updateApp(app: VirtualApp) = withContext(Dispatchers.IO) {
        vmDao.updateApp(app)
    }

    /**
     * Pre-populates the Virtual environment with standard android directory layout,
     * settings, and core application engines.
     */
    suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        // 1. Seed configuration if none exists
        val currentConfig = vmDao.getVmConfig()
        if (currentConfig == null) {
            vmDao.insertVmConfig(
                VmConfig(
                    id = 1,
                    ramSizeGb = 6,
                    storageSizeGb = 128,
                    androidVersion = "Android 13.0 (API 33)",
                    resolution = "1080 x 2400 (FHD+)",
                    rootEnabled = false,
                    googleServicesEnabled = true
                )
            )
        }

        // 2. Seed default system and user-installable apps
        val apps = vmDao.getAllApps()
        if (apps.isEmpty()) {
            val defaultApps = listOf(
                VirtualApp("com.vmos.terminal", "Terminal Console", isSystemApp = true, isInstalled = true, iconName = "terminal"),
                VirtualApp("com.vmos.files", "File Manager", isSystemApp = true, isInstalled = true, iconName = "folder"),
                VirtualApp("com.vmos.magisk", "Superuser Root", isSystemApp = true, isInstalled = true, iconName = "shield"),
                VirtualApp("com.vmos.settings", "OS Settings", isSystemApp = true, isInstalled = true, iconName = "settings"),
                VirtualApp("com.vmos.browser", "Web Browser", isSystemApp = false, isInstalled = false, iconName = "web"),
                VirtualApp("com.vmos.editor", "Dev Code Editor", isSystemApp = false, isInstalled = false, iconName = "edit"),
                VirtualApp("com.vmos.monitor", "X-Ray Monitor", isSystemApp = false, isInstalled = false, iconName = "monitor"),
                VirtualApp("com.vmos.calc", "Simulated Calculator", isSystemApp = false, isInstalled = false, iconName = "calc")
            )
            for (app in defaultApps) {
                vmDao.insertApp(app)
            }
        }

        // 3. Seed default filesystem structure
        // Path matches UNIX/Android style
        val samplePathFiles = vmDao.getFilesByPath("/sdcard")
        if (samplePathFiles.isEmpty()) {
            val rootFolder1 = VirtualFile(name = "sdcard", path = "/", isFolder = true)
            val rootFolder2 = VirtualFile(name = "system", path = "/", isFolder = true)
            val rootFolder3 = VirtualFile(name = "root", path = "/", isFolder = true)
            vmDao.insertFile(rootFolder1)
            vmDao.insertFile(rootFolder2)
            vmDao.insertFile(rootFolder3)

            // subfolders
            vmDao.insertFile(VirtualFile(name = "bin", path = "/system", isFolder = true))
            vmDao.insertFile(VirtualFile(name = "documents", path = "/sdcard", isFolder = true))
            vmDao.insertFile(VirtualFile(name = "downloads", path = "/sdcard", isFolder = true))

            // sample text files
            vmDao.insertFile(
                VirtualFile(
                    name = "welcome_virtuos.txt",
                    path = "/sdcard",
                    isFolder = false,
                    content = """============================================
Welcome to VirtuOS Virtual Space
============================================
VirtuOS simulates a complete sandboxed Guest Android Kernel Core.

Features:
- Full Linux/Android shell commands
- On-Demand Superuser Root elevation (try 'su')
- Fully isolated virtual file system
- Interactive package store
- Network and resource visualization dashboards
============================================
Stable, isolated and reliable developer laboratory.""",
                    sizeBytes = 432
                )
            )

            // configuration file
            vmDao.insertFile(
                VirtualFile(
                    name = "kernel.conf",
                    path = "/root",
                    isFolder = false,
                    content = "kernel.version=6.1.25-virtuos-v1\ncpu.cores=8\nvirt.type=hypervisor.m3\nsuperuser.default=ask",
                    sizeBytes = 94
                )
            )

            // init binary shell script
            vmDao.insertFile(
                VirtualFile(
                    name = "init.sh",
                    path = "/system/bin",
                    isFolder = false,
                    content = "echo 'Starting VirtuOS Services...'\nsudo service zygote start\nsudo service system_server start\necho 'Operating System Is Ready.'",
                    sizeBytes = 118
                )
            )
        }
    }
}
