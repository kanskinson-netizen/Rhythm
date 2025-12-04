package chromahub.rhythm.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.theme.PlayerButtonColor
import chromahub.rhythm.app.ui.theme.PlayerProgressColor
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.M3ImageUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.concurrent.TimeUnit
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import chromahub.rhythm.app.ui.components.M3LinearLoader
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.data.AppSettings
import androidx.compose.runtime.collectAsState


/**
 * Mini player that appears at the bottom of the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val useHoursFormat by appSettings.useHoursInTimeFormat.collectAsState()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )
    
    // Animation for tap feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )

    // Animation for song change bounce effect
    var songChangeBounceTrigger by remember { mutableStateOf(false) }
    val songBounceScale by animateFloatAsState(
        targetValue = if (songChangeBounceTrigger) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "songBounceScale"
    )

    // Animation for initial appearance bounce effect
    var initialAppearanceBounceTrigger by remember { mutableStateOf(false) }
    val initialAppearanceBounceScale by animateFloatAsState(
        targetValue = if (initialAppearanceBounceTrigger) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "initialAppearanceBounceScale"
    )

    // Trigger bounce animation when mini player first appears
    LaunchedEffect(song) {
        if (song != null) {
            // Trigger initial appearance bounce
            initialAppearanceBounceTrigger = true
            delay(150)
            initialAppearanceBounceTrigger = false
            
            // Then trigger song change bounce after a short delay
            delay(50)
            songChangeBounceTrigger = true
            delay(100)
            songChangeBounceTrigger = false
        }
    }
    
    // For swipe gesture detection
    var offsetY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    val swipeUpThreshold = 100f // Minimum distance to trigger player open
    val swipeDownThreshold = 100f // Minimum distance to trigger dismissal
    val swipeHorizontalThreshold = 120f // Minimum distance to trigger prev/next
    
    // Track last offset for haptic feedback at intervals
    var lastHapticOffset by remember { mutableStateOf(0f) }
    var lastHapticOffsetX by remember { mutableStateOf(0f) }
    
    // Animation for translation during swipe
    val translationOffsetY by animateFloatAsState(
        targetValue = if (offsetY > 0) offsetY.coerceAtMost(200f) else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "translationOffsetY"
    )
    
    val translationOffsetX by animateFloatAsState(
        targetValue = offsetX.coerceIn(-300f, 300f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "translationOffsetX"
    )
    
    // Calculate alpha based on offset
    val alphaValue by animateFloatAsState(
        targetValue = if (offsetY > 0) {
            // Fade out as user swipes down
            (1f - (offsetY / 300f)).coerceIn(0.2f, 1f)
        } else {
            1f
        },
        label = "alphaValue"
    )
    
    // For tracking if the mini player is being dismissed
    var isDismissingPlayer by remember { mutableStateOf(false) }
    
    // If dismissing, animate out and stop playback
    LaunchedEffect(isDismissingPlayer) {
        if (isDismissingPlayer) {
            // Stop playback when dismissing
            if (isPlaying) {
                onPlayPause()
            }
            delay(300) // Wait for animation to complete
            // Call the dismiss callback to hide the player
            onDismiss()
            // Reset local state
            isDismissingPlayer = false
            offsetY = 0f
        }
    }

    Card(
        onClick = {
            if (!isDismissingPlayer) {
                // Enhanced haptic feedback for click - respecting settings
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                onPlayerClick()
            }
        },
        shape = RoundedCornerShape(24.dp), // Slightly reduced corner radius for better visual balance
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp, // Remove elevation as requested
            pressedElevation = 0.dp  // Remove press elevation too
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp) // Simple fixed spacing - system insets handled by parent
            .scale(scale * songBounceScale * initialAppearanceBounceScale) // Combined scale for all bounce effects
            .graphicsLayer { 
                // Apply translation based on swipe gesture
                translationY = if (isDismissingPlayer) 300f else translationOffsetY
                translationX = translationOffsetX
                alpha = alphaValue
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        // Reset the last haptic offsets on new drag
                        lastHapticOffset = 0f
                        lastHapticOffsetX = 0f
                        
                        // Initial feedback when starting to drag - respecting settings
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                    },
                    onDragEnd = {
                        // Determine which gesture was dominant
                        val absX = abs(offsetX)
                        val absY = abs(offsetY)
                        
                        if (absX > absY) {
                            // Horizontal swipe is dominant
                            if (offsetX < -swipeHorizontalThreshold) {
                                // Swipe left - next track
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onSkipNext()
                            } else if (offsetX > swipeHorizontalThreshold) {
                                // Swipe right - previous track
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onSkipPrevious()
                            } else {
                                // Not enough swipe distance
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            }
                        } else {
                            // Vertical swipe is dominant
                            if (offsetY < -swipeUpThreshold) {
                                // Swipe up detected, open player with stronger feedback
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onPlayerClick()
                            } else if (offsetY > swipeDownThreshold) {
                                // Swipe down detected, dismiss mini player with stronger feedback
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                isDismissingPlayer = true
                            } else {
                                // Snap-back haptic when releasing before threshold
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            }
                        }
                        
                        // Reset offsets if not dismissing
                        if (!isDismissingPlayer) {
                            offsetY = 0f
                            offsetX = 0f
                        }
                    },
                    onDragCancel = {
                        // Feedback when drag canceled
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        // Reset offsets if not dismissing
                        if (!isDismissingPlayer) {
                            offsetY = 0f
                            offsetX = 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Update offsets for both horizontal and vertical gestures
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        
                        // Provide interval haptic feedback during drag
                        // For vertical swipes
                        if (abs(offsetY) > abs(offsetX)) {
                            // Vertical is dominant
                            if (offsetY < 0 && abs(offsetY) - abs(lastHapticOffset) > swipeUpThreshold / 3) {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                lastHapticOffset = offsetY
                            } else if (offsetY > 0 && abs(offsetY) - abs(lastHapticOffset) > swipeDownThreshold / 3) {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                lastHapticOffset = offsetY
                            }
                        } else {
                            // Horizontal is dominant
                            if (abs(offsetX) - abs(lastHapticOffsetX) > swipeHorizontalThreshold / 3) {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                lastHapticOffsetX = offsetX
                            }
                        }
                    }
                )
            },
        interactionSource = interactionSource
    ) {
        // Display visual hints when user starts dragging
        val dragUpIndicatorAlpha = if (offsetY < 0 && abs(offsetY) > abs(offsetX)) minOf((-offsetY / swipeUpThreshold) * 0.3f, 0.3f) else 0f
        val dragDownIndicatorAlpha = if (offsetY > 0 && abs(offsetY) > abs(offsetX)) minOf((offsetY / swipeDownThreshold) * 0.3f, 0.3f) else 0f
        val dragLeftIndicatorAlpha = if (offsetX < 0 && abs(offsetX) > abs(offsetY)) minOf((-offsetX / swipeHorizontalThreshold) * 0.3f, 0.3f) else 0f
        val dragRightIndicatorAlpha = if (offsetX > 0 && abs(offsetX) > abs(offsetY)) minOf((offsetX / swipeHorizontalThreshold) * 0.3f, 0.3f) else 0f
        
        Column {
            // Enhanced drag handle indicator with better visual feedback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(40.dp) // Slightly wider for better touch target
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.4f + dragUpIndicatorAlpha + dragDownIndicatorAlpha + dragLeftIndicatorAlpha + dragRightIndicatorAlpha
                    )
                )
            }

            // Visual indicators for swipe actions with improved positioning
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Swipe up indicator
                androidx.compose.animation.AnimatedVisibility(
                    visible = offsetY < -20f && abs(offsetY) > abs(offsetX),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = (-offsetY / swipeUpThreshold).coerceIn(0f, 0.8f)),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "⬆ Swipe up for full player",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Swipe down indicator
                androidx.compose.animation.AnimatedVisibility(
                    visible = offsetY > 20f && abs(offsetY) > abs(offsetX),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = (offsetY / swipeDownThreshold).coerceIn(0f, 0.8f)),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "⬇ Swipe down to dismiss",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Swipe left indicator (next track)
                androidx.compose.animation.AnimatedVisibility(
                    visible = offsetX < -20f && abs(offsetX) > abs(offsetY),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = (-offsetX / swipeHorizontalThreshold).coerceIn(0f, 0.8f)),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "⬅ Next track",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Swipe right indicator (previous track)
                androidx.compose.animation.AnimatedVisibility(
                    visible = offsetX > 20f && abs(offsetX) > abs(offsetY),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = (offsetX / swipeHorizontalThreshold).coerceIn(0f, 0.8f)),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Previous track ➡",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Progress bar for the mini player
            if (song != null) {
                LinearProgressIndicator(
                    progress = { animatedProgress }, // Use lambda for progress
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp) // Added horizontal padding
                        .height(4.dp), // Thinner progress bar
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp), // Increased padding for better spacing
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(16.dp)
            ) {
                // Enhanced album art with no shadows as requested
                Surface(
                    modifier = Modifier
                        .size(56.dp), // Slightly smaller for better proportion
                    shape = RoundedCornerShape(14.dp), // Adjusted corner radius
                    shadowElevation = 0.dp, // Remove shadow as requested
                    tonalElevation = 2.dp, // Keep subtle tonal elevation for depth
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box {
                        if (song != null) {
                            M3ImageUtils.TrackImage(
                                imageUrl = song.artworkUri,
                                trackName = song.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Enhanced "live" badge with better styling
                        if (song?.title?.contains("LIVE", ignoreCase = true) == true || 
                            song?.genre?.contains("live", ignoreCase = true) == true) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
                
                // Enhanced song info with better typography and spacing
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = spacedBy(2.dp) // Tighter spacing
                ) {
                    Text(
                        text = song?.title ?: "No song playing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(6.dp)
                    ) {
                        // Artist info with enhanced styling
                        Text(
                            text = song?.artist ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        // Compact time indicator with better styling
                        if (song != null && progress > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${formatDuration((progress * song.duration).toLong(), useHoursFormat)}/${formatDuration(song.duration, useHoursFormat)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                // Enhanced controls with better visual hierarchy and spacing
                Row(
                    horizontalArrangement = spacedBy(10.dp), // Increased spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dynamic shape play/pause button - rounded square for pause, circle for play
                    FilledIconButton(
                        onClick = {
                            // Enhanced haptic feedback for primary action - respecting settings
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                            onPlayPause()
                        },
                        modifier = Modifier.size(44.dp), // Slightly smaller for better proportion
                        shape = if (isPlaying) RoundedCornerShape(18.dp) else CircleShape, // Dynamic shape based on state
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) RhythmIcons.Pause else RhythmIcons.Play,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Enhanced next track button with better styling
                    FilledTonalIconButton(
                        onClick = {
                            // Strong haptic feedback for next track - respecting settings
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            onSkipNext()
                        },
                        modifier = Modifier.size(36.dp), // Smaller secondary button
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.SkipNext,
                            contentDescription = "Next track",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format duration from milliseconds to mm:ss or h:mm:ss format
 * @param durationMs Duration in milliseconds
 * @param useHoursFormat If true, shows hours when duration is >= 60 minutes (e.g., 1:32:26 instead of 92:26)
 */
fun formatDuration(durationMs: Long, useHoursFormat: Boolean = false): String {
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) -
            TimeUnit.MINUTES.toSeconds(totalMinutes)
    
    return if (useHoursFormat && totalMinutes >= 60) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", totalMinutes, seconds)
    }
}
