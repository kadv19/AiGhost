package com.example.aighostextractor

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aighostextractor.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GhostExtractorScreen(
    viewModel: ExtractionViewModel = viewModel()
) {
    val progress by viewModel.progress.collectAsState()
    var username by rememberSaveable { mutableStateOf("") }
    var usernameConfirmed by rememberSaveable { mutableStateOf(false) }

    AIGhostExtractorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!usernameConfirmed) {
                UsernameScreen(
                    username = username,
                    onUsernameChange = { username = it },
                    onConfirm = { if (username.isNotBlank()) usernameConfirmed = true }
                )
            } else {
                MainExtractionScreen(
                    progress = progress,
                    username = username,
                    viewModel = viewModel,
                    onReset = {
                        viewModel.reset()
                        usernameConfirmed = false
                    }
                )
            }
        }
    }
}

@Composable
fun UsernameScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = ElectricPurple,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "WHO ARE YOU?",
            style = MaterialTheme.typography.displayLarge,
            color = SoftWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your identity defines your Ghost ID",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = SoftWhite.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = { Text("e.g. ghost_rider", color = ElectricPurpleDim) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = SoftWhite),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricPurple,
                unfocusedBorderColor = ElectricPurpleDim,
                focusedContainerColor = Color(0xFF1A1A1E),
                unfocusedContainerColor = Color(0xFF15151A),
                cursorColor = ElectricPurple
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onConfirm,
            enabled = username.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricPurple,
                disabledContainerColor = ElectricPurpleDim.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "ESTABLISH IDENTITY",
                color = if (username.isNotBlank()) SoftWhite else SoftWhite.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun MainExtractionScreen(
    progress: ExtractionProgress,
    username: String,
    viewModel: ExtractionViewModel,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Identity Badge
        Box(
            modifier = Modifier
                .background(ElectricPurple.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, ElectricPurple.copy(alpha = 0.3f), CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "ID: $username",
                color = ElectricPurple,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        StatusSection(state = progress.state)
        
        Spacer(modifier = Modifier.weight(0.3f))
        
        CentralButton(
            state = progress.state,
            smsActive = progress.smsActive,
            pdfActive = progress.pdfActive,
            imageActive = progress.imageActive,
            onStartExtraction = { viewModel.startExtraction(username) },
            onReset = onReset
        )
        
        Spacer(modifier = Modifier.weight(0.3f))

        if (progress.state == ExtractionState.Complete && progress.secretPhrase.isNotEmpty()) {
            SecretPhraseCard(phrase = progress.secretPhrase)
            Spacer(modifier = Modifier.height(16.dp))
        }

        LogSection(
            logs = progress.logs,
            modifier = Modifier.fillMaxWidth().weight(0.4f)
        )
    }
}

@Composable
fun SecretPhraseCard(phrase: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF15151A),
                shape = MaterialTheme.shapes.medium
            )
            .border(1.dp, ElectricPurple, MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔑 YOUR GHOST KEY",
            style = MaterialTheme.typography.bodyMedium,
            color = ElectricPurple,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = phrase,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = SoftWhite
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter this on your new phone to unlock your Ghost",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = ElectricPurpleDim
        )
    }
}

@Composable
fun StatusSection(state: ExtractionState) {
    val statusText = when (state) {
        ExtractionState.Idle -> "Ready to Extract"
        ExtractionState.Initializing -> "Initializing..."
        ExtractionState.ReadingMemories -> "Reading your memories..."
        ExtractionState.ExtractingSMS -> "Extracting SMS..."
        ExtractionState.ExtractingPDFs -> "Extracting PDFs..."
        ExtractionState.ExtractingImages -> "Extracting Images..."
        ExtractionState.Encrypting -> "Encrypting locally..."
        ExtractionState.Uploading -> "Uploading securely..."
        ExtractionState.Complete -> "Memory Transfer Complete"
    }

    val subText = when (state) {
        ExtractionState.Idle -> "Initiate phantom sync sequence"
        ExtractionState.Complete -> "Ghost confirmed"
        else -> "Processing data stream"
    }

    AnimatedContent(
        targetState = statusText,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) togetherWith
                    fadeOut(animationSpec = tween(600))
        },
        label = "status_animation"
    ) { text ->
        Text(
            text = text,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = SoftWhite
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    AnimatedContent(
        targetState = subText,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) togetherWith
                    fadeOut(animationSpec = tween(600))
        },
        label = "substatus_animation"
    ) { text ->
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = SoftWhite.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CentralButton(
    state: ExtractionState,
    smsActive: Boolean,
    pdfActive: Boolean,
    imageActive: Boolean,
    onStartExtraction: () -> Unit,
    onReset: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp)
    ) {
        val isExtracting = state != ExtractionState.Idle && state != ExtractionState.Complete

        if (isExtracting) {
            PulsingGlow()
        }

        OrbitIcons(
            smsActive = smsActive,
            pdfActive = pdfActive,
            imageActive = imageActive,
            isExtracting = isExtracting
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(180.dp)
                .border(
                    width = 2.dp,
                    color = if (state == ExtractionState.Complete) ElectricPurple else ElectricPurpleDim,
                    shape = CircleShape
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
        ) {
            AnimatedVisibility(
                visible = state == ExtractionState.Complete,
                enter = fadeIn(animationSpec = tween(1500)),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complete",
                    tint = ElectricPurple,
                    modifier = Modifier.size(100.dp)
                )
            }

            AnimatedVisibility(
                visible = state != ExtractionState.Complete,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(1500))
            ) {
                Button(
                    onClick = onStartExtraction,
                    enabled = state == ExtractionState.Idle,
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = if (state == ExtractionState.Idle) "SYNC" else "...",
                        color = SoftWhite,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        if (state == ExtractionState.Complete) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 30.dp)
            ) {
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricPurple
                    )
                ) {
                    Text("Reset", color = SoftWhite)
                }
            }
        }
    }
}

@Composable
fun PulsingGlow() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .scale(scale)
            .alpha(alpha)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricPurple.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
            .blur(20.dp)
    )
}

@Composable
fun OrbitIcons(
    smsActive: Boolean,
    pdfActive: Boolean,
    imageActive: Boolean,
    isExtracting: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbit")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation"
    )

    val orbitRadius = 120f
    val angleOffset = 120f

    OrbitIcon(
        icon = Icons.Default.Email,
        active = smsActive,
        angle = if (isExtracting) rotation else 90f,
        radius = orbitRadius,
        description = "SMS"
    )

    OrbitIcon(
        icon = Icons.Outlined.Description,
        active = pdfActive,
        angle = if (isExtracting) rotation + angleOffset else 210f,
        radius = orbitRadius,
        description = "PDF"
    )

    OrbitIcon(
        icon = Icons.Outlined.Image,
        active = imageActive,
        angle = if (isExtracting) rotation + 2 * angleOffset else 330f,
        radius = orbitRadius,
        description = "Image"
    )
}

@Composable
fun OrbitIcon(
    icon: ImageVector,
    active: Boolean,
    angle: Float,
    radius: Float,
    description: String
) {
    val radian = Math.toRadians(angle.toDouble())
    val x = (radius * cos(radian)).toFloat()
    val y = (radius * sin(radian)).toFloat()

    val iconAlpha by animateFloatAsState(
        targetValue = if (active) 1f else 0.3f,
        animationSpec = tween(500),
        label = "icon_alpha"
    )

    Box(
        modifier = Modifier
            .offset(x = x.dp, y = y.dp)
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = if (active) ElectricPurple else ElectricPurpleDim,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (active) ElectricPurple else SoftWhite.copy(alpha = 0.5f),
            modifier = Modifier
                .size(24.dp)
                .alpha(iconAlpha)
        )
    }
}

@Composable
fun LogSection(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = TerminalBackground,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            logs.forEach { log ->
                Text(
                    text = log,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricPurple
                )
            }
        }
    }
}
