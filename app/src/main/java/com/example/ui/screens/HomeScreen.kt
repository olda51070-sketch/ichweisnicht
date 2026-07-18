package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassBackground
import com.example.ui.theme.*
import com.example.ui.viewmodel.CallerPartner
import com.example.ui.viewmodel.PracticeViewModel
import com.example.ui.viewmodel.SpeechTopic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PracticeViewModel,
    onStartPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val streak by viewModel.streakDays.collectAsState()
    val totalMins by viewModel.totalPracticeMinutes.collectAsState()
    val totalCalls by viewModel.totalSessionsCount.collectAsState()

    var showExplanationDialog by remember { mutableStateOf(false) }

    FrostedGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
        // App Header with Explanation Trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FaceCall Practice",
                    color = SlateTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Speak naturally in crowded places",
                    color = SlateTextSecondary,
                    fontSize = 13.sp
                )
            }
            IconButton(
                onClick = { showExplanationDialog = true },
                modifier = Modifier.testTag("help_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "How it works",
                    tint = SlatePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Banner Row (Streak, Total Minutes, Total Sessions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Streak",
                value = "$streak days",
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = AccentRed,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Practice",
                value = "$totalMins min",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Practice Minutes",
                        tint = SlatePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Sessions",
                value = "$totalCalls calls",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Total sessions",
                        tint = AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Choose Video-Call Partner
        Text(
            text = "1. Choose Your Call Partner",
            color = SlateTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select who will appear on your screen as the caller",
            color = SlateTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.partners) { partner ->
                PartnerCard(
                    partner = partner,
                    isSelected = viewModel.selectedPartner == partner,
                    onClick = { viewModel.selectedPartner = partner }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Select Topic/Speech Prompt
        Text(
            text = "2. Select Speaking Topic",
            color = SlateTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select a preset structure to keep you focused while talking",
            color = SlateTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        viewModel.topics.forEach { topic ->
            TopicItem(
                topic = topic,
                isSelected = viewModel.selectedTopic == topic,
                onClick = { viewModel.selectedTopic = topic }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Custom Topic Field if Custom/Free Flow is Selected
        if (viewModel.selectedTopic.id == "free") {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = viewModel.customTopicInput,
                onValueChange = { viewModel.customTopicInput = it },
                label = { Text("Custom Prompt / Topic (Optional)") },
                placeholder = { Text("What do you want to talk about today?") },
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
                    .testTag("custom_topic_field")
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Large CTA Practice Button
        Button(
            onClick = onStartPractice,
            colors = ButtonDefaults.buttonColors(
                containerColor = SlatePrimary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start_practice_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneCallback,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Start FaceTime Practice",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
    }

    // Explanatory Help Dialog
    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = {
                Text(
                    text = "How FaceCall Works",
                    color = SlateTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recording yourself speaking in public can be terrifying because you feel like everyone is watching and judging you.",
                        color = SlateTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "FaceCall solves this by tricking onlookers. The screen will display a realistic full-screen incoming/active video call with a simulated partner.",
                        color = SlateTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "In the corner is your standard front-camera bubble. To any passerby, you are simply on FaceTime/Zoom chatting with a friend or colleague. But in reality, the app is recording your speech as a video file so you can practice, self-evaluate, and conquer your public anxiety safely!",
                        color = SlateTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showExplanationDialog = false },
                    modifier = Modifier.testTag("close_help_dialog")
                ) {
                    Text("Got It!", color = SlatePrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SlateSurface
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = SlateTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                color = SlateTextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PartnerCard(
    partner: CallerPartner,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SlateSurface else SlateSurface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            2.dp,
            if (isSelected) SlatePrimary else SlateBorder
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
            .testTag("partner_card_${partner.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = painterResource(id = partner.imageRes),
                    contentDescription = partner.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Role Label overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = partner.role,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = partner.name,
                    color = SlateTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = partner.description,
                    color = SlateTextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun TopicItem(
    topic: SpeechTopic,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SlateSurface else SlateSurface.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) SlatePrimary else SlateBorder
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("topic_card_${topic.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SlatePrimary,
                    unselectedColor = SlateTextSecondary
                ),
                modifier = Modifier.testTag("topic_radio_${topic.id}")
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = topic.title,
                    color = SlateTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = topic.description,
                    color = SlateTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                
                if (isSelected && topic.prompts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Practice prompts:",
                        color = SlatePrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    topic.prompts.forEachIndexed { index, prompt ->
                        Text(
                            text = "• $prompt",
                            color = SlateTextPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
