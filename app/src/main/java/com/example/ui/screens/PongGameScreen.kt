package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ScoreEntry
import com.example.ui.GameViewModel
import com.example.ui.theme.*
import com.example.utils.SoundEffectsManager
import kotlinx.coroutines.android.awaitFrame
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class GameState {
    MENU, PLAYING, GAME_OVER, SCORES
}

@Composable
fun PongGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(GameState.MENU) }
    var currentScore by remember { mutableIntStateOf(0) }
    var collisionFlash by remember { mutableFloatStateOf(0f) } 
    var wallHitFlash by remember { mutableFloatStateOf(0f) } 

    val topScores by viewModel.topScores.collectAsStateWithLifecycle()
    val highScore by viewModel.highScore.collectAsStateWithLifecycle()
    val selectedDifficulty by viewModel.difficulty.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val soundEffectsManager = remember { SoundEffectsManager() }

    DisposableEffect(Unit) {
        onDispose {
            soundEffectsManager.release()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = ThemeBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ThemeBackground)
        ) {
            when (gameState) {
                GameState.MENU -> {
                    MainMenuScreen(
                        highScore = highScore,
                        selectedDifficulty = selectedDifficulty,
                        onDifficultyChange = { viewModel.changeDifficulty(it) },
                        onStartGame = {
                            gameState = GameState.PLAYING
                        },
                        onViewScores = {
                            gameState = GameState.SCORES
                        }
                    )
                }
                GameState.PLAYING -> {
                    GameplayArea(
                        difficulty = selectedDifficulty,
                        soundManager = soundEffectsManager,
                        onGameOver = { score ->
                            currentScore = score
                            viewModel.recordGameScore(score)
                            soundEffectsManager.playGameOver()
                            gameState = GameState.GAME_OVER
                        },
                        onPaddleBounce = {
                            collisionFlash = 1f
                        },
                        onWallBounce = {
                            wallHitFlash = 1f
                        },
                        highScore = highScore,
                        flashOpacity = collisionFlash,
                        wallFlashOpacity = wallHitFlash,
                        onDismissFlash = { collisionFlash = (collisionFlash - 0.15f).coerceAtLeast(0f) },
                        onDismissWallFlash = { wallHitFlash = (wallHitFlash - 0.15f).coerceAtLeast(0f) },
                        onExitGame = {
                            gameState = GameState.MENU
                        }
                    )
                }
                GameState.GAME_OVER -> {
                    GameOverScreen(
                        score = currentScore,
                        highScore = highScore,
                        onRestart = {
                            gameState = GameState.PLAYING
                        },
                        onBackToMenu = {
                            gameState = GameState.MENU
                        }
                    )
                }
                GameState.SCORES -> {
                    ScoresLeaderboard(
                        topScores = topScores,
                        onClearScores = { viewModel.clearHistory() },
                        onBack = { gameState = GameState.MENU }
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    highScore: Int,
    selectedDifficulty: String,
    onDifficultyChange: (String) -> Unit,
    onStartGame: () -> Unit,
    onViewScores: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Decorative Status Header
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CURRENT STAGE BEST",
                style = MaterialTheme.typography.labelSmall,
                color = ThemePrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "$highScore",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
        }

        // Center card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, ThemeBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ThemeSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SportsTennis,
                    contentDescription = "Pong Logo",
                    tint = ThemePrimary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.menu_title).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                
                Text(
                    text = "A dynamic retro pingpong simulator with realistic physics.",
                    fontSize = 12.sp,
                    color = ThemeTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty selectors
                Text(
                    text = stringResource(R.string.difficulty).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemePrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val difficulties = listOf("Easy", "Medium", "Hard")
                    difficulties.forEach { level ->
                        val isSelected = level == selectedDifficulty
                        val chipBg = if (isSelected) ThemePrimary else ThemeBorder.copy(alpha = 0.5f)
                        val chipText = if (isSelected) ThemeSecondary else ThemeTextMuted

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .clickable { onDifficultyChange(level) }
                                .padding(vertical = 12.dp)
                                .testTag("difficulty_${level.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.uppercase(),
                                color = chipText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Play buttons in footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_game_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ThemeSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LAUNCH ACTION",
                        color = ThemeSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onViewScores,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("leaderboard_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemePrimary),
                border = BorderStroke(2.dp, ThemeBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LEADERBOARD RECORDS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GameplayArea(
    difficulty: String,
    soundManager: SoundEffectsManager,
    onGameOver: (Int) -> Unit,
    onPaddleBounce: () -> Unit,
    onWallBounce: () -> Unit,
    highScore: Int,
    flashOpacity: Float,
    wallFlashOpacity: Float,
    onDismissFlash: () -> Unit,
    onDismissWallFlash: () -> Unit,
    onExitGame: () -> Unit
) {
    val density = LocalDensity.current

    // Difficulty settings
    val (paddleWidthRatio, speedFactor) = remember(difficulty) {
        when (difficulty) {
            "Easy" -> Pair(0.38f, 0.70f)
            "Hard" -> Pair(0.18f, 1.35f)
            else -> Pair(0.26f, 1.0f) // Medium
        }
    }

    // Coordinates variables
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    var paddleWidth by remember { mutableFloatStateOf(0f) }
    val paddleHeight = remember { with(density) { 12.dp.toPx() } }

    // TARGET positions representing player input for elastic smoothing
    var targetPaddleX by remember { mutableFloatStateOf(0f) }
    var paddleX by remember { mutableFloatStateOf(0f) }
    var previousPaddleX by remember { mutableFloatStateOf(0f) }
    var paddleVelocityX by remember { mutableFloatStateOf(0f) }

    var score by remember { mutableIntStateOf(0) }
    var ballX by remember { mutableFloatStateOf(0f) }
    var ballY by remember { mutableFloatStateOf(0f) }
    var ballSpeedX by remember { mutableFloatStateOf(0f) }
    var ballSpeedY by remember { mutableFloatStateOf(0f) }

    val ballRadius = remember { with(density) { 9.dp.toPx() } }

    var isRunning by remember { mutableStateOf(true) }
    var isInitialized by remember { mutableStateOf(false) }

    fun initGameCoordinates() {
        if (canvasWidth > 0 && canvasHeight > 0) {
            paddleWidth = canvasWidth * paddleWidthRatio
            paddleX = (canvasWidth - paddleWidth) / 2f
            targetPaddleX = paddleX
            previousPaddleX = paddleX
            paddleVelocityX = 0f
            
            ballX = canvasWidth / 2f
            ballY = canvasHeight * 0.35f
            
            val speedXMultiplier = if (Random.nextBoolean()) 1f else -1f
            ballSpeedX = with(density) { 4.5.dp.toPx() } * speedXMultiplier * speedFactor
            ballSpeedY = with(density) { 5.5.dp.toPx() } * speedFactor
            
            isInitialized = true
        }
    }

    // Core Frame Update Tick
    LaunchedEffect(isInitialized, isRunning, canvasWidth, canvasHeight, difficulty) {
        if (!isInitialized) {
            initGameCoordinates()
        }
        
        while (isRunning && isInitialized) {
            awaitFrame()
            
            if (flashOpacity > 0f) onDismissFlash()
            if (wallFlashOpacity > 0f) onDismissWallFlash()

            // 1. ADVANCED SMOOTH PADDLE PHYSICS LERP
            val interpolationWeight = 0.35f // Weighted slide factor
            paddleX += (targetPaddleX - paddleX) * interpolationWeight
            
            // Calculate movement velocity
            paddleVelocityX = paddleX - previousPaddleX
            previousPaddleX = paddleX

            // 2. BALL MOVEMENT
            ballX += ballSpeedX
            ballY += ballSpeedY

            // 3. COLLISION WITH WALLS
            if (ballX <= ballRadius) {
                ballX = ballRadius + 1f
                ballSpeedX = abs(ballSpeedX)
                soundManager.playBounceWall()
                onWallBounce()
            } else if (ballX >= canvasWidth - ballRadius) {
                ballX = canvasWidth - ballRadius - 1f
                ballSpeedX = -abs(ballSpeedX)
                soundManager.playBounceWall()
                onWallBounce()
            }

            if (ballY <= ballRadius) {
                ballY = ballRadius + 1f
                ballSpeedY = abs(ballSpeedY)
                soundManager.playBounceWall()
                onWallBounce()
            }

            // 4. PRECISION DYNAMIC BALL-PADDLE TOUCH COLLISION
            val paddleTopEdgeY = canvasHeight - paddleHeight - with(density) { 42.dp.toPx() }
            
            // Intersection boundary check
            if (ballY + ballRadius >= paddleTopEdgeY && ballY - ballRadius <= paddleTopEdgeY + paddleHeight) {
                if (ballX >= paddleX - 2f && ballX <= paddleX + paddleWidth + 2f) {
                    score++
                    soundManager.playBouncePaddle()
                    onPaddleBounce()

                    // Calculate point of impact relative to paddle center [-1.0f to 1.0f]
                    val paddleCenter = paddleX + paddleWidth / 2f
                    val hitRelativeOffset = ((ballX - paddleCenter) / (paddleWidth / 2f)).coerceIn(-1f, 1f)

                    // Steering exit angles (Max deviation 60 degrees)
                    val maxDeviationAngle = Math.toRadians(60.0)
                    val exitDevAngle = hitRelativeOffset * maxDeviationAngle
                    val normalAngle = Math.PI / 2.0 // straight up
                    val actualAngle = normalAngle - exitDevAngle

                    // Retain existing vector speed but step up velocity by difficulty factor
                    val stepMultiplier = 1.033f 
                    val currentSpeed = hypot(ballSpeedX, ballSpeedY) * stepMultiplier

                    // Calculate physical components
                    ballSpeedX = (currentSpeed * cos(actualAngle)).toFloat()
                    ballSpeedY = -(currentSpeed * sin(actualAngle)).toFloat()

                    // Momentum transfer from paddle drag/sweeping movements
                    val horizontalMomentumMultiplier = 0.38f
                    ballSpeedX += paddleVelocityX * horizontalMomentumMultiplier

                    // Max speed clamp to keep game playable
                    val maxClampSpeed = with(density) { 24.dp.toPx() }
                    ballSpeedX = ballSpeedX.coerceIn(-maxClampSpeed, maxClampSpeed)
                    ballSpeedY = ballSpeedY.coerceIn(-maxClampSpeed, maxClampSpeed)

                    // Position correction above paddle horizon to block clipping loops
                    ballY = paddleTopEdgeY - ballRadius - 2f
                }
            }

            // 5. MISSED OUT OF ACCESSIBLE HORIZON
            if (ballY >= canvasHeight) {
                isRunning = false
                onGameOver(score)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        // High polish Top Header displaying score inside Bold Typography structure
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExitGame,
                modifier = Modifier.testTag("exit_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit to Menu",
                    tint = ThemeTextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MODE: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemeTextMuted
                )
                Text(
                    text = difficulty.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Epic Bold Header displays Score (like Streak Dashboard in mockup)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CURRENT STREAKSCORE",
                style = MaterialTheme.typography.labelSmall,
                color = ThemePrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontSize = 88.sp,
                lineHeight = 90.sp,
                modifier = Modifier.testTag("current_score_hud")
            )

            // High Score Banner
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ThemeBorder)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BEST RECORD  ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$highScore",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Gameplay Area Grid wrapped in custom structural container matching design mockup
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .pointerInput(canvasWidth, paddleWidth) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (canvasWidth > 0f && paddleWidth > 0f) {
                            // Update target paddle offset (handled smoothly in frame tick interpolation)
                            targetPaddleX = (targetPaddleX + dragAmount.x).coerceIn(0f, canvasWidth - paddleWidth)
                        }
                    }
                }
                .testTag("gameplay_canvas_container")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        canvasWidth = size.width.toFloat()
                        canvasHeight = size.height.toFloat()
                        initGameCoordinates()
                    }
                    .testTag("ping_pong_canvas")
            ) {
                val measuredWidth = size.width
                val measuredHeight = size.height

                // Render deep background space
                drawRect(color = ThemeSurface)

                // Render beautiful design dotted grid
                val dotColor = ThemeBorder.copy(alpha = 0.22f)
                val dotSpacing = 30.dp.toPx()
                var dx = 15f
                while (dx < measuredWidth) {
                    var dy = 15f
                    while (dy < measuredHeight) {
                        drawCircle(color = dotColor, radius = 2.dp.toPx(), center = Offset(dx, dy))
                        dy += dotSpacing
                    }
                    dx += dotSpacing
                }

                // Render Dashed Cyber Container Frame around the arena
                drawRoundRect(
                    color = ThemeBorder,
                    topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                    size = Size(measuredWidth - 2.dp.toPx(), measuredHeight - 2.dp.toPx()),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                    )
                )

                // Center court separator dash
                drawLine(
                    color = ThemeBorder.copy(alpha = 0.25f),
                    start = Offset(0f, measuredHeight / 2f),
                    end = Offset(measuredWidth, measuredHeight / 2f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
                )

                // Draw Top Bounce Wall (Bricks effect flashing on hits)
                val topWallHeight = 16.dp.toPx()
                val activeWallGlow = if (wallFlashOpacity > 0f) {
                    Color.White.copy(alpha = wallFlashOpacity)
                } else {
                    ThemePrimary.copy(alpha = 0.8f)
                }

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(activeWallGlow, ThemeBorder)
                    ),
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = Size(measuredWidth - 8.dp.toPx(), topWallHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Render Paddle
                val paddleTopY = measuredHeight - paddleHeight - 42.dp.toPx()
                val paddleGradBrush = Brush.horizontalGradient(
                    colors = if (flashOpacity > 0f) {
                        listOf(Color.White, ThemePrimary, Color.White)
                    } else {
                        listOf(ThemePrimary.copy(alpha = 0.7f), ThemePrimary, ThemePrimary.copy(alpha = 0.7f))
                    }
                )

                // Custom Rounded Paddle
                drawRoundRect(
                    brush = paddleGradBrush,
                    topLeft = Offset(paddleX, paddleTopY),
                    size = Size(paddleWidth, paddleHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                // Extra drop shadow/glowing outline effect around paddle
                drawRoundRect(
                    color = ThemePrimary.copy(alpha = 0.3f + flashOpacity * 0.5f),
                    topLeft = Offset(paddleX - 4.dp.toPx(), paddleTopY - 2.dp.toPx()),
                    size = Size(paddleWidth + 8.dp.toPx(), paddleHeight + 4.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Render Bouncing Energy Ball
                if (isInitialized) {
                    // Motion energy glow
                    drawCircle(
                        color = ThemePrimary.copy(alpha = 0.3f),
                        radius = ballRadius + 6.dp.toPx(),
                        center = Offset(ballX, ballY)
                    )
                    // Core ball
                    drawCircle(
                        color = Color.White,
                        radius = ballRadius,
                        center = Offset(ballX, ballY)
                    )
                }
            }

            // Keyboard/Tactile Arrow Controls for accessibility navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow Pill
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ThemeBorder.copy(alpha = 0.8f))
                        .clickable {
                            if (canvasWidth > 0f && paddleWidth > 0f) {
                                targetPaddleX = (targetPaddleX - with(density) { 40.dp.toPx() }).coerceIn(
                                    0f,
                                    canvasWidth - paddleWidth
                                )
                            }
                        }
                        .testTag("left_paddle_arrow"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowLeft,
                        contentDescription = "Move Left",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "SWIPE TO DRAG PADDLE",
                    color = ThemeTextMuted.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Right Arrow Pill
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ThemeBorder.copy(alpha = 0.8f))
                        .clickable {
                            if (canvasWidth > 0f && paddleWidth > 0f) {
                                targetPaddleX = (targetPaddleX + with(density) { 40.dp.toPx() }).coerceIn(
                                    0f,
                                    canvasWidth - paddleWidth
                                )
                            }
                        }
                        .testTag("right_paddle_arrow"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = "Move Right",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    highScore: Int,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("game_over_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, ThemeLostAccent, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ThemeSurface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentVeryDissatisfied,
                    contentDescription = "Lost Sad Mood Icon",
                    tint = ThemeLostAccent,
                    modifier = Modifier.size(72.dp)
                )

                // Crucial Custom Text constraint request!
                // Whenever i miss, MUST show "opps you lost zafaryab"
                Text(
                    text = stringResource(R.string.game_over_lost_message),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ThemeLostAccent,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .testTag("game_over_lost_text")
                )

                Text(
                    text = "The ball breached your line. Defend the bounce next time.",
                    fontSize = 12.sp,
                    color = ThemeTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                HorizontalDivider(
                    color = ThemeBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Large Bold Metrics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STREAK SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemeTextMuted
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            modifier = Modifier.testTag("final_score_label")
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STAGE BEST",
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemeTextMuted
                        )
                        Text(
                            text = "$highScore",
                            style = MaterialTheme.typography.displayMedium,
                            color = ThemePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action controls
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("try_again_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemePrimary,
                        contentColor = ThemeSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = ThemeSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.try_again).uppercase(),
                            color = ThemeSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, ThemeBorder, RoundedCornerShape(12.dp))
                        .testTag("back_to_menu_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeSecondary,
                        contentColor = ThemePrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "BACK TO LOBBY",
                        color = ThemePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ScoresLeaderboard(
    topScores: List<ScoreEntry>,
    onClearScores: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_from_scores")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Menu",
                    tint = Color.White
                )
            }

            Text(
                text = "HIGH SCORES",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 20.sp,
                color = Color.White
            )

            IconButton(
                onClick = onClearScores,
                enabled = topScores.isNotEmpty(),
                modifier = Modifier.testTag("clear_scores_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Leaderboard",
                    tint = if (topScores.isNotEmpty()) ThemeLostAccent else ThemeTextMuted
                )
            }
        }

        if (topScores.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ThemeBorder,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO HISTORIC RECORDS FOUND",
                        color = ThemeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Establish a high score on easy, medium or hard mode!",
                        color = ThemeTextMuted.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("scores_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(topScores) { scoreItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        border = BorderStroke(1.dp, ThemeBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = ThemePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "DIFFICULTY: ${scoreItem.difficulty.uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemePrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    val formattedDate = remember(scoreItem.timestamp) {
                                        val date = java.util.Date(scoreItem.timestamp)
                                        val format = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                                        format.format(date)
                                    }
                                    Text(
                                        text = formattedDate,
                                        fontSize = 10.sp,
                                        color = ThemeTextMuted
                                    )
                                }
                            }

                            Text(
                                text = "${scoreItem.score}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, ThemeBorder, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = ThemeSecondary,
                contentColor = ThemePrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "BACK TO LOBBY",
                color = ThemePrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
