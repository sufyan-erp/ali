package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.ScoreEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    private val _difficulty = MutableStateFlow("Medium")
    val difficulty: StateFlow<String> = _difficulty.asStateFlow()

    // High score flow dynamically switching whenever the difficulty changes
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val highScore: StateFlow<Int> = _difficulty.flatMapLatest { diff ->
        repository.getHighScoreForDifficulty(diff).map { it ?: 0 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Top 10 offline local high scores list
    val topScores: StateFlow<List<ScoreEntry>> = repository.topScores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun changeDifficulty(newDifficulty: String) {
        _difficulty.value = newDifficulty
    }

    fun recordGameScore(score: Int) {
        viewModelScope.launch {
            repository.saveScore(score, _difficulty.value)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    class Factory(
        private val application: Application,
        private val repository: GameRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
