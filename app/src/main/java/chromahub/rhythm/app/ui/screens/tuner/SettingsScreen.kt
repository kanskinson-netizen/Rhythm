package chromahub.rhythm.app.ui.screens.tuner

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Science
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
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import chromahub.rhythm.app.ui.theme.RhythmTheme

// Define routes for navigation
object SettingsRoutes {
    const val NOTIFICATIONS = "notifications_settings"
    const val THEMING = "theming_settings"
    const val EXPERIMENTAL_FEATURES = "experimental_features_settings"
    const val ABOUT = "about_screen"
    const val UPDATES = "updates_screen"
    const val STREAMING = "streaming_settings"
    const val AUDIO = "audio_settings"
    const val DOWNLOADS = "downloads_settings"
    const val OFFLINE_MODE = "offline_mode_settings"
    const val MEDIA_SCAN = "media_scan_settings"
    const val PLAYLISTS = "playlist_settings"
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
    var isOfflineModeEnabled by remember { mutableStateOf(false) } // State for offline mode toggle
    var isUpdatesEnabled by remember { mutableStateOf(true) } // State for updates toggle

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
                    SettingItem(Icons.Default.Palette, "Theme", "Customize app theme and appearance", onClick = { /* TODO: Add theme settings */ })
                )
            ),
            SettingGroup(
                title = "Notifications & Alerts",
                items = listOf(
                    SettingItem(Icons.Default.Notifications, "Notifications", "Manage notification preferences", onClick = { onNavigateTo(SettingsRoutes.NOTIFICATIONS) })
                )
            ),
            SettingGroup(
                title = "Audio & Playback",
                items = buildList {
                    if (!isOfflineModeEnabled) {
                        add(SettingItem(Icons.Default.GraphicEq, "Streaming", "Audio quality and streaming settings", onClick = { onNavigateTo(SettingsRoutes.STREAMING) }))
                    }
                    add(SettingItem(Icons.Default.Equalizer, "Audio", "Audio effects, queue, and playback settings", onClick = { onNavigateTo(SettingsRoutes.AUDIO) }))
                }
            ),
            SettingGroup(
                title = "Library & Storage",
                items = buildList {
                    add(SettingItem(Icons.Default.Folder, "Media Scan", "Manage blacklist and media scanning", onClick = { onNavigateTo(SettingsRoutes.MEDIA_SCAN) }))
                    add(SettingItem(Icons.AutoMirrored.Filled.QueueMusic, "Playlists", "Manage your playlists", onClick = { onNavigateTo(SettingsRoutes.PLAYLISTS) }))
                    add(SettingItem(Icons.Default.Download, "Storage & Cache", "Manage cache and storage", onClick = { onNavigateTo(SettingsRoutes.DOWNLOADS) }))
                    add(
                        SettingItem(
                            Icons.Default.CloudOff,
                            "Offline Mode",
                            "Enable or disable offline playback",
                            onClick = { onNavigateTo(SettingsRoutes.OFFLINE_MODE) },
                            toggleState = isOfflineModeEnabled,
                            onToggleChange = { newValue ->
                                isOfflineModeEnabled = newValue
                                // TODO: Implement offline mode toggle
                            }
                        )
                    )
                }
            ),
            SettingGroup(
                title = "Updates & Info",
                items = listOf(
                    SettingItem(
                        Icons.Default.Update,
                        "Updates",
                        "Manage app updates and auto-check",
                        onClick = { onNavigateTo(SettingsRoutes.UPDATES) },
                        toggleState = isUpdatesEnabled,
                        onToggleChange = { newValue -> isUpdatesEnabled = newValue }
                    ),
                    SettingItem(Icons.Default.Info, "About", "Tuner Beta version info", onClick = { onNavigateTo(SettingsRoutes.ABOUT) })
                )
            ),
            SettingGroup(
                title = "Advanced",
                items = listOf(
                    SettingItem(Icons.Default.Science, "Experimental Features", "Beta functionality and haptic feedback", onClick = { onNavigateTo(SettingsRoutes.EXPERIMENTAL_FEATURES) })
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
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var currentRoute by remember { mutableStateOf<String?>(null) }
    
    when (currentRoute) {
        SettingsRoutes.NOTIFICATIONS -> NotificationsSettingsScreen(onBackClick = { currentRoute = null })
//        SettingsRoutes.THEMING -> ThemingSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.STREAMING -> StreamingSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.AUDIO -> AudioSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.DOWNLOADS -> DownloadsSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.OFFLINE_MODE -> OfflineModeSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.PLAYLISTS -> PlaylistsSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.MEDIA_SCAN -> MediaScanSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.ABOUT -> AboutScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.UPDATES -> UpdatesSettingsScreen(onBackClick = { currentRoute = null })
        SettingsRoutes.EXPERIMENTAL_FEATURES -> ExperimentalFeaturesScreen(onBackClick = { currentRoute = null })
        else -> TunerSettingsScreen(
            onBackClick = onBack,
            onNavigateTo = { route -> currentRoute = route }
        )
    }
}
