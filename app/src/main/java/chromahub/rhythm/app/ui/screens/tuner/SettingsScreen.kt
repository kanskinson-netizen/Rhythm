package chromahub.rhythm.app.ui.screens.tuner

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import chromahub.rhythm.app.R
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.utils.LazyListStateSaver
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.ui.components.LanguageSwitcherDialog
import android.content.Context
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import chromahub.rhythm.app.ui.theme.RhythmTheme
import chromahub.rhythm.app.util.HapticUtils

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
    const val PLAYER_CUSTOMIZATION = "player_customization_settings"
    const val EQUALIZER = "equalizer_settings"
    const val SLEEP_TIMER = "sleep_timer_settings"
    const val CRASH_LOG_HISTORY = "crash_log_history_settings"
    const val QUEUE_PLAYBACK = "queue_playback_settings"
    const val LYRICS_SOURCE = "lyrics_source_settings"
    const val WIDGET = "widget_settings"
}

data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val description: String? = null,
    val onClick: (() -> Unit)? = null,
    val toggleState: Boolean? = null,
    val onToggleChange: ((Boolean) -> Unit)? = null,
    val data: Any? = null
)

data class SettingGroup(
    val title: String,
    val items: List<SettingItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateTo: (String) -> Unit, // Add navigation callback
    scrollState: LazyListState? = null // Optional scroll state parameter
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
    val defaultScreen by appSettings.defaultScreen.collectAsState()
    
    var showDefaultScreenDialog by remember { mutableStateOf(false) }
    var showLyricsSourceDialog by remember { mutableStateOf(false) }
    var showLanguageSwitcher by remember { mutableStateOf(false) }

    CollapsibleHeaderScreen(
        title = "Settings",
        showBackButton = true,
        onBackClick = {
            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.LongPress)
            onBackClick()
        }
    ) { modifier ->
        val settingGroups = listOf(
            SettingGroup(
                title = "Appearance",
                items = listOf(
                    SettingItem(Icons.Default.Palette, "Theme Customization", "Customize colors, fonts, and appearance", onClick = { onNavigateTo(SettingsRoutes.THEME_CUSTOMIZATION) }),
                    SettingItem(Icons.Default.Widgets, "Widget Customization", "Customize home screen widgets", onClick = { onNavigateTo(SettingsRoutes.WIDGET) }),
                    SettingItem(Icons.Default.MusicNote, "Player Customization", "Customize player screen chips and layout", onClick = { onNavigateTo(SettingsRoutes.PLAYER_CUSTOMIZATION) }),
                    // SettingItem(Icons.Default.Reorder, "Library Tab Order", "Reorder tabs in the library", onClick = { onNavigateTo(SettingsRoutes.LIBRARY_TAB_ORDER) })
                )
            ),
            SettingGroup(
                title = "User Interface",
                items = listOf(
                    SettingItem(
                        Icons.Default.Home,
                        "Default Landing Screen",
                        if (defaultScreen == "library") "Library" else "Home",
                        onClick = { showDefaultScreenDialog = true }
                    ),
                    SettingItem(
                        Icons.Default.Public,
                        "Language",
                        "Change app language",
                        onClick = { showLanguageSwitcher = true }
                    ),
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
                    // SettingItem(
                    //     Icons.Default.Lyrics, 
                    //     "Show Lyrics", 
                    //     "Display lyrics when available", 
                    //     toggleState = showLyrics,
                    //     onToggleChange = { appSettings.setShowLyrics(it) }
                    // ),
                    SettingItem(Icons.Default.Lyrics, "Lyrics Source Priority", "Configure embedded vs online lyrics", onClick = { 
                        HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                        showLyricsSourceDialog = true 
                    }),
                    SettingItem(Icons.Default.QueueMusic, "Queue & Playback", "Configure queue and playback behavior", onClick = { onNavigateTo(SettingsRoutes.QUEUE_PLAYBACK) }),
                    SettingItem(Icons.Default.GraphicEq, "Equalizer", "Adjust audio frequencies and effects", onClick = { onNavigateTo(SettingsRoutes.EQUALIZER) }),
                    // SettingItem(Icons.Default.AccessTime, "Sleep Timer", "Auto-stop playback after set time", onClick = { onNavigateTo(SettingsRoutes.SLEEP_TIMER) })
                )
            ),
            SettingGroup(
                title = "Library & Content",
                items = listOf(
                    // SettingItem(
                    //     Icons.Default.Person, 
                    //     "Group by Album Artist", 
                    //     "Show collaboration albums under main artist", 
                    //     toggleState = groupByAlbumArtist,
                    //     onToggleChange = { appSettings.setGroupByAlbumArtist(it) }
                    // ),
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
                    SettingItem(Icons.Default.BugReport, "Crash Log History", "View and manage crash reports", onClick = { onNavigateTo(SettingsRoutes.CRASH_LOG_HISTORY) }),
                    SettingItem(Icons.Default.Science, "Experimental Features", "Try out cutting-edge features", onClick = { onNavigateTo(SettingsRoutes.EXPERIMENTAL_FEATURES) })
                )
            )
        )

        val lazyListState = scrollState ?: rememberSaveable(
            key = "settings_scroll_state",
            saver = LazyListStateSaver
        ) {
            LazyListState()
        }
        
        LazyColumn(
            state = lazyListState,
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Ensure background color for the scrollable content
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups, key = { "setting_${it.title}" }) { group ->
                Spacer(modifier = Modifier.height(24.dp)) // Increased space between groups
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), // Smaller title for group
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
                            }

            // Quick Tips Card
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = context.getString(R.string.settings_quick_tips),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        TipItem(
                            icon = Icons.Default.Palette,
                            text = context.getString(R.string.settings_tip_theme)
                        )
                        TipItem(
                            icon = Icons.Default.TouchApp,
                            text = context.getString(R.string.settings_tip_haptic)
                        )
                        TipItem(
                            icon = Icons.Default.Folder,
                            text = "Use media scan to hide unwanted files from your library"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
//                Spacer(modifier = Modifier.height(24.dp)) // Space at the bottom
            }
        }
        
        // Default screen selection bottom sheet
        if (showDefaultScreenDialog) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            
            ModalBottomSheet(
                onDismissRequest = { showDefaultScreenDialog = false },
                sheetState = sheetState,
                dragHandle = { 
                    BottomSheetDefaults.DragHandle(
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = context.getString(R.string.settings_default_screen),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = CircleShape
                                    )
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    text = context.getString(R.string.settings_default_screen_desc),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Home option
                    Card(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                            appSettings.setDefaultScreen("home")
                            showDefaultScreenDialog = false
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (defaultScreen == "home") 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = if (defaultScreen == "home") 
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.common_home),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (defaultScreen == "home") 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = context.getString(R.string.settings_home_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (defaultScreen == "home") 
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (defaultScreen == "home") {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    // Library option
                    Card(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                            appSettings.setDefaultScreen("library")
                            showDefaultScreenDialog = false
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (defaultScreen == "library") 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = chromahub.rhythm.app.ui.components.RhythmIcons.Library,
                                contentDescription = null,
                                tint = if (defaultScreen == "library")
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.common_library),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (defaultScreen == "library") 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = context.getString(R.string.settings_library_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (defaultScreen == "library") 
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (defaultScreen == "library") {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Lyrics source priority dialog
        if (showLyricsSourceDialog) {
            LyricsSourceDialog(
                onDismiss = { showLyricsSourceDialog = false },
                appSettings = appSettings,
                context = context,
                haptic = hapticFeedback
            )
        }
        
        // Language switcher dialog
        if (showLanguageSwitcher) {
            LanguageSwitcherDialog(
                onDismiss = { showLanguageSwitcher = false }
            )
        }
    }
}

@Composable
fun SettingRow(item: SettingItem) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
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
                    if (item.onClick != null && item.toggleState == null) {
                        Modifier.clickable(onClick = {
                            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.LongPress)
                            item.onClick()
                        })
                    } else if (item.onClick != null && item.toggleState != null) {
                        Modifier.clickable(onClick = {
                            HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.LongPress)
                            item.onClick()
                        })
                    } else {
                        Modifier
                    }
                )
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (item.toggleState != null && item.onClick != null) {
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
                onCheckedChange = {
                    HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                    item.onToggleChange?.invoke(it)
                },
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
                onCheckedChange = {
                    HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.TextHandleMove)
                    item.onToggleChange?.invoke(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )
        } else if (item.onClick != null) {
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
fun SettingsScreenPreview() {
    RhythmTheme {
        SettingsScreen(onBackClick = {}, onNavigateTo = {})
    }
}

// Wrapper function for navigation
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsScreenWrapper(onBack: () -> Unit, appSettings: chromahub.rhythm.app.data.AppSettings) {
    var currentRoute by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Hoist the main settings scroll state to persist across navigation
    val mainSettingsScrollState = rememberSaveable(
        key = "main_settings_scroll_state",
        saver = LazyListStateSaver
    ) {
        LazyListState()
    }
    
    // Handle back navigation - if we're in a subsettings screen, go back to main screen
    val handleBack = {
        if (currentRoute != null) {
            currentRoute = null
        } else {
            onBack()
        }
    }
    
    // Handle system back gestures when in subsettings
    BackHandler(enabled = currentRoute != null) {
        handleBack()
    }
    
    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            if (targetState != null) {
                // Enhanced slide in from right when navigating to a screen
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = androidx.compose.animation.core.EaseOutCubic
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 350,
                        delayMillis = 50
                    )
                ) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = androidx.compose.animation.core.EaseOutCubic
                    )
                ) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = androidx.compose.animation.core.EaseInCubic
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 250)
                ) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = androidx.compose.animation.core.EaseInCubic
                    )
                )
            } else {
                // Enhanced slide in from left when going back
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = androidx.compose.animation.core.EaseOutCubic
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 350,
                        delayMillis = 50
                    )
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = androidx.compose.animation.core.EaseOutCubic
                    )
                ) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = androidx.compose.animation.core.EaseInCubic
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 250)
                ) + scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = androidx.compose.animation.core.EaseInCubic
                    )
                )
            }
        },
        label = "settings_navigation",
        contentKey = { it ?: "main_settings" }
    ) { route ->
        when (route) {
            SettingsRoutes.NOTIFICATIONS -> NotificationsSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.PLAYLISTS -> PlaylistsSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.MEDIA_SCAN -> MediaScanSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.ABOUT -> chromahub.rhythm.app.ui.screens.tuner.AboutScreen(
                onBackClick = { currentRoute = null },
                onNavigateToUpdates = { currentRoute = SettingsRoutes.UPDATES }
            )
            SettingsRoutes.UPDATES -> UpdatesSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.EXPERIMENTAL_FEATURES -> ExperimentalFeaturesScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.API_MANAGEMENT -> ApiManagementSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.CACHE_MANAGEMENT -> CacheManagementSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.BACKUP_RESTORE -> BackupRestoreSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.LIBRARY_TAB_ORDER -> LibraryTabOrderSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.THEME_CUSTOMIZATION -> ThemeCustomizationSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.PLAYER_CUSTOMIZATION -> PlayerCustomizationSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.EQUALIZER -> EqualizerSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.SLEEP_TIMER -> SleepTimerSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.CRASH_LOG_HISTORY -> CrashLogHistorySettingsScreen(onBackClick = { currentRoute = null }, appSettings = appSettings)
            SettingsRoutes.QUEUE_PLAYBACK -> QueuePlaybackSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.LYRICS_SOURCE -> LyricsSourceSettingsScreen(onBackClick = { currentRoute = null })
            SettingsRoutes.WIDGET -> WidgetSettingsScreen(onBackClick = { currentRoute = null })
            else -> SettingsScreen(
                onBackClick = handleBack,
                onNavigateTo = { route -> currentRoute = route },
                scrollState = mainSettingsScrollState
            )
        }
    }
}

@Composable
private fun TipItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

