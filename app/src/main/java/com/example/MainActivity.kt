package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BootIntroScreen
import com.example.ui.LockScreen
import com.example.ui.OsPhase
import com.example.ui.PhoneShell
import com.example.ui.YvanViewModel
import com.example.ui.apps.AppStoreApp
import com.example.ui.apps.BrowserApp
import com.example.ui.apps.CalculatorApp
import com.example.ui.apps.ClockApp
import com.example.ui.apps.DialerApp
import com.example.ui.apps.FileManagerApp
import com.example.ui.apps.GalleryApp
import com.example.ui.apps.GenericVirtualAppScreen
import com.example.ui.apps.MessagesApp
import com.example.ui.apps.NotesApp
import com.example.ui.apps.RootManagerApp
import com.example.ui.apps.SensorsApp
import com.example.ui.apps.SettingsApp
import com.example.ui.apps.TerminalApp
import com.example.ui.theme.YvanAccentPreset
import com.example.ui.theme.YvanAndroidTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: YvanViewModel = viewModel()
            val state by viewModel.osState.collectAsStateWithLifecycle()
            val phase by viewModel.phase.collectAsStateWithLifecycle()
            val activeAppPackage by viewModel.activeAppPackage.collectAsStateWithLifecycle()
            val recentPackages by viewModel.recentPackages.collectAsStateWithLifecycle()
            val isShadeOpen by viewModel.isShadeOpen.collectAsStateWithLifecycle()
            val isPowerMenuOpen by viewModel.isPowerMenuOpen.collectAsStateWithLifecycle()
            val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

            LaunchedEffect(toastMessage) {
                if (toastMessage != null) {
                    delay(2600)
                    viewModel.clearToast()
                }
            }

            BackHandler(
                enabled = phase == OsPhase.ACTIVE_APP ||
                    phase == OsPhase.APP_DRAWER ||
                    phase == OsPhase.RECENTS ||
                    isShadeOpen ||
                    isPowerMenuOpen
            ) {
                viewModel.navigateBack()
            }

            YvanAndroidTheme(
                darkTheme = state.config.darkTheme,
                accentPreset = YvanAccentPreset.fromKey(state.config.accentTheme)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!state.isInitialized) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        when (phase) {
                            OsPhase.BOOT_INTRO -> {
                                BootIntroScreen(
                                    config = state.config,
                                    strings = state.strings,
                                    onFinish = { viewModel.finishBootIntro() }
                                )
                            }

                            OsPhase.LOCK_SCREEN -> {
                                LockScreen(
                                    config = state.config,
                                    storage = state.storage,
                                    strings = state.strings,
                                    onUnlock = { viewModel.unlockPhone2() },
                                    onReplayIntro = { viewModel.replayBootIntro() }
                                )
                            }

                            else -> {
                                PhoneShell(
                                    state = state,
                                    phase = phase,
                                    activeAppPackage = activeAppPackage,
                                    recentPackages = recentPackages,
                                    isShadeOpen = isShadeOpen,
                                    isPowerMenuOpen = isPowerMenuOpen,
                                    viewModel = viewModel
                                ) { app ->
                                    when (app.packageId) {
                                        "com.yvan.phone" -> DialerApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.messages" -> MessagesApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.root" -> RootManagerApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.store" -> AppStoreApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.terminal" -> TerminalApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.files" -> FileManagerApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.settings" -> SettingsApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.gallery" -> GalleryApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.notes" -> NotesApp(state, viewModel) { viewModel.goHome() }
                                        "com.yvan.clock" -> ClockApp(state) { viewModel.goHome() }
                                        "com.yvan.calc" -> CalculatorApp(state) { viewModel.goHome() }
                                        "com.yvan.sensors" -> SensorsApp(state) { viewModel.goHome() }
                                        "com.yvan.browser" -> BrowserApp(state) { viewModel.goHome() }
                                        else -> GenericVirtualAppScreen(app, state, viewModel) { viewModel.goHome() }
                                    }
                                }
                            }
                        }
                    }

                    // OS Toast Notification Pill
                    AnimatedVisibility(
                        visible = toastMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                    ) {
                        toastMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.inverseSurface,
                                tonalElevation = 8.dp
                            ) {
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
