package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerName: String,
    val topic: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val videoFilePath: String,
    val confidenceRating: Int = 3, // 1-5 scale
    val notes: String = ""
)
