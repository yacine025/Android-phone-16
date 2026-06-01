package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SuperuserLog
import com.example.data.model.VirtualApp
import com.example.data.model.VirtualFile
import com.example.data.model.VmConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface VmDao {

    // VM Configuration
    @Query("SELECT * FROM vm_config WHERE id = 1 LIMIT 1")
    fun getVmConfigFlow(): Flow<VmConfig?>

    @Query("SELECT * FROM vm_config WHERE id = 1 LIMIT 1")
    suspend fun getVmConfig(): VmConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVmConfig(config: VmConfig)

    // Virtual Files
    @Query("SELECT * FROM virtual_files ORDER BY isFolder DESC, name ASC")
    fun getAllFilesFlow(): Flow<List<VirtualFile>>

    @Query("SELECT * FROM virtual_files WHERE path = :path")
    suspend fun getFilesByPath(path: String): List<VirtualFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VirtualFile): Long

    @Query("DELETE FROM virtual_files WHERE id = :id")
    suspend fun deleteFileById(id: Int)

    @Query("DELETE FROM virtual_files WHERE path = :path AND name = :name")
    suspend fun deleteFileByPathAndName(path: String, name: String)

    // Superuser Access Logs
    @Query("SELECT * FROM superuser_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<SuperuserLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SuperuserLog): Long

    @Query("DELETE FROM superuser_logs")
    suspend fun clearLogs()

    // Virtual Apps / Packages
    @Query("SELECT * FROM virtual_apps")
    fun getAllAppsFlow(): Flow<List<VirtualApp>>

    @Query("SELECT * FROM virtual_apps")
    suspend fun getAllApps(): List<VirtualApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: VirtualApp)

    @Update
    suspend fun updateApp(app: VirtualApp)

    @Query("UPDATE virtual_apps SET isInstalled = :installed WHERE packageName = :packageName")
    suspend fun updateAppInstallationStatus(packageName: String, installed: Boolean)
}
