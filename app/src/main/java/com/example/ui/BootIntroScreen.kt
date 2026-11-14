package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.YvanOsConfig
import com.example.data.YvanStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay

private data class IntroFeatureSlide(
    val icon: ImageVector,
    val titleRw: String,
    val titleEn: String,
    val descRw: String,
    val descEn: String,
    val badge: String
)

private val introSlides = listOf(
    IntroFeatureSlide(
        icon = Icons.Rounded.PhoneAndroid,
        titleRw = "Telefoni ya 2 Yuzuye",
        titleEn = "Complete Second Phone",
        descRw = "Ufite Telefoni, Ubutumwa, Amafoto, Inyandiko, Isaha, Kubara na Browser muri Phone 2.",
        descEn = "Includes Dialer, Messages, Gallery, Notes, Clock, Calculator & Browser inside Phone 2.",
        badge = "PHONE 2 OS"
    ),
    IntroFeatureSlide(
        icon = Icons.Rounded.Security,
        titleRw = "Sisitemu ya Root (# su)",
        titleEn = "Rooted Superuser (# su)",
        descRw = "Cunga Root permissions kuri buri app, koresha Terminal # kandi uhindure imikorere ya CPU.",
        descEn = "Manage per-app su permissions, run root Terminal # commands, and tune CPU/ZRAM.",
        badge = "UID 0 • ROOT"
    ),
    IntroFeatureSlide(
        icon = Icons.Rounded.SdStorage,
        titleRw = "Ububiko bwa 16 GB Bwigenga",
        titleEn = "Dedicated 16 GB Storage",
        descRw = "Shyiramo app nshya muri Yvan Store, bika amadosiye muri /sdcard na /system, usibe cache.",
        descEn = "Install apps from Yvan Store, manage /sdcard & /system files, and wipe cache anytime.",
        badge = "16.0 GB NAND"
    ),
    IntroFeatureSlide(
        icon = Icons.Rounded.Tune,
        titleRw = "Genzura & Hindura Byose",
        titleEn = "Total System Control",
        descRw = "Hindura izina rya telefoni, Theme, Wallpaper, PIN, Ururimi na build.prop uko ubishaka.",
        descEn = "Customize device name, accent colors, wallpapers, PIN lock, language & build.prop.",
        badge = "FULL CONTROL"
    )
)

@Composable
fun BootIntroScreen(
    config: YvanOsConfig,
    strings: YvanStrings,
    onFinish: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    // Stage 0 = kernel boot log, Stage 1 = hero logo animation, Stage 2 = 4-part feature video showcase
    var stage by remember { mutableIntStateOf(0) }
    var visibleBootLines by remember { mutableIntStateOf(1) }
    var slideIndex by remember { mutableIntStateOf(0) }
    var autoAdvance by remember { mutableStateOf(true) }

    val bootLogMessages = remember(config.deviceName) {
        listOf(
            "[0.004] Bootloader: YvanBoot v2.4 (unlocked)",
            "[0.128] Kernel: Linux 6.1.75-yvan-root #1 PREEMPT",
            "[0.340] Mounting /system (rw), /data (16.0 GB)...",
            "[0.612] Starting YvanSU daemon -> UID=0 ready",
            "[0.905] Launching ${config.deviceName} Shell..."
        )
    }

    LaunchedEffect(Unit) {
        for (i in 1..bootLogMessages.size) {
            visibleBootLines = i
            delay(260)
        }
        delay(250)
        stage = 1
        delay(2000)
        stage = 2
        while (autoAdvance && slideIndex < introSlides.lastIndex) {
            delay(1850)
            if (autoAdvance) {
                slideIndex += 1
            }
        }
        if (autoAdvance) {
            delay(2100)
            onFinish()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "boot_rings")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "ring_rot"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0.72f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(650),
        label = "logo_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF04070E), Color(0xFF0A1224), Color(0xFF050811))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Animated Cyber Orbit Canvas Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.38f)
            val baseRadius = size.minDimension * 0.28f * pulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.22f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 2.1f
                ),
                center = center,
                radius = baseRadius * 2.1f
            )

            drawArc(
                color = primary.copy(alpha = 0.45f),
                startAngle = ringRotation,
                sweepAngle = 210f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = secondary.copy(alpha = 0.40f),
                startAngle = -ringRotation * 1.3f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius * 1.24f, center.y - baseRadius * 1.24f),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2.48f, baseRadius * 2.48f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Top Bar: LIVE VIDEO tag + Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = primary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "INTRO BOOT • 16 GB ROOT",
                        style = MaterialTheme.typography.labelSmall,
                        color = primary
                    )
                }
            }

            TextButton(
                onClick = onFinish,
                modifier = Modifier.testTag("skip_intro_button")
            ) {
                Text(
                    text = strings.skipIntro,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Main Center Sequence
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (stage == 0) {
                // Stage 0: Fast Kernel Boot Terminal
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A0F1A).copy(alpha = 0.9f))
                        .border(1.dp, primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "YVAN_BOOTLOADER // SECURE_ROOT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    bootLogMessages.take(visibleBootLines).forEach { line ->
                        Text(
                            text = line,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = primary,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            } else {
                // Stage 1 & 2: Glowing "Yvan android" Kinetic Reveal
                Box(
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(logoAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(listOf(primary, secondary))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF041016)
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = config.deviceName,
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = strings.bootSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(34.dp))

                // Stage 2: Animated Showcase Video Cards
                if (stage >= 2) {
                    val slide = introSlides[slideIndex]
                    AnimatedContent(
                        targetState = slideIndex,
                        transitionSpec = {
                            (fadeIn(tween(350)) + slideInVertically { it / 4 }) togetherWith
                                fadeOut(tween(250))
                        },
                        label = "intro_slide"
                    ) { idx ->
                        val s = introSlides[idx]
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF10192D).copy(alpha = 0.88f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                primary.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = primary.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = s.badge,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                Icon(
                                    imageVector = s.icon,
                                    contentDescription = s.titleEn,
                                    tint = secondary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = if (strings.isRw) s.titleRw else s.titleEn,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (strings.isRw) s.descRw else s.descEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFB5C3DB),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Slide Indicator Dots (interactive!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        introSlides.indices.forEach { idx ->
                            val selected = idx == slideIndex
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (selected) 26.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) primary else Color.White.copy(alpha = 0.25f))
                                    .clickable {
                                        autoAdvance = false
                                        slideIndex = idx
                                    }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("enter_phone2_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = Color(0xFF041016)
                        )
                    ) {
                        Text(
                            text = strings.enterPhone2,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
