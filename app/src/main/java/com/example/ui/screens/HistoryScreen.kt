package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PracticeSession
import com.example.ui.components.FrostedGlassBackground
import com.example.ui.components.LocalVideoPlayer
import com.example.ui.theme.*
import com.example.ui.viewmodel.PracticeViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: PracticeViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()

    var activePlaybackSession by remember { mutableStateOf<PracticeSession?>(null) }
    var sessionToDelete by remember { mutableStateOf<PracticeSession?>(null) }

    FrostedGlassBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Practice History",
                color = SlateTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Review your recordings to evaluate your public performance",
                color = SlateTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (sessions.isEmpty()) {
                // Empty History State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(SlateSurface, CircleShape)
                                .border(1.dp, SlateBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = SlateTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No practice logs yet",
                            color = SlateTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Complete your first FaceTime practice session to save your speaking recording privately on this device.",
                            color = SlateTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // List of Sessions
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("sessions_list")
                ) {
                    items(sessions) { session ->
                        SessionCard(
                            session = session,
                            onPlay = { activePlaybackSession = session },
                            onDelete = { sessionToDelete = session }
                        )
                    }
                }
            }
        }

        // Playback Modal Dialog
        if (activePlaybackSession != null) {
            val session = activePlaybackSession!!
            AlertDialog(
                onDismissRequest = { activePlaybackSession = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reviewing Session",
                                color = SlateTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${session.partnerName} • ${formatDuration(session.durationSeconds)}",
                                color = SlateTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { activePlaybackSession = null },
                            modifier = Modifier.testTag("close_playback_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Player",
                                tint = SlateTextSecondary
                            )
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Integrated Native Android Video Player
                        LocalVideoPlayer(
                            videoPath = session.videoFilePath,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("active_video_player")
                        )

                        if (session.notes.isNotBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateBackground),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Your Reflection Notes:",
                                        color = SlatePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = session.notes,
                                        color = SlateTextPrimary,
                                        fontSize = 13.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { activePlaybackSession = null },
                        modifier = Modifier.testTag("playback_dialog_ok")
                    ) {
                        Text("Done", color = SlatePrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SlateSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }

        // Delete Confirmation Dialog
        if (sessionToDelete != null) {
            val session = sessionToDelete!!
            AlertDialog(
                onDismissRequest = { sessionToDelete = null },
                title = {
                    Text(
                        text = "Delete Practice Session?",
                        color = SlateTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This will permanently delete your recording for this practice session with ${session.partnerName}. This action cannot be undone.",
                        color = SlateTextPrimary,
                        fontSize = 14.sp
                    )
                },
                dismissButton = {
                    TextButton(onClick = { sessionToDelete = null }) {
                        Text("Cancel", color = SlateTextSecondary)
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSession(session)
                            sessionToDelete = null
                        },
                        modifier = Modifier.testTag("confirm_delete_button")
                    ) {
                        Text("Delete", color = AccentRed, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SlateSurface
            )
        }
    }
    }
}

@Composable
fun SessionCard(
    session: PracticeSession,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val file = File(session.videoFilePath)
    val isRealVideo = file.exists() && file.length() > 0

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Partner & Date & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = session.partnerName,
                            color = SlateTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(SlatePrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = formatDuration(session.durationSeconds),
                                color = SlatePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatTimestamp(session.timestamp),
                        color = SlateTextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_session_button_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Practice",
                        tint = AccentRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Topic practiced
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = SlatePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Topic: ${session.topic}",
                    color = SlateTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Confidence rating (stars)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "Confidence: ",
                    color = SlateTextSecondary,
                    fontSize = 12.sp
                )
                for (i in 1..5) {
                    val isStarred = i <= session.confidenceRating
                    Icon(
                        imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (isStarred) Color(0xFFFFB300) else SlateTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Notes preview (if any)
            if (session.notes.isNotBlank()) {
                Text(
                    text = "\"${session.notes}\"",
                    color = SlateTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Playback Actions Button
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlatePrimary.copy(alpha = 0.15f),
                    contentColor = SlatePrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("play_playback_button_${session.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isRealVideo) Icons.Default.PlayCircleFilled else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isRealVideo) "Watch Recording Playback" else "View Practice Summary",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
