package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class ScoreEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis()
)
