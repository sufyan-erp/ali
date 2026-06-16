package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<ScoreEntry>>

    @Query("SELECT MAX(score) FROM high_scores WHERE difficulty = :difficulty")
    fun getHighScoreForDifficulty(difficulty: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(scoreEntry: ScoreEntry)

    @Query("DELETE FROM high_scores")
    suspend fun clearAllScores()
}
