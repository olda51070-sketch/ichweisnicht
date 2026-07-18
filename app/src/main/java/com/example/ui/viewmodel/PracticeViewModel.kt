package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PracticeRepository
import com.example.data.PracticeSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class CallerPartner(
    val id: String,
    val name: String,
    val role: String,
    val imageRes: Int,
    val description: String,
    val greeting: String
)

data class SpeechTopic(
    val id: String,
    val title: String,
    val description: String,
    val prompts: List<String>
)

class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PracticeRepository

    val partners = listOf(
        CallerPartner(
            id = "sophia",
            name = "Sophia",
            role = "Close Friend",
            imageRes = com.example.R.drawable.caller_female,
            description = "Casual, friendly, and supportive. Ideal for relaxing and speaking naturally.",
            greeting = "Hey! How's it going? Tell me about your day, I'm all ears!"
        ),
        CallerPartner(
            id = "alex",
            name = "Alex",
            role = "Work Colleague",
            imageRes = com.example.R.drawable.caller_male,
            description = "Professional but relaxed. Good for practice updates, work reports, or status syncs.",
            greeting = "Hey there! Ready for our sync? Let me know how that project is going."
        ),
        CallerPartner(
            id = "dr_harrison",
            name = "Dr. Harrison",
            role = "University Professor / Boss",
            imageRes = com.example.R.drawable.caller_formal,
            description = "Formal, analytical, and highly structured. Excellent for academic presentations or formal pitches.",
            greeting = "Welcome. Please go ahead and present your ideas. I am listening closely."
        )
    )

    val topics = listOf(
        SpeechTopic(
            id = "free",
            title = "Free Flow Speaking",
            description = "No structured topic. Just ramble, complain, think aloud, or gossip naturally.",
            prompts = listOf(
                "Talk about what you had for breakfast.",
                "Rant about the weather or traffic today.",
                "Gossip about a movie or show you recently watched.",
                "Just hum, laugh, or complain about being tired."
            )
        ),
        SpeechTopic(
            id = "elevator_pitch",
            title = "Elevator Pitch",
            description = "Synthesize and present a business idea, app idea, or your professional skills under 2 minutes.",
            prompts = listOf(
                "What is the problem you are solving?",
                "How does your solution work?",
                "What is your unique value proposition?",
                "Why should someone invest in your idea?"
            )
        ),
        SpeechTopic(
            id = "storytelling",
            title = "Personal Storytelling",
            description = "Practice emotional delivery and pacing by sharing a memorable life event.",
            prompts = listOf(
                "Describe your most embarrassing childhood memory.",
                "Tell a story about a time you got completely lost.",
                "What was the happiest day of your life so far?",
                "Describe a lesson you learned the hard way."
            )
        ),
        SpeechTopic(
            id = "tech_sharing",
            title = "Explain Like I'm Five",
            description = "Break down a complex technical concept into very simple, accessible terms.",
            prompts = listOf(
                "Explain how the internet works to a 5-year-old.",
                "What is artificial intelligence in simple words?",
                "Explain how a refrigerator keeps things cold.",
                "Describe how money/inflation works."
            )
        )
    )

    // Current selections
    var selectedPartner by mutableStateOf(partners[0])
    var selectedTopic by mutableStateOf(topics[0])
    var customTopicInput by mutableStateOf("")

    // Active practice state
    var isCallInProgress by mutableStateOf(false)
    var isAudioMuted by mutableStateOf(false)
    var isCameraOff by mutableStateOf(false)
    var currentSessionSeconds by mutableStateOf(0)

    // Last completed session for review
    var lastRecordedFilePath by mutableStateOf<String?>(null)
    var reviewRating by mutableStateOf(3)
    var reviewNotes by mutableStateOf("")
    var showReviewDialog by mutableStateOf(false)

    // Database sessions list
    val sessions: StateFlow<List<PracticeSession>>

    // Calculated statistics
    val totalPracticeMinutes: StateFlow<Int>
    val totalSessionsCount: StateFlow<Int>
    val streakDays: StateFlow<Int>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PracticeRepository(database.practiceSessionDao())
        
        sessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        totalSessionsCount = sessions.map { list -> list.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        totalPracticeMinutes = sessions.map { list ->
            val totalSeconds = list.sumOf { it.durationSeconds }
            (totalSeconds + 59) / 60 // Round up to nearest minute
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        // Calculate active streak
        streakDays = sessions.map { list ->
            calculateStreak(list)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    private fun calculateStreak(list: List<PracticeSession>): Int {
        if (list.isEmpty()) return 0
        // Find distinct dates
        val dates = list.map { 
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        if (dates.isEmpty()) return 0

        val todayCal = java.util.Calendar.getInstance()
        todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        todayCal.set(java.util.Calendar.MINUTE, 0)
        todayCal.set(java.util.Calendar.SECOND, 0)
        todayCal.set(java.util.Calendar.MILLISECOND, 0)
        val today = todayCal.timeInMillis

        val yesterday = today - 24 * 60 * 60 * 1000

        // Streak is broken if the first practice in list is older than yesterday
        if (dates[0] < yesterday && dates[0] != today) {
            return 0
        }

        var streak = 1
        var currentExpected = dates[0]
        for (i in 1 until dates.size) {
            val expectedYesterday = currentExpected - 24 * 60 * 60 * 1000
            if (dates[i] == expectedYesterday) {
                streak++
                currentExpected = expectedYesterday
            } else {
                break
            }
        }
        return streak
    }

    fun startCall() {
        isCallInProgress = true
        currentSessionSeconds = 0
        isAudioMuted = false
        isCameraOff = false
    }

    fun endCall(filePath: String?, seconds: Int) {
        isCallInProgress = false
        if (filePath != null && seconds >= 2) {
            lastRecordedFilePath = filePath
            currentSessionSeconds = seconds
            reviewRating = 3
            reviewNotes = ""
            showReviewDialog = true
        }
    }

    fun saveReview() {
        val path = lastRecordedFilePath ?: return
        val currentTopicTitle = if (selectedTopic.id == "free" && customTopicInput.isNotBlank()) {
            customTopicInput
        } else {
            selectedTopic.title
        }

        viewModelScope.launch {
            repository.insert(
                PracticeSession(
                    partnerName = selectedPartner.name,
                    topic = currentTopicTitle,
                    durationSeconds = currentSessionSeconds,
                    videoFilePath = path,
                    confidenceRating = reviewRating,
                    notes = reviewNotes
                )
            )
            showReviewDialog = false
            lastRecordedFilePath = null
        }
    }

    fun discardReview() {
        val path = lastRecordedFilePath
        if (path != null) {
            // Delete actual file to save space
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        showReviewDialog = false
        lastRecordedFilePath = null
    }

    fun deleteSession(session: PracticeSession) {
        viewModelScope.launch {
            repository.delete(session)
            // Also delete physical file
            try {
                val file = File(session.videoFilePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
