package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.yvanDataStore by preferencesDataStore(name = "yvan_os_prefs")

data class YvanOsConfig(
    val deviceName: String = "Yvan android",
    val deviceModel: String = "Yvan Root Pro 16G",
    val osVersionLabel: String = "YvanOS 15 • Kernel 6.1-su",
    val bootGreeting: String = "Phone 2 Ready • Root Unlocked",
    val isRootEnabled: Boolean = true,
    val cpuGovernor: String = "performance",
    val cpuClockMhz: Int = 2400,
    val zramMb: Int = 2048,
    val animationScale: Float = 1.0f,
    val darkTheme: Boolean = true,
    val accentTheme: String = "EMERALD",
    val wallpaperKey: String = "emerald_cyber",
    val languageCode: String = "rw",
    val lockPin: String = "",
    val virtualBrightness: Float = 0.96f,
    val wifiEnabled: Boolean = true,
    val mobileDataEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = false,
    val cacheMb: Float = 148f,
    val showBootIntroOnStart: Boolean = true
)

class YvanOsPreferences(private val context: Context) {

    private object Keys {
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val DEVICE_MODEL = stringPreferencesKey("device_model")
        val OS_VERSION = stringPreferencesKey("os_version")
        val BOOT_GREETING = stringPreferencesKey("boot_greeting")
        val ROOT_ENABLED = booleanPreferencesKey("root_enabled")
        val CPU_GOVERNOR = stringPreferencesKey("cpu_governor")
        val CPU_CLOCK = intPreferencesKey("cpu_clock")
        val ZRAM_MB = intPreferencesKey("zram_mb")
        val ANIMATION_SCALE = floatPreferencesKey("animation_scale")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val ACCENT_THEME = stringPreferencesKey("accent_theme")
        val WALLPAPER_KEY = stringPreferencesKey("wallpaper_key")
        val LANGUAGE = stringPreferencesKey("language")
        val LOCK_PIN = stringPreferencesKey("lock_pin")
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val WIFI = booleanPreferencesKey("wifi")
        val MOBILE_DATA = booleanPreferencesKey("mobile_data")
        val BLUETOOTH = booleanPreferencesKey("bluetooth")
        val CACHE_MB = floatPreferencesKey("cache_mb")
        val SHOW_BOOT_INTRO = booleanPreferencesKey("show_boot_intro")
    }

    val configFlow: Flow<YvanOsConfig> = context.yvanDataStore.data.map { prefs ->
        YvanOsConfig(
            deviceName = prefs[Keys.DEVICE_NAME] ?: "Yvan android",
            deviceModel = prefs[Keys.DEVICE_MODEL] ?: "Yvan Root Pro 16G",
            osVersionLabel = prefs[Keys.OS_VERSION] ?: "YvanOS 15 • Kernel 6.1-su",
            bootGreeting = prefs[Keys.BOOT_GREETING] ?: "Phone 2 Ready • Root Unlocked",
            isRootEnabled = prefs[Keys.ROOT_ENABLED] ?: true,
            cpuGovernor = prefs[Keys.CPU_GOVERNOR] ?: "performance",
            cpuClockMhz = prefs[Keys.CPU_CLOCK] ?: 2400,
            zramMb = prefs[Keys.ZRAM_MB] ?: 2048,
            animationScale = prefs[Keys.ANIMATION_SCALE] ?: 1.0f,
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            accentTheme = prefs[Keys.ACCENT_THEME] ?: "EMERALD",
            wallpaperKey = prefs[Keys.WALLPAPER_KEY] ?: "emerald_cyber",
            languageCode = prefs[Keys.LANGUAGE] ?: "rw",
            lockPin = prefs[Keys.LOCK_PIN] ?: "",
            virtualBrightness = prefs[Keys.BRIGHTNESS] ?: 0.96f,
            wifiEnabled = prefs[Keys.WIFI] ?: true,
            mobileDataEnabled = prefs[Keys.MOBILE_DATA] ?: true,
            bluetoothEnabled = prefs[Keys.BLUETOOTH] ?: false,
            cacheMb = prefs[Keys.CACHE_MB] ?: 148f,
            showBootIntroOnStart = prefs[Keys.SHOW_BOOT_INTRO] ?: true
        )
    }

    suspend fun updateConfig(transform: (YvanOsConfig) -> YvanOsConfig) {
        context.yvanDataStore.edit { prefs ->
            val current = YvanOsConfig(
                deviceName = prefs[Keys.DEVICE_NAME] ?: "Yvan android",
                deviceModel = prefs[Keys.DEVICE_MODEL] ?: "Yvan Root Pro 16G",
                osVersionLabel = prefs[Keys.OS_VERSION] ?: "YvanOS 15 • Kernel 6.1-su",
                bootGreeting = prefs[Keys.BOOT_GREETING] ?: "Phone 2 Ready • Root Unlocked",
                isRootEnabled = prefs[Keys.ROOT_ENABLED] ?: true,
                cpuGovernor = prefs[Keys.CPU_GOVERNOR] ?: "performance",
                cpuClockMhz = prefs[Keys.CPU_CLOCK] ?: 2400,
                zramMb = prefs[Keys.ZRAM_MB] ?: 2048,
                animationScale = prefs[Keys.ANIMATION_SCALE] ?: 1.0f,
                darkTheme = prefs[Keys.DARK_THEME] ?: true,
                accentTheme = prefs[Keys.ACCENT_THEME] ?: "EMERALD",
                wallpaperKey = prefs[Keys.WALLPAPER_KEY] ?: "emerald_cyber",
                languageCode = prefs[Keys.LANGUAGE] ?: "rw",
                lockPin = prefs[Keys.LOCK_PIN] ?: "",
                virtualBrightness = prefs[Keys.BRIGHTNESS] ?: 0.96f,
                wifiEnabled = prefs[Keys.WIFI] ?: true,
                mobileDataEnabled = prefs[Keys.MOBILE_DATA] ?: true,
                bluetoothEnabled = prefs[Keys.BLUETOOTH] ?: false,
                cacheMb = prefs[Keys.CACHE_MB] ?: 148f,
                showBootIntroOnStart = prefs[Keys.SHOW_BOOT_INTRO] ?: true
            )
            val next = transform(current)
            prefs[Keys.DEVICE_NAME] = next.deviceName
            prefs[Keys.DEVICE_MODEL] = next.deviceModel
            prefs[Keys.OS_VERSION] = next.osVersionLabel
            prefs[Keys.BOOT_GREETING] = next.bootGreeting
            prefs[Keys.ROOT_ENABLED] = next.isRootEnabled
            prefs[Keys.CPU_GOVERNOR] = next.cpuGovernor
            prefs[Keys.CPU_CLOCK] = next.cpuClockMhz
            prefs[Keys.ZRAM_MB] = next.zramMb
            prefs[Keys.ANIMATION_SCALE] = next.animationScale
            prefs[Keys.DARK_THEME] = next.darkTheme
            prefs[Keys.ACCENT_THEME] = next.accentTheme
            prefs[Keys.WALLPAPER_KEY] = next.wallpaperKey
            prefs[Keys.LANGUAGE] = next.languageCode
            prefs[Keys.LOCK_PIN] = next.lockPin
            prefs[Keys.BRIGHTNESS] = next.virtualBrightness
            prefs[Keys.WIFI] = next.wifiEnabled
            prefs[Keys.MOBILE_DATA] = next.mobileDataEnabled
            prefs[Keys.BLUETOOTH] = next.bluetoothEnabled
            prefs[Keys.CACHE_MB] = next.cacheMb
            prefs[Keys.SHOW_BOOT_INTRO] = next.showBootIntroOnStart
        }
    }

    suspend fun resetAll() {
        context.yvanDataStore.edit { it.clear() }
    }
}
