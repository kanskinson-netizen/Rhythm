package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.R
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.FestiveDecorations
import chromahub.rhythm.app.ui.theme.FestiveTheme
import chromahub.rhythm.app.ui.theme.FestiveThemeConfig
import chromahub.rhythm.app.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SplashScreen(
    musicViewModel: MusicViewModel,
    onMediaScanComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    
    // Festive theme states
    val festiveThemeEnabled by appSettings.festiveThemeEnabled.collectAsState()
    val festiveThemeSelected by appSettings.festiveThemeSelected.collectAsState()
    val festiveThemeAutoDetect by appSettings.festiveThemeAutoDetect.collectAsState()
    val festiveThemeShowParticles by appSettings.festiveThemeShowParticles.collectAsState()
    val festiveThemeShowDecorations by appSettings.festiveThemeShowDecorations.collectAsState()
    val festiveThemeParticleIntensity by appSettings.festiveThemeParticleIntensity.collectAsState()
    val festiveThemeShowEmojiDecorations by appSettings.festiveThemeShowEmojiDecorations.collectAsState()
    val festiveThemeEmojiDecorationsIntensity by appSettings.festiveThemeEmojiDecorationsIntensity.collectAsState()
    val festiveThemeApplyToSplash by appSettings.festiveThemeApplyToSplash.collectAsState()
    
    // Determine active festive theme
    val activeFestiveTheme = remember(festiveThemeEnabled, festiveThemeAutoDetect, festiveThemeSelected) {
        if (!festiveThemeEnabled) {
            FestiveTheme.NONE
        } else if (festiveThemeAutoDetect) {
            FestiveTheme.detectCurrentFestival()
        } else {
            try {
                FestiveTheme.valueOf(festiveThemeSelected)
            } catch (e: Exception) {
                FestiveTheme.NONE
            }
        }
    }
    
    // Memoize the festive greeting to prevent re-randomization on recomposition
    val festiveGreeting = remember(activeFestiveTheme) {
        getFestiveGreeting(activeFestiveTheme)
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "splashAnimations")
    
    // Subtle breathing animation for logo
    val logoBreathing by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoBreathing"
    )

    // Animation state flags
    var showLogo by remember { mutableStateOf(false) }
    var showAppName by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showLoader by remember { mutableStateOf(false) }
    var exitSplash by remember { mutableStateOf(false) }

    // Animatable values for smooth animations
    val logoAlpha = remember { Animatable(1f) } // Start visible
    val logoScaleAnim = remember { Animatable(1.0f) } // Start at full splash screen size
    val logoOffsetX = remember { Animatable(0f) } // Logo position - slides left
    
    val appNameOffsetX = remember { Animatable(0f) } // Starts centered behind logo - slides right
    
    val loaderAlpha = remember { Animatable(0f) }
    
    val exitScale = remember { Animatable(1f) }
    val exitAlpha = remember { Animatable(1f) }

    // Monitor media scanning completion
    val isInitialized by musicViewModel.isInitialized.collectAsState()

    LaunchedEffect(Unit) {
        delay(150) // Brief initial delay to match system splash

        // STEP 1: Logo is already visible at full size (matches system splash)
        showLogo = true
        delay(250) // Hold at full size briefly
        
        // Shrink logo to fit alongside text
        logoScaleAnim.animateTo(
            targetValue = 0.55f, // Shrink to smaller size for side-by-side layout
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        delay(100) // Brief pause after shrink

        // STEP 2: Logo slides LEFT and App name appears with expand animation (like TabButton)
        showAppName = true
        // Logo slides left
        launch {
            logoOffsetX.animateTo(
                targetValue = -265f, // Slide left from center
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        // App name offset happens via AnimatedVisibility now
        appNameOffsetX.animateTo(
            targetValue = 105f, // Slide right from center
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        delay(250) // Pause for name to settle

        // STEP 3: Tagline appears with expand animation
        showTagline = true

        delay(150) // Brief delay before loader

        // STEP 4: Loader fades in smoothly
        showLoader = true
        loaderAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = EaseInOut)
        )
    }



    // Handle media scanning completion - exit animation
    LaunchedEffect(isInitialized) {
        if (isInitialized && !exitSplash) {
            delay(2000)
            exitSplash = true
            
            launch {
                exitScale.animateTo(0.90f, animationSpec = tween(350))
            }
            exitAlpha.animateTo(0f, animationSpec = tween(350))
            
            delay(100)
            onMediaScanComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer {
                scaleX = exitScale.value
                scaleY = exitScale.value
                alpha = exitAlpha.value
            },
        contentAlignment = Alignment.Center
    ) {
        // Festive decorations overlay
        if (festiveThemeEnabled && festiveThemeApplyToSplash && activeFestiveTheme != FestiveTheme.NONE) {
            FestiveDecorations(
                config = FestiveThemeConfig(
                    enabled = festiveThemeEnabled,
                    selectedTheme = activeFestiveTheme,
                    autoDetect = festiveThemeAutoDetect,
                    showParticles = festiveThemeShowParticles,
                    particleIntensity = festiveThemeParticleIntensity,
                    applyToSplash = festiveThemeApplyToSplash,
                    applyToMainUI = false
                ),
                modifier = Modifier.fillMaxSize()
            )
            
            // Emoji decorations overlay
            if (festiveThemeShowEmojiDecorations) {
                EmojiDecorationsOverlay(
                    theme = activeFestiveTheme,
                    intensity = festiveThemeEmojiDecorationsIntensity,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Background particles using the drawable
//        AnimatedVisibility(
//            visible = showContent,
//            enter = fadeIn(animationSpec = tween(1000))
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.splash_particles),
//                contentDescription = null,
//                modifier = Modifier
//                    .size(300.dp)
//                    .graphicsLayer {
//                        alpha = 0.3f
//                        scaleX = logoScale * 1.2f
//                        scaleY = logoScale * 1.2f
//                    }
//            )
//        }

        // Main content with dramatic text reveal
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo and App name on the same line
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // App name revealing with expand animation
                    // Rendered first so it appears BEHIND the logo in z-order
                    if (showAppName) {
                        Row(
                            modifier = Modifier.graphicsLayer {
                                translationX = appNameOffsetX.value
                            }
                        ) {
                            AnimatedVisibility(
                                visible = showAppName,
                                enter = expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(400)
                                ),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Text(
                                    text = "Rhythm",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 48.sp,
                                        letterSpacing = 2.sp
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    // Logo with entrance animation and breathing effect (centered, then slides left)
                    // Rendered second so it appears ON TOP of the text in z-order
                    if (showLogo) {
                        Image(
                            painter = painterResource(id = R.drawable.rhythm_splash_logo),
                            contentDescription = "Rhythm",
                            modifier = Modifier
                                .size(200.dp) // Larger base size to match system splash
                                .graphicsLayer {
                                    alpha = logoAlpha.value
                                    scaleX = logoScaleAnim.value * logoBreathing
                                    scaleY = logoScaleAnim.value * logoBreathing
                                    translationX = logoOffsetX.value
                                }
                        )
                    }
                }

                // Tagline with expand animation from center
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (showTagline) {
                        Row {
                            AnimatedVisibility(
                                visible = showTagline,
                                enter = expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(400)
                                ) + slideInVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    initialOffsetY = { 50 }
                                ),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Show festive greeting if enabled and decorations are on
                                    if (festiveThemeEnabled && festiveThemeApplyToSplash && 
                                        festiveThemeShowDecorations && 
                                        activeFestiveTheme != FestiveTheme.NONE) {
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = activeFestiveTheme.emoji,
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = festiveGreeting,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    letterSpacing = 1.sp,
                                                    fontSize = 17.sp
                                                ),
                                                fontWeight = FontWeight.Medium,
                                                color = activeFestiveTheme.primaryColor,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = activeFestiveTheme.emoji,
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    
                                    Text(
                                        text = "Your Music, Your Rhythm",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            letterSpacing = 1.sp,
                                            fontSize = 17.sp
                                        ),
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loading indicator at bottom with fade in
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .graphicsLayer {
                    alpha = loaderAlpha.value
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            if (showLoader) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Loading your music library...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val animationDelay = index * 150
                            val dotScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        600,
                                        delayMillis = animationDelay,
                                        easing = EaseInOut
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "dot$index"
                            )

                            Surface(
                                modifier = Modifier
                                    .size(6.dp)
                                    .scale(dotScale),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

/**
 * Emoji decorations overlay for festive themes
 */
@Composable
fun EmojiDecorationsOverlay(
    theme: FestiveTheme,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    if (theme.emojiDecorations.isEmpty() || intensity <= 0f) return

    // Generate unique layout ID per screen instance
    val layoutId = remember { Random.nextInt(10000) }
    
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        
        val emojiData = remember(theme, intensity, layoutId, screenWidth, screenHeight) {
            val emojiCount = (theme.emojiDecorations.size * intensity * 1.5f).toInt().coerceIn(8, 20)
            val selectedEmojis = theme.emojiDecorations.shuffled(Random(layoutId)).take(emojiCount)
            
            selectedEmojis.mapIndexed { index, emoji ->
                val random = Random(layoutId + index * 123)
                
                // Distribute emojis ONLY around the screen edges (corners and sides)
                val edgeSelection = random.nextFloat()
                val (x, y) = when {
                    // Top edge: 30% of emojis
                    edgeSelection < 0.30f -> {
                        Pair(
                            random.nextFloat() * screenWidth,
                            random.nextFloat() * 80f // Top 80px
                        )
                    }
                    // Bottom edge: 30% of emojis
                    edgeSelection < 0.60f -> {
                        Pair(
                            random.nextFloat() * screenWidth,
                            screenHeight - random.nextFloat() * 80f // Bottom 80px
                        )
                    }
                    // Left edge: 20% of emojis
                    edgeSelection < 0.80f -> {
                        Pair(
                            random.nextFloat() * 80f, // Left 80px
                            random.nextFloat() * screenHeight
                        )
                    }
                    // Right edge: 20% of emojis
                    else -> {
                        Pair(
                            screenWidth - random.nextFloat() * 80f, // Right 80px
                            random.nextFloat() * screenHeight
                        )
                    }
                }
                
                val rotation = random.nextInt(-30, 31).toFloat()
                val alpha = 0.25f + random.nextFloat() * 0.35f
                val size = 0.8f + random.nextFloat() * 0.6f
                
                EmojiData(emoji, x, y, rotation, alpha, size)
            }
        }

        emojiData.forEach { data ->
            Text(
                text = data.emoji,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = (20 * data.size).sp
                ),
                modifier = Modifier
                    .offset(x = data.x.dp, y = data.y.dp)
                    .graphicsLayer {
                        rotationZ = data.rotation
                        this.alpha = data.alpha
                    }
            )
        }
    }
}

/**
 * Data class to hold emoji decoration information
 */
private data class EmojiData(
    val emoji: String,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val alpha: Float,
    val size: Float = 1f
)

/**
 * Get festive greeting text for the given theme with random comic variations
 */
private fun getFestiveGreeting(theme: FestiveTheme): String {
    val greetings = when (theme) {
        FestiveTheme.DIWALI -> listOf(
            "Happy Diwali! 🪔✨",
            "Light Up Your Music! 🎵🪔",
            "Diwali Vibes & Music Tribes! 🎉",
            "Sparkle Like Your Playlist! ✨🎶",
            "Festival of Lights & Beats! 💫",
            "Diwali Dhamaka! 🎇🎵"
        )
        FestiveTheme.CHRISTMAS -> listOf(
            "Merry Christmas! 🎄🎁",
            "Jingle All The Way! 🔔🎵",
            "Ho Ho Ho & Let's Go! 🎅✨",
            "Santa's Got Your Playlist! 🎁🎶",
            "Rockin' Around The Music Tree! 🎄🎸",
            "Sleigh Your Musical Day! 🛷🎵",
            "Jolly Tunes & Christmas Moons! 🌙⭐"
        )
        FestiveTheme.NEW_YEAR -> listOf(
            "Happy New Year! 🎊🎉",
            "New Year, New Beats! 🎵✨",
            "Countdown to Music! 🕛🎶",
            "Cheers to Fresh Tunes! 🥂🎵",
            "365 Days of Rhythm! 📅🎶",
            "Let's Rock This Year! 🎸🎊",
            "Turn Up for the New Year! 🔊🎉"
        )
        FestiveTheme.HOLI -> listOf(
            "Happy Holi! 🎨🌈",
            "Paint The Town With Music! 🎵🎨",
            "Colors of Melody! 🌈🎶",
            "Splash Beats Everywhere! 💦🎵",
            "Rainbow Rhythms! 🌈✨",
            "Let's Get Colorful! 🎨🎉"
        )
        FestiveTheme.HALLOWEEN -> listOf(
            "Happy Halloween! 🎃👻",
            "Spooky Beats Alert! 👻🎵",
            "Trick or Treat Your Ears! 🍬🎶",
            "Creep It Real! 🎃✨",
            "Boo-tiful Music Time! 👻🎵",
            "Scary Good Playlists! 🦇🎶",
            "Fang-tastic Vibes! 🧛🎵"
        )
        FestiveTheme.VALENTINES -> listOf(
            "Happy Valentine's Day! 💕💘",
            "Love Your Music! ❤️🎵",
            "Cupid's Playlist! 💘🎶",
            "Music From The Heart! 💗🎵",
            "Love Songs & Good Vibes! 💝✨",
            "You + Music = 💕",
            "Rhythm of Love! 💓🎶"
        )
        FestiveTheme.EASTER -> listOf(
            "Happy Easter! 🐰🥚",
            "Hop Into Music! 🐇🎵",
            "Egg-cellent Tunes! 🥚🎶",
            "Spring Into Rhythm! 🌸🎵",
            "Bunny Approved Beats! 🐰✨",
            "Crack Open Some Jams! 🥚🎶"
        )
        FestiveTheme.INDEPENDENCE_DAY -> listOf(
            "Happy Independence Day! 🇮🇳🎆",
            "Freedom to Rock! 🎸🇮🇳",
            "Patriotic Beats! 🎵🇮🇳",
            "Celebrate with Music! 🎆🎶",
            "Liberty & Melodies! ✨🇮🇳",
            "Nation's Rhythm! 🎵🇮🇳"
        )
        FestiveTheme.THANKSGIVING -> listOf(
            "Happy Thanksgiving! 🦃🍂",
            "Grateful for Music! 🎵🙏",
            "Feast on Beats! 🍽️🎶",
            "Thankful Tunes! 🦃🎵",
            "Harvest of Melodies! 🍂🎶",
            "Turkey & Tunes! 🦃🎵"
        )
        else -> listOf("")
    }
    
    // Return random greeting from the list
    return greetings.randomOrNull() ?: ""
}
