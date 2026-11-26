package com.kingzcheung.xime.update

import android.content.Context
import com.kingzcheung.xime.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Checking : AppUpdateState
    data class UpToDate(val latest: String?) : AppUpdateState
    data class Available(val release: AppRelease) : AppUpdateState
    data class Downloading(val release: AppRelease, val downloaded: Long, val total: Long) : AppUpdateState
    data class ReadyToInstall(val release: AppRelease, val file: File) : AppUpdateState
    data class Failed(val message: String, val release: AppRelease?) : AppUpdateState
}

object AppUpdater {
    val supported: Boolean get() = false

    @Volatile
    var storeManaged: Boolean = false
        private set

    @Suppress("UNUSED_PARAMETER")
    fun init(context: Context) {
    }

    val privateBuild: Boolean get() = BuildConfig.DEV_TOOLS

    private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    val state: StateFlow<AppUpdateState> = _state

    @Suppress("UNUSED_PARAMETER")
    fun isAutoCheckEnabled(context: Context): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    fun setAutoCheckEnabled(context: Context, enabled: Boolean) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun checkIfDue(context: Context) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun check(context: Context, manual: Boolean = true) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun isMeteredNetwork(context: Context): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    fun download(context: Context, release: AppRelease) {
    }

    fun cancelDownload() {
    }

    @Suppress("UNUSED_PARAMETER")
    fun install(context: Context, file: File): Boolean = false
}
