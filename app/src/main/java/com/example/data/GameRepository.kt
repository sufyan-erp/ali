package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val scoreDao: ScoreDao) {
    val topScores: Flow<List<ScoreEntry>> = scoreDao.getTopScores()

    fun getHighScoreForDifficulty(difficulty: String): Flow<Int?> {
        return scoreDao.getHighScoreForDifficulty(difficulty)
    }

    suspend fun saveScore(score: Int, difficulty: String) {
        val entry = ScoreEntry(score = score, difficulty = difficulty)
        scoreDao.insertScore(entry)
    }

    suspend fun clearHistory() {
        scoreDao.clearAllScores()
    }
}
