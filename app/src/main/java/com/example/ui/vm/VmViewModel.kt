package com.example.ui.vm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.SuperuserLog
import com.example.data.model.VirtualApp
import com.example.data.model.VirtualFile
import com.example.data.model.VmConfig
import com.example.data.repository.VmRepository
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class VmBootState {
    STOPPED, BOOTING, RUNNING, SHUTTING_DOWN
}

enum class SystemUiScreen {
    HOST_DASHBOARD, BOOT_SCREEN, GUEST_DESKTOP
}

class VmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VmRepository
    
    // UI state flows
    val vmConfig: StateFlow<VmConfig?>
    val allFiles: StateFlow<List<VirtualFile>>
    val allLogs: StateFlow<List<SuperuserLog>>
    val allApps: StateFlow<List<VirtualApp>>

    // Local system ui control state
    var bootState by mutableStateOf(VmBootState.STOPPED)
        private set
    
    var activeScreen by mutableStateOf(SystemUiScreen.HOST_DASHBOARD)

    // Active App in Guest VM (packagename, null = launcher desktop)
    var openAppPackage by mutableStateOf<String?>(null)
    
    // Core Simulated Bios Logs during Boot Loop
    val biosLogs = mutableStateListOf<String>()

    // Simulated Telemetry Metrics
    var simulatedCpuLoad by mutableStateOf(5.4f)
        private set
    var simulatedRamUsedGb by mutableStateOf(1.8f)
        private set
    var simulatedTemperature by mutableStateOf(34.2f)
        private set

    // Terminal Screen variables
    val terminalHistory = mutableStateListOf<String>()
    var terminalCurrentPath by mutableStateOf("/sdcard")
    var hasRootAccessInTerminal by mutableStateOf(false)
    var showRootPromptDialog by mutableStateOf(false)
    private var pendingRootAcceptCallback: (() -> Unit)? = null
    private var pendingRootRejectCallback: (() -> Unit)? = null

    // File Explorer current directory path
    var explorerCurrentPath by mutableStateOf("/sdcard")

    // Simple Browser search address bar
    var browserAddress by mutableStateOf("https://developer.android.com/vmos-engine")
    var browserSearchHistory = mutableStateListOf<String>()

    // Root Access Magisk list manager
    var selectedLogFilter by mutableStateOf("ALL") // "ALL", "GRANTED", "DENIED"

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VmRepository(database.vmDao())
        
        vmConfig = repository.vmConfig.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allFiles = repository.allFiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allApps = repository.allApps.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initial launch seeding
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            // Seed initial browser search history
            browserSearchHistory.addAll(listOf(
                "https://vmos.app/stable-kernels",
                "https://github.com/vmos-engine/virtual-android-core",
                "https://developer.android.com/about",
                "https://magisk.me/installer-superuser"
            ))
        }

        // Live CPU/Metrics Loop Simulation while VM is running
        viewModelScope.launch {
            while (true) {
                delay(1200)
                if (bootState == VmBootState.RUNNING) {
                    val appsCount = allApps.value.filter { it.isInstalled }.size
                    val baseCpu = 4.0f + (appsCount * 1.5f)
                    simulatedCpuLoad = (baseCpu + Random.nextFloat() * 14f).coerceIn(1.0f, 99.0f)
                    
                    val configRam = vmConfig.value?.ramSizeGb ?: 6
                    val ramBase = 1.9f + (appsCount * 0.22f)
                    simulatedRamUsedGb = (ramBase + Random.nextFloat() * 0.4f).coerceIn(1.0f, configRam.toFloat())
                    
                    val tempBase = 32.5f + (simulatedCpuLoad * 0.12f)
                    simulatedTemperature = (tempBase + Random.nextFloat() * 0.8f).coerceIn(28f, 75f)
                } else {
                    simulatedCpuLoad = (1.2f + Random.nextFloat() * 2.5f)
                    simulatedRamUsedGb = 0.82f // minimal host background draw
                    simulatedTemperature = 29.4f
                }
            }
        }
    }

    // Update Virtual Machine Core configuration in settings
    fun updateConfig(ramSize: Int, storageSize: Int, osVersion: String, res: String, root: Boolean, googleSvc: Boolean) {
        viewModelScope.launch {
            val current = vmConfig.value ?: VmConfig()
            val newConfig = current.copy(
                ramSizeGb = ramSize,
                storageSizeGb = storageSize,
                androidVersion = osVersion,
                resolution = res,
                rootEnabled = root,
                googleServicesEnabled = googleSvc
            )
            repository.saveVmConfig(newConfig)
        }
    }

    // Toggle Root Globally from Device Switch Settings
    fun toggleRootGlobally(enabled: Boolean) {
        viewModelScope.launch {
            val current = vmConfig.value ?: VmConfig()
            repository.saveVmConfig(current.copy(rootEnabled = enabled))
            if (!enabled) {
                hasRootAccessInTerminal = false // reset active shell
            }
        }
    }

    // Start / Boot the VM Simulator
    fun bootVirtualMachine() {
        if (bootState != VmBootState.STOPPED) return
        bootState = VmBootState.BOOTING
        activeScreen = SystemUiScreen.BOOT_SCREEN
        biosLogs.clear()
        
        viewModelScope.launch {
            val config = vmConfig.value ?: VmConfig()
            
            // Bios Sequence logs animation
            val bootSequences = listOf(
                "Initializing VirtuOS Engine BIOS core...",
                "Allocating guest hardware containers... (RAM: ${config.ramSizeGb}GB, STORAGE: ${config.storageSizeGb}GB)",
                "Mounting virtual secure sandboxed file systems...",
                "Guest partitions mounted: /system (ext4), /sdcard (vfat), /root (secure_crypt)",
                "Loading guest Linux ARM64 kernel headers... Version 6.1.25-virtuos",
                "Booting initial virtual user space environment... Starting init main thread [PID 1]",
                "Spawning daemon services: servicemanager, keystore, installd...",
                "Initializing Zygote interface... Listening on /dev/socket/zygote",
                "Root Access capability toggled: ${if (config.rootEnabled) "SUPPORTED" else "DISABLED"}",
                "Bootstrapping micro SystemUI environment (Resolution: ${config.resolution})...",
                "Google Services Framework virtualization layer status: ${if(config.googleServicesEnabled) "ACTIVE" else "SANDBOXED_OFF"}",
                "Network interfaces loaded: wlan0 (Bridge virtual 192.168.10.2)",
                "Launching custom VirtuOS Hyper-Launcher v2.2...",
                "Virtual boot sequence complete. Welcome Guest Sandbox Guest OS."
            )

            for (log in bootSequences) {
                biosLogs.add(log)
                delay(300 + (Random.nextLong(200))) // real typing BIOS feel
            }
            
            delay(500)
            bootState = VmBootState.RUNNING
            activeScreen = SystemUiScreen.GUEST_DESKTOP
            
            // clear and initialize terminal history
            terminalHistory.clear()
            terminalHistory.add("VirtuOS Linux Terminal Emulator v1.2")
            terminalHistory.add("Guest Core OS: ${config.androidVersion}")
            terminalHistory.add("Terminal shell initialized at Path: $terminalCurrentPath")
            terminalHistory.add("Run 'help' to examine available shell commands.")
            terminalHistory.add("")
        }
    }

    // Terminate Virtual Machine / Shutdown Guest Environment
    fun shutdownVirtualMachine() {
        if (bootState != VmBootState.RUNNING) return
        bootState = VmBootState.SHUTTING_DOWN
        activeScreen = SystemUiScreen.BOOT_SCREEN
        biosLogs.clear()
        
        viewModelScope.launch {
            val steps = listOf(
                "Sending system shutdown broadcasts to guest services...",
                "Terminating active app packages and processes gracefully...",
                "Stopping Android zygote core threads [PID 36]...",
                "Syncing volatile memory streams to SQLite databases...",
                "Unmounting virtual file system layers...",
                "Deallocating sandboxed memory page tables...",
                "Power state: V_OS POWER_OFF"
            )
            for (step in steps) {
                biosLogs.add(step)
                delay(250)
            }
            delay(400)
            openAppPackage = null
            bootState = VmBootState.STOPPED
            activeScreen = SystemUiScreen.HOST_DASHBOARD
        }
    }

    // Open/Close sandbox guest applications
    fun openGuestApp(packageName: String) {
        if (bootState == VmBootState.RUNNING) {
            openAppPackage = packageName
        }
    }

    fun closeGuestApp() {
        openAppPackage = null
    }

    // Uninstall/Install user package in guest market
    fun toggleAppInstallationState(app: VirtualApp) {
        viewModelScope.launch {
            val newState = !app.isInstalled
            repository.updateAppInstallation(app.packageName, newState)
            if (openAppPackage == app.packageName && !newState) {
                openAppPackage = null // close app if it gets uninstalled
            }
        }
    }

    // Trigger Root Request Popup
    private fun requestRootPrivilege(appName: String, command: String, onGranted: () -> Unit, onDenied: () -> Unit) {
        // If Root is disabled globally, deny immediately
        val isRootEnabled = vmConfig.value?.rootEnabled == true
        if (!isRootEnabled) {
            viewModelScope.launch {
                repository.addLog(SuperuserLog(appName = appName, command = command, granted = false))
            }
            onDenied()
            return
        }

        // Show prompt dialog
        pendingRootAcceptCallback = {
            viewModelScope.launch {
                repository.addLog(SuperuserLog(appName = appName, command = command, granted = true))
                onGranted()
            }
        }
        pendingRootRejectCallback = {
            viewModelScope.launch {
                repository.addLog(SuperuserLog(appName = appName, command = command, granted = false))
                onDenied()
            }
        }
        showRootPromptDialog = true
    }

    fun respondToRootPrompt(grant: Boolean) {
        showRootPromptDialog = false
        if (grant) {
            pendingRootAcceptCallback?.invoke()
        } else {
            pendingRootRejectCallback?.invoke()
        }
        pendingRootAcceptCallback = null
        pendingRootRejectCallback = null
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // Execute shell command in Guest Terminal
    fun executeConsoleCommand(rawInput: String) {
        val input = rawInput.trim()
        if (input.isEmpty()) return

        val promptChar = if (hasRootAccessInTerminal) "#" else "$"
        terminalHistory.add("$promptChar $input")

        val parts = input.split("\\s+".toRegex())
        val command = parts[0].lowercase()
        val args = parts.drop(1)

        when (command) {
            "help", "h" -> {
                terminalHistory.add("Supported Shell Commands:")
                terminalHistory.add("  help / h            - Displays help manual")
                terminalHistory.add("  ls [path]           - List files/directories")
                terminalHistory.add("  cd [path]           - Change directory paths")
                terminalHistory.add("  cat [file]          - Read target text file content")
                terminalHistory.add("  echo [txt] > [file] - Write/Create files securely")
                terminalHistory.add("  rm [file]           - Removes directory/files")
                terminalHistory.add("  mkdir [dir]         - Create new nested directory folders")
                terminalHistory.add("  su                  - Login to Superuser (ROOT) privilege modes")
                terminalHistory.add("  uname -a            - Outputs kernel configuration")
                terminalHistory.add("  top / ps            - Telemetry processes monitoring list")
                terminalHistory.add("  clear               - Empty console logs screen")
                terminalHistory.add("  status              - Fetch OS build parameters diagnostics")
            }
            "clear" -> {
                terminalHistory.clear()
            }
            "uname", "uname -a" -> {
                terminalHistory.add("Linux localhost 6.1.25-virtuos-v1 #1 SMP PREEMPT_DYNAMIC Mon Jun 1 UTC 2026 aarch64 Android")
            }
            "status" -> {
                val cfg = vmConfig.value
                terminalHistory.add("VirtuOS Virtual Core Engine Diagnostics:")
                terminalHistory.add(" - Hardware: Simulated Qualcomm Snapdragon 8 Gen 2 Virt-Layer")
                terminalHistory.add(" - OS Version: ${cfg?.androidVersion}")
                terminalHistory.add(" - Allocated RAM: ${cfg?.ramSizeGb} GB (Active workload: ${"%.1f".format(simulatedRamUsedGb)} GB)")
                terminalHistory.add(" - Simulated Hardware Storage: ${cfg?.storageSizeGb} GB (Allocated system: 12.4 GB)")
                terminalHistory.add(" - Global Superuser Root Toggles: ${if (cfg?.rootEnabled == true) "YES" else "NO"}")
                terminalHistory.add(" - Device Display: ${cfg?.resolution} Virtual Canvas")
            }
            "su" -> {
                if (hasRootAccessInTerminal) {
                    terminalHistory.add("You are already root inside this shell.")
                } else {
                    terminalHistory.add("Contacting Magisk core Superuser daemon...")
                    requestRootPrivilege(
                        appName = "Terminal Console",
                        command = "Root Shell Terminal su",
                        onGranted = {
                            hasRootAccessInTerminal = true
                            terminalHistory.add("Root access GRANTED by Superuser.")
                            terminalHistory.add("Prompt changed. Current UID: 0 (root).")
                        },
                        onDenied = {
                            terminalHistory.add("Permission DENIED by Superuser.")
                        }
                    )
                }
            }
            "ls" -> {
                val targetPath = if (args.isNotEmpty()) normalizePath(args[0]) else terminalCurrentPath
                viewModelScope.launch {
                    val matchingFiles = allFiles.value.filter { it.path == targetPath }
                    if (matchingFiles.isEmpty()) {
                        terminalHistory.add("Empty or directory does not exist: $targetPath")
                    } else {
                        matchingFiles.forEach { file ->
                            if (file.isFolder) {
                                terminalHistory.add("  [DIR]  ${file.name}/")
                            } else {
                                terminalHistory.add("  [FILE] ${file.name}    (${file.sizeBytes} Bytes)")
                            }
                        }
                    }
                }
            }
            "cd" -> {
                if (args.isEmpty()) {
                    terminalCurrentPath = "/sdcard"
                    terminalHistory.add("Returned to /sdcard")
                } else {
                    val pathArg = args[0]
                    viewModelScope.launch {
                        val absolutePath = normalizePath(pathArg)
                        // Verify directory exists
                        val exists = allFiles.value.any { it.isFolder && (absolutePath == "/" || (it.path + "/" + it.name).replace("//", "/") == absolutePath || getFolderFullpath(it) == absolutePath) }
                        if (exists || absolutePath == "/" || absolutePath == "/sdcard" || absolutePath == "/system" || absolutePath == "/root") {
                            terminalCurrentPath = absolutePath
                            terminalHistory.add("Current path is: $terminalCurrentPath")
                        } else {
                            terminalHistory.add("Directory not found: $pathArg")
                        }
                    }
                }
            }
            "cat" -> {
                if (args.isEmpty()) {
                    terminalHistory.add("Error: Please specify target file")
                } else {
                    val filename = args[0]
                    viewModelScope.launch {
                        val file = allFiles.value.find { !it.isFolder && it.path == terminalCurrentPath && it.name == filename }
                        if (file != null) {
                            terminalHistory.add(file.content)
                        } else {
                            terminalHistory.add("File not found in current directory: $filename")
                        }
                    }
                }
            }
            "echo" -> {
                val echoString = args.joinToString(" ")
                if (echoString.contains(">")) {
                    val splitParts = echoString.split(">")
                    val contentText = splitParts[0].trim().removePrefix("\"").removeSuffix("\"").removePrefix("'").removeSuffix("'")
                    val filename = splitParts[1].trim()
                    
                    if (filename.isEmpty()) {
                        terminalHistory.add("Error: destination filename empty")
                    } else {
                        viewModelScope.launch {
                            val existing = allFiles.value.find { !it.isFolder && it.path == terminalCurrentPath && it.name == filename }
                            val newVirtualFile = VirtualFile(
                                id = existing?.id ?: 0,
                                name = filename,
                                content = contentText,
                                path = terminalCurrentPath,
                                isFolder = false,
                                sizeBytes = contentText.length.toLong(),
                                updatedAt = System.currentTimeMillis()
                            )
                            repository.createFile(newVirtualFile)
                            terminalHistory.add("Successfully wrote content to $filename.")
                        }
                    }
                } else {
                    terminalHistory.add(echoString)
                }
            }
            "mkdir" -> {
                if (args.isEmpty()) {
                    terminalHistory.add("Error: Specify folder name")
                } else {
                    val folderName = args[0]
                    viewModelScope.launch {
                        val newFolder = VirtualFile(
                            name = folderName,
                            path = terminalCurrentPath,
                            isFolder = true,
                            content = "",
                            sizeBytes = 0,
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.createFile(newFolder)
                        terminalHistory.add("Folder directory created: $folderName/")
                    }
                }
            }
            "rm" -> {
                if (args.isEmpty()) {
                    terminalHistory.add("Error: specify path to delete")
                } else {
                    val item = args[0]
                    viewModelScope.launch {
                        val file = allFiles.value.find { it.path == terminalCurrentPath && it.name == item }
                        if (file != null) {
                            repository.deleteFile(file.id)
                            terminalHistory.add("Successfully removed $item.")
                        } else {
                            terminalHistory.add("No such item file found in current directory: $item")
                        }
                    }
                }
            }
            "top", "ps" -> {
                terminalHistory.add("USER       PID   %CPU %MEM   VSZ   RSS TTY      STAT START   TIME COMMAND")
                terminalHistory.add("root         1    0.1  0.2 12560  4820 ?        Ss   08:40   0:01 /init")
                terminalHistory.add("root        36    0.8  0.4 45600 12840 ?        S    08:40   0:05 zygote64")
                terminalHistory.add("system     142    1.4  1.8 198030 42300 ?       Sl   08:40   0:12 system_server")
                terminalHistory.add("u0_a21     180    2.5  1.2 125400 32440 ?       Sl   08:40   0:08 com.vmos.launcher")
                terminalHistory.add("u0_a25     211    1.8  0.9 98200 18450 pts/0    R+   08:41   0:02 com.vmos.terminal -sh")
                if (openAppPackage != null && openAppPackage != "com.vmos.terminal") {
                    val cleanPkg = openAppPackage?.replace("com.vmos.", "") ?: "app"
                    terminalHistory.add("u0_a30     244    4.8  1.5 145000 38240 ?       Sl   08:42   0:04 guest.app.$cleanPkg")
                }
            }
            else -> {
                terminalHistory.add("bash/sh: command not found: $command")
            }
        }
    }

    private fun getFolderFullpath(folder: VirtualFile): String {
        return (folder.path + "/" + folder.name).replace("//", "/")
    }

    private fun normalizePath(rawArg: String): String {
        var arg = rawArg
        if (arg == "..") {
            if (terminalCurrentPath == "/" || terminalCurrentPath.isEmpty()) return "/"
            val parent = terminalCurrentPath.substringBeforeLast('/')
            return if (parent.isEmpty()) "/" else parent
        }
        if (arg.startsWith("/")) {
            return if (arg == "/") "/" else arg.removeSuffix("/")
        }
        val composed = if (terminalCurrentPath == "/") "/$arg" else "$terminalCurrentPath/$arg"
        return composed.replace("//", "/").removeSuffix("/")
    }

    // Interactive File Manager Actions
    fun createTextFileInExplorer(name: String, content: String) {
        viewModelScope.launch {
            val file = VirtualFile(
                name = name,
                content = content,
                path = explorerCurrentPath,
                isFolder = false,
                sizeBytes = content.length.toLong(),
                updatedAt = System.currentTimeMillis()
            )
            repository.createFile(file)
        }
    }

    fun deleteFileInExplorer(file: VirtualFile) {
        viewModelScope.launch {
            repository.deleteFile(file.id)
        }
    }

    fun createFolderInExplorer(name: String) {
        viewModelScope.launch {
            val folder = VirtualFile(
                name = name,
                content = "",
                path = explorerCurrentPath,
                isFolder = true,
                sizeBytes = 0,
                updatedAt = System.currentTimeMillis()
            )
            repository.createFile(folder)
        }
    }

    // App Store simulated triggers
    fun openSimulatedWebsite(url: String) {
        browserAddress = url
        if (!browserSearchHistory.contains(url)) {
            browserSearchHistory.add(0, url)
        }
    }

    fun requestRootAccessFromApp(appName: String, command: String, onDecision: (Boolean) -> Unit) {
        requestRootPrivilege(
            appName = appName,
            command = command,
            onGranted = { onDecision(true) },
            onDenied = { onDecision(false) }
        )
    }
}
