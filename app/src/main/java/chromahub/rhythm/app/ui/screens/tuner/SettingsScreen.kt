package chromahub.rhythm.app.ui.screens.tuner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.data.AppSettings
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import chromahub.rhythm.app.ui.theme.RhythmTheme

// Define routes for navigation
object SettingsRoutes {
    const val NOTIFICATIONS = "notifications_settings"
    const val EXPERIMENTAL_FEATURES = "experimental_features_settings"
    const val ABOUT = "about_screen"
    const val UPDATES = "updates_screen"
    const val MEDIA_SCAN = "media_scan_settings"
    const val PLAYLISTS = "playlist_settings"
    const val API_MANAGEMENT = "api_management_settings"
    const val CACHE_MANAGEMENT = "cache_management_settings"
    const val BACKUP_RESTORE = "backup_restore_settings"
    const val LIBRARY_TAB_ORDER = "library_tab_order_settings"
    const val THEME_CUSTOMIZATION = "theme_customization_settings"
    const val EQUALIZER = "equalizer_settings"
    const val SLEEP_TIMER = "sleep_timer_settings"
    const val CRASH_LOG_HISTORY = "crash_log_history_settings"
    const val QUEUE_PLAYBACK = "queue_playback_settings"
}

data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit = {},
    val toggleState: Boolean? = null,
    val onToggleChange: ((Boolean) -> Unit)? = null
)

data class SettingGroup(
    val title: String,
    val items: List<SettingItem>
)

@Composable
fun TunerSettingsScreen(
    onBackClick: () -> Unit,
    onNavigateTo: (String) -> Unit // Add navigation callback
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    
    // Collect states for toggles
    val updatesEnabled by appSettings.updatesEnabled.collectAsState()
    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
    val useSystemVolume by appSettings.useSystemVolume.collectAsState()
    val showLyrics by appSettings.showLyrics.collectAsState()
    val groupByAlbumArtist by appSettings.groupByAlbumArtist.collectAsState()

    CollapsibleHeaderScreen(
        title = "Tuner",
        showBackButton = true,
        onBackClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackClick()
        }
    ) { modifier ->
        val settingGroups = listOf(
            SettingGroup(
                title = "Appearance",
                items = listOf(
                    SettingItem(Icons.Default.Palette, "Theme Customization", "Customize colors, fonts, and appearance", onClick = { onNavigateTo(SettingsRoutes.THEME_CUSTOMIZATION) }),
                    SettingItem(Icons.Default.Reorder, "Library Tab Order", "Reorder tabs in the library", onClick = { onNavigateTo(SettingsRoutes.LIBRARY_TAB_ORDER) })
                )
            ),
            SettingGroup(
                title = "User Interface",
                items = listOf(
                    SettingItem(
                        Icons.Default.TouchApp, 
                        "Haptic Feedback", 
                        "Vibrate when tapping buttons", 
                        toggleState = hapticFeedbackEnabled,
                        onToggleChange = { appSettings.setHapticFeedbackEnabled(it) }
                    )
                )
            ),
            SettingGroup(
                title = "Audio & Playback",
                items = listOf(
                    SettingItem(
                        RhythmIcons.Player.VolumeUp, 
                        "System Volume", 
                        "Use device volume for playback", 
                        toggleState = useSystemVolume,
                        onToggleChange = { appSettings.setUseSystemVolume(it) }
                    ),
                    SettingItem(
                        Icons.Default.Lyrics, 
                        "Show Lyrics", 
                        "Display lyrics when available", 
                        toggleState = showLyrics,
                        onToggleChange = { appSettings.setShowLyrics(it) }
                    ),
                    SettingItem(Icons.Default.QueueMusic, "Queue & Playback", "Configure queue and playback behavior", onClick = { onNavigateTo(SettingsRoutes.QUEUE_PLAYBACK) }),
                    SettingItem(Icons.Default.GraphicEq, "Equalizer", "Adjust audio frequencies and effects", onClick = { onNavigateTo(SettingsRoutes.EQUALIZER) }),
                    SettingItem(Icons.Default.AccessTime, "Sleep Timer", "Auto-stop playback after set time", onClick = { onNavigateTo(SettingsRoutes.SLEEP_TIMER) })
                )
            ),
            SettingGroup(
                title = "Library & Content",
                items = listOf(
                    SettingItem(
                        Icons.Default.Person, 
                        "Group by Album Artist", 
                        "Show collaboration albums under main artist", 
                        toggleState = groupByAlbumArtist,
                        onToggleChange = { appSettings.setGroupByAlbumArtist(it) }
                    ),
                    SettingItem(Icons.Default.Folder, "Media Scan", "Manage blacklist and media scanning", onClick = { onNavigateTo(SettingsRoutes.MEDIA_SCAN) }),
                    SettingItem(Icons.AutoMirrored.Filled.QueueMusic, "Playlists", "Manage your playlists", onClick = { onNavigateTo(SettingsRoutes.PLAYLISTS) })
                )
            ),
            SettingGroup(
                title = "Storage & Data",
                items = listOf(
                    SettingItem(Icons.Default.Storage, "Cache Management", "Control cache size and clearing", onClick = { onNavigateTo(SettingsRoutes.CACHE_MANAGEMENT) }),
                    SettingItem(Icons.Default.Backup, "Backup & Restore", "Safeguard settings and playlists", onClick = { onNavigateTo(SettingsRoutes.BACKUP_RESTORE) })
                )
            ),
            SettingGroup(
                title = "Services",
                items = listOf(
                    SettingItem(Icons.Default.Api, "API Management", "Configure external API services", onClick = { onNavigateTo(SettingsRoutes.API_MANAGEMENT) })
                )
            ),
            SettingGroup(
                title = "Updates & Info",
                items = listOf(
                    SettingItem(
                        Icons.Default.Update,
                        "Updates",
                        "Check for app updates",
                        toggleState = updatesEnabled,
                        onToggleChange = { appSettings.setUpdatesEnabled(it) },
                        onClick = { onNavigateTo(SettingsRoutes.UPDATES) }
                    ),
                    SettingItem(Icons.Default.Info, "About", "App version and info", onClick = { onNavigateTo(SettingsRoutes.ABOUT) })
                )
            ),
            SettingGroup(
                title = "Advanced",
                items = listOf(
                    SettingItem(Icons.Default.BugReport, "Crash Log History", "View and manage crash reports", onClick = { onNavigateTo(SettingsRoutes.CRASH_LOG_HISTORY) })
                )
            )
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Ensure background color for the scrollable content
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups) { group ->
                Spacer(modifier = Modifier.height(24.dp)) // Increased space between groups
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold), // Smaller title for group
                    color = MaterialTheme.colorScheme.primary, // Use primary color for group title
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp) // Indent group title
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), // Use surface for card background
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Add subtle elevation
                ) {
                    Column {
                        group.items.forEachIndexed { index, item ->
                            SettingRow(item = item)
                            if (index < group.items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
//                Spacer(modifier = Modifier.height(24.dp)) // Space at the bottom
            }
        }
    }
}

@Composable
fun SettingRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (item.onClick != {} && item.toggleState == null) {
                        Modifier.clickable(onClick = item.onClick)
                    } else if (item.onClick != {} && item.toggleState != null) {
                        Modifier.clickable(onClick = item.onClick)
                    } else {
                        Modifier
                    }
                )
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Default),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (item.toggleState != null && item.onClick != {}) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = item.toggleState,
                onCheckedChange = { item.onToggleChange?.invoke(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )
        } else if (item.toggleState != null) {
            Switch(
                checked = item.toggleState,
                onCheckedChange = { item.onToggleChange?.invoke(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )
        } else if (item.onClick != {}) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TunerSettingsScreenPreview() {
    RhythmTheme {
        TunerSettingsScreen(onBackClick = {}, onNavigateTo = {})
    }
}

// Wrapper function for navigation
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, appSettings: chromahub.rhythm.app.data.AppSettings) {
    var currentRoute by remember { mutableStateOf<String?>(null) }
    
    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            if (targetState != null) {
                // Slide in from right when navigating to a screen
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            } else {
                // Slide in from left when going back
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
        },
        label = "settings_navigation"
    ) { route ->
        when (route) {
            SettingsRoutes.NOTIFICATIONS -> NotificationsSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.PLAYLISTS -> PlaylistsSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.MEDIA_SCAN -> MediaScanSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.ABOUT -> AboutScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.UPDATES -> UpdatesSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.EXPERIMENTAL_FEATURES -> ExperimentalFeaturesScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.API_MANAGEMENT -> ApiManagementSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.CACHE_MANAGEMENT -> CacheManagementSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.BACKUP_RESTORE -> BackupRestoreSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.LIBRARY_TAB_ORDER -> LibraryTabOrderSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.THEME_CUSTOMIZATION -> ThemeCustomizationSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.EQUALIZER -> EqualizerSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.SLEEP_TIMER -> SleepTimerSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.CRASH_LOG_HISTORY -> CrashLogHistorySettingsScreen(onBackClick = { currentRoute = null }, appSettings = appSettings)
            SettingsRoutes.QUEUE_PLAYBACK -> QueuePlaybackSettingsScreen(onBackClick = { currentRoute = null })
            else -> TunerSettingsScreen(
                onBackClick = onBack,
                onNavigateTo = { route -> currentRoute = route }
            )
        }
    }
}
