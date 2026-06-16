package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.ui.GameViewModel
import com.example.ui.screens.PongGameScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Local Offline High Score Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.scoreDao())
        
        // Instantiate the Game ViewModel using the Factory pattern
        val viewModelFactory = GameViewModel.Factory(application, repository)
        val gameViewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PongGameScreen(viewModel = gameViewModel)
            }
        }
    }
}
