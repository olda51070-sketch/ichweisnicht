package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CameraView
import com.example.ui.theme.*
import com.example.ui.viewmodel.PracticeViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallPracticeScreen(
    viewModel: PracticeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var callState by remember { mutableStateOf("ringing") } // ringing, active, review
    var elapsedSeconds by remember { mutableStateOf(0) }
    var recordingStarted by remember { mutableStateOf(false) }
    var currentFilePath by remember { mutableStateOf<String?>(null) }
    var showPromptsList by remember { mutableStateOf(true) }

    // Floating Camera position state (draggable!)
    var cameraOffsetX by remember { mutableStateOf(0f) }
    var cameraOffsetY by remember { mutableStateOf(0f) }

    val partner = viewModel.selectedPartner
    val topic = viewModel.selectedTopic

    // Timer effect for Active Call
    LaunchedEffect(callState) {
        if (callState == "active") {
            elapsedSeconds = 0
            while (callState == "active") {
                delay(1000)
                elapsedSeconds++
                viewModel.currentSessionSeconds = elapsedSeconds
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (callState) {
            "ringing" -> {
                // Incoming Call interface
                RingingScreen(
                    partnerName = partner.name,
                    partnerRole = partner.role,
                    partnerImageRes = partner.imageRes,
                    onAccept = { callState = "active" },
                    onDecline = onBack
                )
            }
            "active" -> {
                // Main active full-screen FaceTime call interface
                Box(modifier = Modifier.fillMaxSize()) {
                    // Full screen partner image
                    Image(
                        painter = painterResource(id = partner.imageRes),
                        contentDescription = partner.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(if (viewModel.isCameraOff) 8.dp else 0.dp)
                    )

                    // Subtle overlay gradient to keep status bar and bottom toolbar readable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    // Partner Name and Status Indicator
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = partner.name,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("active_partner_name")
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(AccentGreen, CircleShape)
                            )
                            Text(
                                text = "Connected (${formatTimer(elapsedSeconds)})",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Subtle Practice Mode Indicator (Disguised as Encryption tag)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 115.dp)
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "END-TO-END ENCRYPTED",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    // Floating prompt box overlay (so user has speaking cues!)
                    if (showPromptsList && topic.prompts.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp)
                                .widthIn(max = 280.dp)
                                .testTag("prompts_overlay_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = topic.title,
                                        color = SlatePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hide Prompts",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { showPromptsList = false }
                                            .testTag("close_prompts_button")
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Practice prompts to guide your speech:",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                topic.prompts.forEachIndexed { index, prompt ->
                                    Text(
                                        text = "${index + 1}. $prompt",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    } else if (!showPromptsList && topic.prompts.isNotEmpty()) {
                        // Small overlay tab to bring prompts back
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                .background(Color.Black.copy(alpha = 0.8f))
                                .clickable { showPromptsList = true }
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .testTag("show_prompts_tab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Show Prompts",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Draggable picture-in-picture local camera view (re-creates exactly FaceTime)
                    CameraView(
                        isRecording = true,
                        isMuted = viewModel.isAudioMuted,
                        isCameraOff = viewModel.isCameraOff,
                        onRecordingStarted = { recordingStarted = true },
                        onRecordingStopped = { path, duration ->
                            currentFilePath = path
                            // Transition to review dialogue
                            viewModel.endCall(path, duration)
                        },
                        modifier = Modifier
                            .size(110.dp, 160.dp)
                            .offset { IntOffset(cameraOffsetX.roundToInt(), cameraOffsetY.roundToInt()) }
                            .align(Alignment.TopEnd)
                            .padding(top = 40.dp, end = 16.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    cameraOffsetX += dragAmount.x
                                    cameraOffsetY += dragAmount.y
                                }
                            }
                    )

                    // Active partner greeting subtitle prompt at bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 120.dp)
                            .padding(horizontal = 24.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = partner.greeting,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }

                    // Bottom controls row with Frosted Glass Panel styling
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(40.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(40.dp))
                            .padding(vertical = 14.dp, horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mic Button
                            IconButton(
                                onClick = { viewModel.isAudioMuted = !viewModel.isAudioMuted },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(if (viewModel.isAudioMuted) AccentRed else Color.White.copy(alpha = 0.2f))
                                    .testTag("mute_mic_button")
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isAudioMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Toggle Mic",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Red Hangup Button
                            IconButton(
                                onClick = {
                                    // Triggering camera stopping recording -> triggers save
                                    callState = "review"
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AccentRed)
                                    .testTag("hangup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Hang Up",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Camera Toggle Button
                            IconButton(
                                onClick = { viewModel.isCameraOff = !viewModel.isCameraOff },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(if (viewModel.isCameraOff) AccentRed else Color.White.copy(alpha = 0.2f))
                                    .testTag("toggle_camera_button")
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                    contentDescription = "Toggle Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            "review" -> {
                // Stopping active recording triggers review dialog handled by main screen or ViewModel.
                // We show a full screen dark review panel here
                ReviewPanel(
                    viewModel = viewModel,
                    durationSeconds = viewModel.currentSessionSeconds,
                    onSave = {
                        viewModel.saveReview()
                        onBack()
                    },
                    onDiscard = {
                        viewModel.discardReview()
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
fun RingingScreen(
    partnerName: String,
    partnerRole: String,
    partnerImageRes: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A13)) // Extremely deep blue-black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FaceTime",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Simulating Video Call...",
                    color = SlatePrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Central Avatar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .border(3.dp, SlatePrimary, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = partnerImageRes),
                        contentDescription = partnerName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = partnerName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("ringing_partner_name")
                )
                Text(
                    text = partnerRole,
                    color = SlateTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Accept / Decline Slider & Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentRed)
                            .testTag("decline_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decline",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                // Accept Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onAccept,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                            .testTag("accept_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accept",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewPanel(
    viewModel: PracticeViewModel,
    durationSeconds: Int,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(3) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, SlateBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("review_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Practice Completed!",
                    color = SlateTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Session Duration: ${formatTimer(durationSeconds)}",
                    color = SlateTextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Rate Confidence (1-5 Stars)
                Text(
                    text = "How confident did you feel speaking?",
                    color = SlateTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        val isStarred = i <= rating
                        Icon(
                            imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$i Star Rating",
                            tint = if (isStarred) Color(0xFFFFB300) else SlateTextSecondary,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    rating = i
                                    viewModel.reviewRating = i
                                }
                                .testTag("star_rating_$i")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Self-reflection text field
                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        viewModel.reviewNotes = it
                    },
                    label = { Text("Self-Reflection Notes") },
                    placeholder = { Text("Did you make eye contact? Did you pause naturally? Note it here...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedLabelColor = SlatePrimary,
                        unfocusedLabelColor = SlateTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("reflection_notes_field")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action CTA buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        border = BorderStroke(1.dp, AccentRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("discard_recording_button")
                    ) {
                        Text("Discard Video", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlatePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("save_recording_button")
                    ) {
                        Text("Save & Log", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun formatTimer(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
