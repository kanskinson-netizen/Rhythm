package chromahub.rhythm.app.ui.screens.tuner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.RhythmIcons

@Composable
fun PlaceholderScreen(
    title: String,
    description: String = "This feature is under development.",
    onBackClick: () -> Unit
) {
    CollapsibleHeaderScreen(
        title = title,
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Coming soon...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Individual screens with actual settings
@Composable
fun NotificationsSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val useCustomNotification by appSettings.useCustomNotification.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Notifications",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                TunerSettingCard(
                    title = "Custom Notifications",
                    description = "Use app's custom notification style instead of system media notification",
                    icon = Icons.Default.Notifications,
                    checked = useCustomNotification,
                    onCheckedChange = { appSettings.setUseCustomNotification(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun StreamingSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val highQualityAudio by appSettings.highQualityAudio.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Streaming",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                TunerSettingCard(
                    title = "High Quality Audio",
                    description = "Enable higher bitrate audio streaming and playback",
                    icon = Icons.Default.HighQuality,
                    checked = highQualityAudio,
                    onCheckedChange = { appSettings.setHighQualityAudio(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AudioSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val useSystemVolume by appSettings.useSystemVolume.collectAsState()
    val gaplessPlayback by appSettings.gaplessPlayback.collectAsState()
    val crossfade by appSettings.crossfade.collectAsState()
    val audioNormalization by appSettings.audioNormalization.collectAsState()
    val replayGain by appSettings.replayGain.collectAsState()
    val shuffleUsesExoplayer by appSettings.shuffleUsesExoplayer.collectAsState()
    val autoAddToQueue by appSettings.autoAddToQueue.collectAsState()
    val clearQueueOnNewSong by appSettings.clearQueueOnNewSong.collectAsState()
    val repeatModePersistence by appSettings.repeatModePersistence.collectAsState()
    val shuffleModePersistence by appSettings.shuffleModePersistence.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Audio & Playback",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Audio Quality Section
            item {
                Text(
                    text = "Audio Quality",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            
            item {
                TunerSettingCard(
                    title = "System Volume Control",
                    description = "Use device volume controls for music playback",
                    icon = RhythmIcons.Player.VolumeUp,
                    checked = useSystemVolume,
                    onCheckedChange = { appSettings.setUseSystemVolume(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Gapless Playback",
                    description = "Eliminate gaps between tracks for continuous listening",
                    icon = Icons.Default.QueueMusic,
                    checked = gaplessPlayback,
                    onCheckedChange = { appSettings.setGaplessPlayback(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Crossfade",
                    description = "Smoothly transition between songs",
                    icon = Icons.Default.Shuffle,
                    checked = crossfade,
                    onCheckedChange = { appSettings.setCrossfade(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Audio Normalization",
                    description = "Adjust volume levels to consistent loudness",
                    icon = Icons.Rounded.Tune,
                    checked = audioNormalization,
                    onCheckedChange = { appSettings.setAudioNormalization(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "ReplayGain",
                    description = "Apply ReplayGain tags for consistent playback volume",
                    icon = Icons.Default.GraphicEq,
                    checked = replayGain,
                    onCheckedChange = { appSettings.setReplayGain(it) }
                )
            }
            
            // Queue & Playback Behavior Section
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                Text(
                    text = "Queue & Playback Behavior",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Use ExoPlayer Shuffle",
                    description = "Let the media player handle shuffle (recommended: OFF)",
                    icon = RhythmIcons.Shuffle,
                    checked = shuffleUsesExoplayer,
                    onCheckedChange = { appSettings.setShuffleUsesExoplayer(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Auto Queue",
                    description = "Automatically add related songs to queue when playing",
                    icon = RhythmIcons.Queue,
                    checked = autoAddToQueue,
                    onCheckedChange = { appSettings.setAutoAddToQueue(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Clear Queue on New Song",
                    description = "Clear the current queue when playing a new song directly",
                    icon = RhythmIcons.Delete,
                    checked = clearQueueOnNewSong,
                    onCheckedChange = { appSettings.setClearQueueOnNewSong(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Remember Repeat Mode",
                    description = "Save repeat mode (Off/All/One) between app restarts",
                    icon = RhythmIcons.Repeat,
                    checked = repeatModePersistence,
                    onCheckedChange = { appSettings.setRepeatModePersistence(it) }
                )
            }
            
            item {
                TunerSettingCard(
                    title = "Remember Shuffle Mode",
                    description = "Save shuffle on/off state between app restarts",
                    icon = RhythmIcons.Shuffle,
                    checked = shuffleModePersistence,
                    onCheckedChange = { appSettings.setShuffleModePersistence(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun DownloadsSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val maxCacheSize by appSettings.maxCacheSize.collectAsState()
    val clearCacheOnExit by appSettings.clearCacheOnExit.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Storage & Cache",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Cache Size",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${String.format("%.1f", maxCacheSize / (1024f * 1024f))} MB maximum",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                TunerSettingCard(
                    title = "Auto Clear Cache",
                    description = "Automatically clear cache when exiting the app",
                    icon = Icons.Default.DeleteSweep,
                    checked = clearCacheOnExit,
                    onCheckedChange = { appSettings.setClearCacheOnExit(it) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun OfflineModeSettingsScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Offline Mode",
        description = "Configure offline playback and sync settings.",
        onBackClick = onBackClick
    )
}

@Composable
fun PlaylistsSettingsScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = "Playlists",
        description = "Manage playlist preferences and behavior.\n\nThis would integrate with the main Settings screen's Playlist Management feature.",
        onBackClick = onBackClick
    )
}

@Composable
fun MediaScanSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val blacklistedSongs by appSettings.blacklistedSongs.collectAsState()
    val blacklistedFolders by appSettings.blacklistedFolders.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Media Scan",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Blacklisted Items",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${blacklistedSongs.size} songs, ${blacklistedFolders.size} folders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use the main Settings screen to manage blacklisted media and folders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    CollapsibleHeaderScreen(
        title = "About",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Rhythm",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rhythm Music Player",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Tuner Beta",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "New modern settings interface\nwith improved organization and design",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun UpdatesSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val updatesEnabled by appSettings.updatesEnabled.collectAsState()
    val autoCheckForUpdates by appSettings.autoCheckForUpdates.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Updates",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                TunerSettingCard(
                    title = "Enable Updates",
                    description = "Allow the app to check for and download updates",
                    icon = Icons.Default.SystemUpdate,
                    checked = updatesEnabled,
                    onCheckedChange = { appSettings.setUpdatesEnabled(it) }
                )
            }
            
            if (updatesEnabled) {
                item {
                    TunerSettingCard(
                        title = "Periodic Check",
                        description = "Automatically check for updates from Rhythm's GitHub repo",
                        icon = Icons.Default.Update,
                        checked = autoCheckForUpdates,
                        onCheckedChange = { appSettings.setAutoCheckForUpdates(it) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun ExperimentalFeaturesScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
    
    CollapsibleHeaderScreen(
        title = "Experimental Features",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                TunerSettingCard(
                    title = "Haptic Feedback",
                    description = "Vibrate when tapping buttons and interacting with the interface",
                    icon = Icons.Default.Vibration,
                    checked = hapticFeedbackEnabled,
                    onCheckedChange = { appSettings.setHapticFeedbackEnabled(it) }
                )
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "More experimental features coming soon in future updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// Reusable setting card component
@Composable
fun TunerSettingCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (checked != null && onCheckedChange != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

