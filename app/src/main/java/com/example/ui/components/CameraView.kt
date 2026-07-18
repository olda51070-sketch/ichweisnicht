package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import kotlinx.coroutines.delay
import java.io.File

@SuppressLint("RestrictedApi")
@Composable
fun CameraView(
    isRecording: Boolean,
    isMuted: Boolean,
    isCameraOff: Boolean,
    onRecordingStarted: () -> Unit,
    onRecordingStopped: (videoFilePath: String, actualDurationSec: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var videoCaptureState by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var isSimulatedMode by remember { mutableStateOf(false) }
    var recordingStartTime by remember { mutableStateOf(0L) }

    // Check permissions
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Set up camera provider binding
    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            isSimulatedMode = true
            return@LaunchedEffect
        }
        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.SD)) // SD is lightweight and reliable
                .build()
            val videoCapture = VideoCapture.withOutput(recorder)
            
            videoCaptureState = videoCapture

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
                isSimulatedMode = false
            } catch (e: Exception) {
                Log.e("CameraView", "Failed to bind front camera, trying back camera", e)
                try {
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        videoCapture
                    )
                    isSimulatedMode = false
                } catch (e2: Exception) {
                    Log.e("CameraView", "Failed to bind back camera. Entering simulation mode.", e2)
                    isSimulatedMode = true
                }
            }
        } catch (e: Exception) {
            Log.e("CameraView", "CameraProvider failed. Entering simulation mode.", e)
            isSimulatedMode = true
        }
    }

    // Handle starting and stopping recordings
    LaunchedEffect(isRecording, isCameraOff, isMuted, videoCaptureState, isSimulatedMode) {
        if (isRecording) {
            recordingStartTime = System.currentTimeMillis()
            if (isSimulatedMode || videoCaptureState == null) {
                // Simulation recording start
                onRecordingStarted()
            } else {
                // Real CameraX recording start
                try {
                    val file = File(context.filesDir, "practice_${System.currentTimeMillis()}.mp4")
                    val fileOutputOptions = FileOutputOptions.Builder(file).build()
                    
                    val pendingRecording = videoCaptureState!!.output
                        .prepareRecording(context, fileOutputOptions)

                    if (!isMuted && ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        pendingRecording.withAudioEnabled()
                    }

                    activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
                        when (event) {
                            is VideoRecordEvent.Start -> {
                                onRecordingStarted()
                            }
                            is VideoRecordEvent.Finalize -> {
                                val duration = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
                                onRecordingStopped(file.absolutePath, maxOf(1, duration))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CameraView", "Error starting real recording, falling back to simulated session", e)
                    onRecordingStarted()
                }
            }
        } else {
            // Stop recording
            val start = recordingStartTime
            recordingStartTime = 0L
            if (start > 0) {
                val duration = ((System.currentTimeMillis() - start) / 1000).toInt()
                if (isSimulatedMode || activeRecording == null) {
                    // Create simulated empty video file to support standard media players
                    val file = File(context.filesDir, "practice_simulated_${System.currentTimeMillis()}.mp4")
                    try {
                        file.createNewFile()
                    } catch (e: Exception) { e.printStackTrace() }
                    onRecordingStopped(file.absolutePath, maxOf(1, duration))
                } else {
                    // Stop real recording
                    activeRecording?.stop()
                    activeRecording = null
                }
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121212))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .testTag("camera_preview_container")
    ) {
        if (isCameraOff) {
            // Camera is explicitly turned off
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = "Camera Off",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera Muted",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (isSimulatedMode) {
            // Simulated Camera Preview UI (Webcam-style moving graphics)
            SimulatedCameraFeed(
                isRecording = isRecording,
                isMuted = isMuted
            )
        } else {
            // Real Camera Preview View
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    val providerFuture = ProcessCameraProvider.getInstance(context)
                    try {
                        val cameraProvider = providerFuture.get()
                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        cameraProvider.unbindAll()
                        if (videoCaptureState != null) {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                videoCaptureState
                            )
                        } else {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("CameraView", "Failed to update PreviewView surface provider", e)
                    }
                }
            )
            
            // Subtle "Live" label
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(AccentGreen, CircleShape)
                    )
                    Text(
                        text = "Front Cam",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SimulatedCameraFeed(
    isRecording: Boolean,
    isMuted: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "simulated_camera")
    
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "simulated_camera_pulse"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "simulated_camera_rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // dark slate background
        contentAlignment = Alignment.Center
    ) {
        // Decorative pulsing camera outline
        Box(
            modifier = Modifier
                .size(72.dp)
                .alpha(pulsingAlpha)
                .border(2.dp, AccentGreen.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Simulated Cam",
                tint = AccentGreen,
                modifier = Modifier.size(32.dp)
            )
        }

        // Recording stats & indicators on simulated stream
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isRecording) AccentRed else Color.Gray, CircleShape)
                )
                Text(
                    text = if (isRecording) "SIMULATED REC" else "SIMULATED PREVIEW",
                    color = if (isRecording) AccentRed else Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            if (isMuted) {
                Text(
                    text = "Muted",
                    color = AccentRed,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
