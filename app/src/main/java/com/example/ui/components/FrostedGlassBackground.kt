package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FrostedGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF131317)) // Deep slate-dark background (similar to #1C1B1F)
    ) {
        // Draw decorative blurred circular glow gradients in the background
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Blob 1: Top Right/Center Slate Grey-Blue
            drawCircle(
                color = Color(0xFF2A2D35).copy(alpha = 0.8f),
                radius = size.width * 0.7f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f)
            )

            // Blob 2: Center Left Purple-Grey
            drawCircle(
                color = Color(0xFF3E424B).copy(alpha = 0.5f),
                radius = size.width * 0.6f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.5f)
            )

            // Blob 3: Bottom Right Slate-Blue
            drawCircle(
                color = Color(0xFF4D525D).copy(alpha = 0.4f),
                radius = size.width * 0.5f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.85f)
            )
        }

        // Translucent overlay to blend the blobs smoothly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131317).copy(alpha = 0.45f))
        ) {
            content()
        }
    }
}
