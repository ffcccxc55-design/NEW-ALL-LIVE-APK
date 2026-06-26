package com.example.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val count: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

data class AdOverlayState(
    val showOverlay: Boolean = false,
    val countdown: Int = 5,
    val canSkip: Boolean = false,
    val targetUrl: String = ""
)

class AppViewModel(
    private val repository: AppRepository,
    context: Context
) : ViewModel() {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("all_live_prefs", Context.MODE_PRIVATE)

    // Database flows converted to StateFlows for Compose
    val channels = repository.channels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val premiumItems = repository.premiumItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val feedback = repository.feedback.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val visitorLogs = repository.visitorLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeNotification = repository.activeNotification.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val appConfig = repository.config.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppConfig()
    )

    // Screen navigation overlay state
    private val _adState = MutableStateFlow(AdOverlayState())
    val adState: StateFlow<AdOverlayState> = _adState.asStateFlow()

    // Sync operation state
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // Visitor profile state
    private val _visitorProfile = MutableStateFlow<VisitorLog?>(null)
    val visitorProfile: StateFlow<VisitorLog?> = _visitorProfile.asStateFlow()

    // Admin authentication state
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    // Ad free status
    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    init {
        initVisitorProfile()
        checkAdFreeStatus()
        // Run initial Sheet Sync on startup if ID exists
        viewModelScope.launch {
            val cfg = repository.getAppConfig()
            if (cfg.googleSheetsId.isNotBlank()) {
                syncFromGoogleSheets(cfg.googleSheetsId)
            }
        }
    }

    private fun checkAdFreeStatus() {
        val savedAdFree = sharedPrefs.getBoolean("is_ad_free", false)
        _isAdFree.value = savedAdFree
    }

    fun submitAdFreeKey(key: String): Boolean {
        return if (key.trim() == "ADFREE_HASAN") {
            sharedPrefs.edit().putBoolean("is_ad_free", true).apply()
            _isAdFree.value = true
            true
        } else {
            false
        }
    }

    fun removeAdFree() {
        sharedPrefs.edit().putBoolean("is_ad_free", false).apply()
        _isAdFree.value = false
    }

    private fun initVisitorProfile() {
        viewModelScope.launch {
            var userId = sharedPrefs.getString("user_id", null)
            var username = sharedPrefs.getString("username", null)

            if (userId == null) {
                // Generate usr_a1b2c3d4 format
                val fullUuid = UUID.randomUUID().toString()
                val hexPart = fullUuid.substring(0, 8)
                userId = "usr_$hexPart"
                sharedPrefs.edit().putString("user_id", userId).apply()
            }

            if (username == null) {
                // Generate ব্যবহারকারী_১২৩৪ format
                val randomNum = (1000..9999).random()
                username = "ব্যবহারকারী_$randomNum"
                sharedPrefs.edit().putString("username", username).apply()
            }

            // Sync with DB and track visit count
            val profile = repository.syncVisitor(userId!!, username!!)
            _visitorProfile.value = profile
        }
    }

    fun updateVisitorUsername(newUsername: String) {
        viewModelScope.launch {
            val current = _visitorProfile.value ?: return@launch
            if (newUsername.isNotBlank()) {
                sharedPrefs.edit().putString("username", newUsername).apply()
                val updatedProfile = repository.syncVisitor(current.userId, newUsername)
                _visitorProfile.value = updatedProfile
            }
        }
    }

    // Google Sheets sync
    fun syncFromGoogleSheets(sheetId: String) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val result = repository.syncGoogleSheet(sheetId)
            result.onSuccess { count ->
                _syncState.value = SyncState.Success(count)
                // Persist sheet ID in configuration
                val currentCfg = appConfig.value ?: AppConfig()
                repository.saveConfig(currentCfg.copy(googleSheetsId = sheetId))
            }.onFailure { exception ->
                _syncState.value = SyncState.Error(exception.message ?: "অজানা ত্রুটি ঘটেছে")
            }
        }
    }

    fun clearSyncStatus() {
        _syncState.value = SyncState.Idle
    }

    // Admin PIN Check
    fun verifyAdminPin(pin: String): Boolean {
        val cfg = appConfig.value ?: AppConfig()
        val isVerified = pin == cfg.securityPinPrimary || pin == cfg.securityPinSecondary
        if (isVerified) {
            _isAdminAuthenticated.value = true
        }
        return isVerified
    }

    // Admin Verification via Security Reset
    fun verifySecurityAnswer(answer: String): Boolean {
        val cfg = appConfig.value ?: AppConfig()
        val isVerified = answer.trim().equals(cfg.securityResetAnswer.trim(), ignoreCase = true)
        if (isVerified) {
            _isAdminAuthenticated.value = true
        }
        return isVerified
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    // Feedback Portal submission
    fun submitUserFeedback(message: String) {
        viewModelScope.launch {
            val user = _visitorProfile.value?.username ?: "ব্যবহারকারী"
            repository.addFeedback(username = user, message = message)
        }
    }

    // Channel Click & Ad Engine Action
    fun onChannelClicked(channel: Channel) {
        if (_isAdFree.value) {
            // Ad Free - directly open target
            _adState.value = AdOverlayState(showOverlay = false, targetUrl = channel.link)
        } else {
            // Trigger interactive 5 seconds video overlay
            viewModelScope.launch {
                _adState.value = AdOverlayState(
                    showOverlay = true,
                    countdown = 5,
                    canSkip = false,
                    targetUrl = channel.link
                )
                
                for (timer in 4 downTo 0) {
                    delay(1000)
                    _adState.value = _adState.value.copy(
                        countdown = timer,
                        canSkip = timer == 0
                    )
                }
            }
        }
    }

    fun dismissAdOverlay() {
        _adState.value = AdOverlayState(showOverlay = false, targetUrl = "")
    }

    // Admin configurations
    fun updateAppConfig(updated: AppConfig) {
        viewModelScope.launch {
            repository.saveConfig(updated)
        }
    }

    fun addChannel(channel: Channel) {
        viewModelScope.launch {
            repository.addChannel(channel)
        }
    }

    fun updateChannel(channel: Channel) {
        viewModelScope.launch {
            repository.updateChannel(channel)
        }
    }

    fun deleteChannel(channel: Channel) {
        viewModelScope.launch {
            repository.deleteChannel(channel)
        }
    }

    fun addPremiumItem(item: PremiumItem) {
        viewModelScope.launch {
            repository.addPremiumItem(item)
        }
    }

    fun updatePremiumItem(item: PremiumItem) {
        viewModelScope.launch {
            repository.updatePremiumItem(item)
        }
    }

    fun deletePremiumItem(item: PremiumItem) {
        viewModelScope.launch {
            repository.deletePremiumItem(item)
        }
    }

    fun addNotification(text: String) {
        viewModelScope.launch {
            // Turn off others
            repository.getNotificationsFlow().firstOrNull()?.forEach {
                if (it.isActive) {
                    repository.updateNotification(it.copy(isActive = false))
                }
            }
            repository.insertNotification(NotificationBanner(content = text, isActive = true))
        }
    }

    fun deleteNotification(notification: NotificationBanner) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }
}

class AppViewModelFactory(
    private val repository: AppRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
