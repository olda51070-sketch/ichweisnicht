package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassBackground
import com.example.ui.theme.*

@Composable
fun TipsScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    FrostedGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
        Text(
            text = "Public Speaking Tips",
            color = SlateTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Master your anxiety and improve your public speaking delivery",
            color = SlateTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Guide Card: The FaceTime Trick
        GuideSectionCard(
            title = "The 'FaceTime Trick' Psychology",
            icon = Icons.Default.Psychology,
            color = SlatePrimary,
            content = "Psychologists study the 'Spotlight Effect'—our tendency to overestimate how much others notice our appearance and behavior. " +
                    "In public, you feel intense anxiety recording yourself because you assume everyone is judging you. " +
                    "By looking like you're on a standard FaceTime/video call, you immediately trigger a social exception. " +
                    "Onlookers think you are just chatting with a loved one. The spotlight disappears, allowing you to speak freely!"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Guide Card: How to Look Natural
        GuideSectionCard(
            title = "How to Look Natural",
            icon = Icons.Default.Hearing,
            color = AccentGreen,
            content = "1. Hold your phone at chest-height, slightly tilted up (exactly how people hold video calls).\n" +
                    "2. Smile or nod occasionally at your screen partner (Sophia, Alex, etc.) to mimic a real connection.\n" +
                    "3. Don't whisper! Whispery talking looks suspicious and tense. Speak at a relaxed, conversational volume.\n" +
                    "4. Wear headphones (or AirPods). This explains why you're speaking without holding the phone flat to your ear!"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Guide Card: Managing Speaking Anxiety
        GuideSectionCard(
            title = "Managing Sudden Anxiety",
            icon = Icons.Default.SelfImprovement,
            color = AccentRed,
            content = "If you feel a surge of public panic:\n" +
                    "• Take a slow, diaphragmatic breath (inhale for 4 seconds, hold for 4, exhale for 4).\n" +
                    "• Remind yourself of your fake caller partner. Sophia or Alex is 'listening' to you.\n" +
                    "• Slow down your pacing. Anxiety makes us speak 50% faster than normal.\n" +
                    "• Embrace pauses. Pausing on a call looks natural, like you are thinking or letting the other side speak."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Guide Card: Self-Review Checklist
        GuideSectionCard(
            title = "Self-Evaluation Checklist",
            icon = Icons.Default.FactCheck,
            color = Color(0xFFFFB300),
            content = "When playing back your recorded videos in the History tab, look for:\n" +
                    "1. Eye Contact: Did you look at the camera/screen rather than darting your eyes anxiously around the street?\n" +
                    "2. Filler Words: Did you use too many 'umms', 'likes', or 'so-s'?\n" +
                    "3. Voice Projection: Was your voice crisp, steady, and audible?\n" +
                    "4. Posture: Was your neck relaxed, or were your shoulders hunched in tension?"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
    }
}

@Composable
fun GuideSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    color = SlateTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                color = SlateTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
