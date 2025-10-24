package chromahub.rhythm.app.ui.screens.tuner

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.Slider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.data.AppSettings
import chromahub.rhythm.app.data.Playlist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.util.HapticUtils
import kotlin.system.exitProcess
import chromahub.rhythm.app.ui.components.CollapsibleHeaderScreen
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.StandardBottomSheetHeader
import chromahub.rhythm.app.viewmodel.MusicViewModel
import chromahub.rhythm.app.ui.theme.getFontPreviewStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import chromahub.rhythm.app.utils.FontLoader
import chromahub.rhythm.app.ui.theme.parseCustomColorScheme

@Composable
private fun TunerSettingRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.onClick != {} && item.toggleState == null) {
                    Modifier.clickable(onClick = item.onClick)
                } else {
                    Modifier
                }
            )
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
            modifier = Modifier.weight(1f)
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

        if (item.toggleState != null) {
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

@Composable
private fun TunerSettingCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && checked == null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Default),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (checked != null && onCheckedChange != null) {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Navigate",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        val settingGroups = listOf(
            SettingGroup(
                title = "Notification Style",
                items = listOf(
                    SettingItem(
                        Icons.Default.Notifications,
                        "Custom Notifications",
                        "Use app's custom notification style instead of system media notification",
                        toggleState = useCustomNotification,
                        onToggleChange = { appSettings.setUseCustomNotification(it) }
                    )
                )
            )
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups) { group ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        group.items.forEachIndexed { index, item ->
                            TunerSettingRow(item = item)
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
        }
    }
}

// StreamingSettingsScreen removed - not in original SettingsScreen
// AudioSettingsScreen removed - was inline settings in original SettingsScreen
// DownloadsSettingsScreen removed - not in original SettingsScreen
// OfflineModeSettingsScreen removed - not in original SettingsScreen

// Queue & Playback Settings Screen
@Composable
fun QueuePlaybackSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)

    val shuffleUsesExoplayer by appSettings.shuffleUsesExoplayer.collectAsState()
    val autoAddToQueue by appSettings.autoAddToQueue.collectAsState()
    val clearQueueOnNewSong by appSettings.clearQueueOnNewSong.collectAsState()
    val repeatModePersistence by appSettings.repeatModePersistence.collectAsState()
    val shuffleModePersistence by appSettings.shuffleModePersistence.collectAsState()

    CollapsibleHeaderScreen(
        title = "Queue & Playback",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        val settingGroups = listOf(
            SettingGroup(
                title = "Queue Behavior",
                items = listOf(
                    SettingItem(
                        RhythmIcons.Shuffle,
                        "Use ExoPlayer Shuffle",
                        "Let the media player handle shuffle (recommended: OFF for manual shuffle)",
                        toggleState = shuffleUsesExoplayer,
                        onToggleChange = { appSettings.setShuffleUsesExoplayer(it) }
                    ),
                    SettingItem(
                        RhythmIcons.Queue,
                        "Auto Queue",
                        "Automatically add related songs to queue when playing",
                        toggleState = autoAddToQueue,
                        onToggleChange = { appSettings.setAutoAddToQueue(it) }
                    ),
                    SettingItem(
                        RhythmIcons.Delete,
                        "Clear Queue on New Song",
                        "Clear the current queue when playing a new song directly",
                        toggleState = clearQueueOnNewSong,
                        onToggleChange = { appSettings.setClearQueueOnNewSong(it) }
                    )
                )
            ),
            SettingGroup(
                title = "Playback Persistence",
                items = listOf(
                    SettingItem(
                        RhythmIcons.Repeat,
                        "Remember Repeat Mode",
                        "Save repeat mode (Off/All/One) between app restarts",
                        toggleState = repeatModePersistence,
                        onToggleChange = { appSettings.setRepeatModePersistence(it) }
                    ),
                    SettingItem(
                        RhythmIcons.Shuffle,
                        "Remember Shuffle Mode",
                        "Save shuffle on/off state between app restarts",
                        toggleState = shuffleModePersistence,
                        onToggleChange = { appSettings.setShuffleModePersistence(it) }
                    )
                )
            )
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups) { group ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        group.items.forEachIndexed { index, item ->
                            TunerSettingRow(item = item)
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
        }
    }
}

// ✅ FULLY MERGED Playlists Screen (simplified playlist management)
@Composable
fun PlaylistsSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val musicViewModel: MusicViewModel = viewModel()
    val playlists by musicViewModel.playlists.collectAsState()

    val defaultPlaylists = playlists.filter { it.isDefault }
    val userPlaylists = playlists.filter { !it.isDefault }
    val emptyPlaylists = playlists.filter { !it.isDefault && it.songs.isEmpty() }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var showBulkExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showOperationProgress by remember { mutableStateOf(false) }
    var operationProgressText by remember { mutableStateOf("") }
    var showCleanupConfirmDialog by remember { mutableStateOf(false) }

    CollapsibleHeaderScreen(
        title = "Playlists",
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

            // Statistics Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Your Collection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${playlists.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text("Total", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${userPlaylists.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text("Custom", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${defaultPlaylists.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text("Default", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            // Management Actions
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Manage Playlists",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        TunerSettingCard(
                            icon = Icons.Rounded.AddCircle,
                            title = "Create New Playlist",
                            description = "Add a new custom playlist",
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                showCreatePlaylistDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TunerSettingCard(
                            icon = Icons.Rounded.Upload,
                            title = "Import Playlists",
                            description = "Import from JSON, M3U, or PLS files",
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                showImportDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TunerSettingCard(
                            icon = Icons.Rounded.Download,
                            title = "Export All Playlists",
                            description = "Backup all playlists to file",
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                showBulkExportDialog = true
                            }
                        )

                        if (emptyPlaylists.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TunerSettingCard(
                                icon = Icons.Rounded.Delete,
                                title = "Cleanup Empty Playlists",
                                description = "Remove ${emptyPlaylists.size} empty playlist${if (emptyPlaylists.size > 1) "s" else ""}",
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    showCleanupConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Default Playlists
            if (defaultPlaylists.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Default Playlists",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                defaultPlaylists.forEach { playlist ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = playlist.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${playlist.songs.size} songs",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // User Playlists
            if (userPlaylists.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "My Playlists",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                userPlaylists.forEach { playlist ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QueueMusic,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = playlist.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${playlist.songs.size} songs",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    HapticUtils.performHapticFeedback(
                                                        context,
                                                        haptic,
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    playlistToDelete = playlist
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    // Dialogs
    if (showCreatePlaylistDialog) {
        chromahub.rhythm.app.ui.components.CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                musicViewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }

    if (showBulkExportDialog) {
        chromahub.rhythm.app.ui.components.BulkPlaylistExportDialog(
            playlistCount = playlists.size,
            onDismiss = { showBulkExportDialog = false },
            onExport = { format, includeDefault ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = "Exporting playlists..."
                musicViewModel.exportAllPlaylists(format, includeDefault) { result ->
                    showOperationProgress = false
                }
            },
            onExportToCustomLocation = { format, includeDefault, directoryUri ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = "Exporting playlists to selected location..."
                musicViewModel.exportAllPlaylists(format, includeDefault, directoryUri) { result ->
                    showOperationProgress = false
                }
            }
        )
    }

    if (showImportDialog) {
        chromahub.rhythm.app.ui.components.PlaylistImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { uri, onResult, onRestartRequired ->
                showImportDialog = false
                showOperationProgress = true
                operationProgressText = "Importing playlist..."
                musicViewModel.importPlaylist(uri, onResult, onRestartRequired)
            }
        )
    }

    if (showOperationProgress) {
        chromahub.rhythm.app.ui.components.PlaylistOperationProgressDialog(
            operation = operationProgressText,
            onDismiss = { /* Cannot dismiss during operation */ }
        )
    }

    if (showCleanupConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCleanupConfirmDialog = false },
            title = { Text("Cleanup Empty Playlists?") },
            text = {
                Text("This will permanently delete ${emptyPlaylists.size} empty playlist${if (emptyPlaylists.size > 1) "s" else ""}. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        emptyPlaylists.forEach { playlist ->
                            musicViewModel.deletePlaylist(playlist.id)
                        }
                        showCleanupConfirmDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanupConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Playlist?") },
            text = { Text("Are you sure you want to delete \"${playlist.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        musicViewModel.deletePlaylist(playlist.id)
                        playlistToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ✅ FULLY MERGED Media Scan Screen (blacklist/whitelist management)
@Composable
fun MediaScanSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val appSettings = AppSettings.getInstance(context)
    val musicViewModel: MusicViewModel = viewModel()
    
    // Get all songs and filtered items
    val allSongs by musicViewModel.songs.collectAsState()
    val filteredSongs by musicViewModel.filteredSongs.collectAsState()
    val blacklistedSongs by appSettings.blacklistedSongs.collectAsState()
    val blacklistedFolders by appSettings.blacklistedFolders.collectAsState()
    val whitelistedSongs by appSettings.whitelistedSongs.collectAsState()
    val whitelistedFolders by appSettings.whitelistedFolders.collectAsState()
    
    // Get current media scan mode from settings
    val mediaScanMode by appSettings.mediaScanMode.collectAsState()
    
    // Mode state
    var currentMode by remember { 
        mutableStateOf(
            if (mediaScanMode == "whitelist") chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST 
            else chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST
        ) 
    }
    
    // View state
    var currentView by remember { mutableStateOf("overview") } // "overview", "songs", "folders"
    
    // Computed values OUTSIDE LazyColumn
    val filteredSongDetails = remember(allSongs, blacklistedSongs, whitelistedSongs, currentMode) {
        when (currentMode) {
            chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST -> 
                allSongs.filter { song -> blacklistedSongs.contains(song.id) }
            chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST -> 
                allSongs.filter { song -> whitelistedSongs.contains(song.id) }
        }
    }
    
    val filteredFoldersList = remember(blacklistedFolders, whitelistedFolders, currentMode) {
        when (currentMode) {
            chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST -> blacklistedFolders
            chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST -> whitelistedFolders
        }
    }

    val settingGroups = listOf(
        SettingGroup(
            title = "Mode Selection",
            items = listOf(
                SettingItem(
                    Icons.Default.Block,
                    "Blacklist Mode",
                    "Hide specific songs and folders from your library",
                    toggleState = currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST,
                    onToggleChange = { enabled ->
                        if (enabled) {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            currentMode = chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST
                            appSettings.setMediaScanMode("blacklist")
                        }
                    }
                ),
                SettingItem(
                    Icons.Default.CheckCircle,
                    "Whitelist Mode",
                    "Only show songs from selected folders",
                    toggleState = currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST,
                    onToggleChange = { enabled ->
                        if (enabled) {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            currentMode = chromahub.rhythm.app.ui.screens.MediaScanMode.WHITELIST
                            appSettings.setMediaScanMode("whitelist")
                        }
                    }
                )
            )
        ),
        SettingGroup(
            title = "Information",
            items = listOf(
                SettingItem(
                    Icons.Default.Info,
                    "How It Works",
                    if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST)
                        "Hide specific songs or folders from library. Perfect for excluding ringtones and notifications."
                    else
                        "Only show songs from selected folders. Create a curated library.",
                    onClick = {} // Read-only info
                )
            )
        ),
        SettingGroup(
            title = "Song Management",
            items = listOf(
                SettingItem(
                    Icons.AutoMirrored.Filled.QueueMusic,
                    "Manage Songs",
                    "${filteredSongDetails.size} ${if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "blocked" else "whitelisted"} songs",
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        currentView = "songs"
                    }
                ),
                SettingItem(
                    Icons.Default.Clear,
                    "Clear All Songs",
                    "Remove all ${if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "blocked" else "whitelisted"} songs",
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                            appSettings.clearBlacklist()
                        } else {
                            appSettings.clearWhitelist()
                        }
                    }
                )
            )
        ),
        SettingGroup(
            title = "Folder Management",
            items = listOf(
                SettingItem(
                    Icons.Default.Folder,
                    "Manage Folders",
                    "${filteredFoldersList.size} ${if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "blocked" else "whitelisted"} folders",
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        currentView = "folders"
                    }
                ),
                SettingItem(
                    Icons.Default.Clear,
                    "Clear All Folders",
                    "Remove all ${if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "blocked" else "whitelisted"} folders",
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                            blacklistedFolders.forEach { folder ->
                                appSettings.removeFolderFromBlacklist(folder)
                            }
                        } else {
                            whitelistedFolders.forEach { folder ->
                                appSettings.removeFolderFromWhitelist(folder)
                            }
                        }
                    }
                )
            )
        )
    )

    CollapsibleHeaderScreen(
        title = if (currentView == "overview") "Media Scan" else if (currentView == "songs") "Manage Songs" else "Manage Folders",
        showBackButton = true,
        onBackClick = {
            if (currentView != "overview") {
                currentView = "overview"
            } else {
                onBackClick()
            }
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (currentView) {
                "songs" -> {
                    // Songs list
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                        MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${filteredSongDetails.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "Blocked" else "Whitelisted",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${allSongs.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Total",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    
                    items(filteredSongDetails, key = { it.id }) { song ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                        if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                                            appSettings.removeFromBlacklist(song.id)
                                        } else {
                                            appSettings.removeFromWhitelist(song.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RemoveCircle,
                                        contentDescription = "Remove",
                                        tint = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                "folders" -> {
                    // Folders list
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                    MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${filteredFoldersList.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) "Blocked Folders" else "Whitelisted Folders",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    items(filteredFoldersList) { folder ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                        if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) {
                                            appSettings.removeFolderFromBlacklist(folder)
                                        } else {
                                            appSettings.removeFolderFromWhitelist(folder)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RemoveCircle,
                                        contentDescription = "Remove",
                                        tint = if (currentMode == chromahub.rhythm.app.ui.screens.MediaScanMode.BLACKLIST) 
                                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                else -> {
                    // Overview - show setting groups
                    items(settingGroups) { group ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                }
            }
        }
    }
}

@Composable
fun ApiServiceRow(
    title: String,
    description: String,
    status: String,
    isConfigured: Boolean,
    icon: ImageVector,
    isEnabled: Boolean = true,
    showToggle: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                        isConfigured -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when {
                    !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    isConfigured -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = when {
                        !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                        isConfigured -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (!isEnabled) "Disabled" else status,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                            isConfigured -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        // Toggle or Arrow icon
        if (showToggle && onToggle != null) {
            Switch(
                checked = isEnabled,
                onCheckedChange = { enabled ->
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                    onToggle(enabled)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        } else {
            // Arrow icon (only for configurable services)
            if (title == "Spotify Canvas") {
                Icon(
                    imageVector = RhythmIcons.Forward,
                    contentDescription = "Configure",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SpotifyApiConfigDialog(
    currentClientId: String,
    currentClientSecret: String,
    onDismiss: () -> Unit,
    onSave: (clientId: String, clientSecret: String) -> Unit,
    appSettings: AppSettings
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var clientId by remember { mutableStateOf(currentClientId) }
    var clientSecret by remember { mutableStateOf(currentClientSecret) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Spotify API Configuration",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter your Spotify API credentials to enable track search and Canvas videos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Check if using default keys and display a warning
                val isUsingDefaultKeys =
                    currentClientId.isEmpty() && currentClientSecret.isEmpty()
                if (isUsingDefaultKeys) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You are currently using the app's default Spotify keys. For full functionality and to avoid rate limits, please input your own Client ID and Secret.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = clientId,
                    onValueChange = {
                        clientId = it
                        testResult = null
                    },
                    label = { Text("Client ID") },
                    placeholder = { Text("Your Spotify Client ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = clientSecret,
                    onValueChange = {
                        clientSecret = it
                        testResult = null
                    },
                    label = { Text("Client Secret") },
                    placeholder = { Text("Your Spotify Client Secret") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Test connection button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isTestingConnection = true
                                try {
                                    // Temporarily set the credentials for testing
                                    appSettings.setSpotifyClientId(clientId)
                                    appSettings.setSpotifyClientSecret(clientSecret)

                                    val canvasRepository =
                                        chromahub.rhythm.app.data.CanvasRepository(
                                            context,
                                            appSettings
                                        )
                                    testResult = canvasRepository.testSpotifyApiConfiguration()
                                } catch (e: Exception) {
                                    testResult = Pair(false, "Error: ${e.message}")
                                } finally {
                                    isTestingConnection = false
                                }
                            }
                        },
                        enabled = !isTestingConnection && clientId.isNotBlank() && clientSecret.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isTestingConnection) "Testing..." else "Test Connection")
                    }
                }

                // Test result display
                testResult?.let { (success, message) ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (success)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (success)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (success)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Help text
                Text(
                    text = "To get your Spotify API credentials:\n• Go to developer.spotify.com\n• Create a new app\n• Copy the Client ID and Client Secret",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(clientId, clientSecret)
                },
                enabled = clientId.isNotBlank() && clientSecret.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
        }
    )
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
        val updateItems = mutableListOf(
            SettingItem(
                Icons.Default.SystemUpdate,
                "Enable Updates",
                "Allow the app to check for and download updates",
                toggleState = updatesEnabled,
                onToggleChange = { appSettings.setUpdatesEnabled(it) }
            )
        )

        if (updatesEnabled) {
            updateItems.add(
                SettingItem(
                    Icons.Default.Update,
                    "Periodic Check",
                    "Automatically check for updates from Rhythm's GitHub repo",
                    toggleState = autoCheckForUpdates,
                    onToggleChange = { appSettings.setAutoCheckForUpdates(it) }
                )
            )
        }

        val settingGroups = listOf(
            SettingGroup(
                title = "Update Settings",
                items = updateItems
            )
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups) { group ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        group.items.forEachIndexed { index, item ->
                            TunerSettingRow(item = item)
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
        }
    }
}

@Composable
fun ExperimentalFeaturesScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val hapticFeedbackEnabled by appSettings.hapticFeedbackEnabled.collectAsState()
    val groupByAlbumArtist by appSettings.groupByAlbumArtist.collectAsState()
    val showLyrics by appSettings.showLyrics.collectAsState()

    CollapsibleHeaderScreen(
        title = "Experimental Features",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        val settingGroups = listOf(
            SettingGroup(
                title = "Interface",
                items = listOf(
                    SettingItem(
                        Icons.Default.Vibration,
                        "Haptic Feedback",
                        "Vibrate when tapping buttons and interacting with the interface",
                        toggleState = hapticFeedbackEnabled,
                        onToggleChange = { appSettings.setHapticFeedbackEnabled(it) }
                    )
                )
            ),
            SettingGroup(
                title = "Library Organization",
                items = listOf(
                    SettingItem(
                        Icons.Default.Person,
                        "Group by Album Artist",
                        "Show collaboration albums under main artist",
                        toggleState = groupByAlbumArtist,
                        onToggleChange = { appSettings.setGroupByAlbumArtist(it) }
                    )
                )
            ),
            SettingGroup(
                title = "Lyrics & Metadata",
                items = listOf(
                    SettingItem(
                        Icons.Default.Lyrics,
                        "Show Lyrics",
                        "Display lyrics when available (priority: Spotify → Apple Music → LRCLib)",
                        toggleState = showLyrics,
                        onToggleChange = { appSettings.setShowLyrics(it) }
                    )
                )
            )
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups) { group ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        group.items.forEachIndexed { index, item ->
                            TunerSettingRow(item = item)
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

            item {
                Spacer(modifier = Modifier.height(16.dp))
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
        }
    }
}

// Cache Management Screen (merged from CacheManagementBottomSheet)
@Composable
fun CacheManagementSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val musicViewModel: MusicViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Collect states
    val maxCacheSize by appSettings.maxCacheSize.collectAsState()
    val clearCacheOnExit by appSettings.clearCacheOnExit.collectAsState()
    
    // Local states
    var currentCacheSize by remember { mutableStateOf(0L) }
    var isCalculatingSize by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }
    var showCacheSizeDialog by remember { mutableStateOf(false) }
    var cacheDetails by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    
    // Canvas repository for cache management
    val canvasRepository = remember { 
        chromahub.rhythm.app.data.CanvasRepository(context, appSettings)
    }

    // Calculate cache size when the screen opens
    LaunchedEffect(Unit) {
        isCalculatingSize = true
        try {
            currentCacheSize = chromahub.rhythm.app.util.CacheManager.getCacheSize(context, canvasRepository)
            cacheDetails = chromahub.rhythm.app.util.CacheManager.getDetailedCacheSize(context, canvasRepository)
        } catch (e: Exception) {
            Log.e("CacheManagement", "Error calculating cache size", e)
        } finally {
            isCalculatingSize = false
        }
    }
    
    CollapsibleHeaderScreen(
        title = "Cache Management",
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
                Text(
                    text = "Manage cached data including images, temporary files, and other app data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Current Cache Status
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Current Cache Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isCalculatingSize) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Calculating cache size...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Total cache size
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Total Cache Size:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = chromahub.rhythm.app.util.CacheManager.formatBytes(currentCacheSize),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Cache breakdown
                            cacheDetails.forEach { (label, size) ->
                                if (size > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "  • $label:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = chromahub.rhythm.app.util.CacheManager.formatBytes(size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Cache limit
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Cache Limit:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", maxCacheSize / (1024f * 1024f))} MB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Settings
            item {
                TunerSettingCard(
                    title = "Clear Cache on Exit",
                    description = "Automatically clear cache when exiting the app",
                    icon = Icons.Default.DeleteSweep,
                    checked = clearCacheOnExit,
                    onCheckedChange = { appSettings.setClearCacheOnExit(it) }
                )
            }
            
            // Clear cache button
            item {
                Button(
                    onClick = {
                        if (!isClearingCache) {
                            scope.launch {
                                isClearingCache = true
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                try {
                                    currentCacheSize = chromahub.rhythm.app.util.CacheManager.getCacheSize(context, canvasRepository)
                                    cacheDetails = chromahub.rhythm.app.util.CacheManager.getDetailedCacheSize(context, canvasRepository)
                                    Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error clearing cache: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isClearingCache = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isClearingCache && currentCacheSize > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    if (isClearingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isClearingCache) "Clearing..." else "Clear All Cache Now")
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// BackupInfoItem composable for displaying backup information
@Composable
private fun BackupInfoItem(
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
            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

// Backup & Restore Screen (merged from BackupRestoreBottomSheet)
@Composable
fun BackupRestoreSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val musicViewModel: MusicViewModel = viewModel()
    
    // Collect states
    val autoBackupEnabled by appSettings.autoBackupEnabled.collectAsState()
    val lastBackupTimestamp by appSettings.lastBackupTimestamp.collectAsState()
    val backupLocation by appSettings.backupLocation.collectAsState()
    
    // Local states
    var isCreatingBackup by remember { mutableStateOf(false) }
    var isRestoringFromFile by remember { mutableStateOf(false) }
    var isRestoringFromClipboard by remember { mutableStateOf(false) }
    var showBackupSuccess by remember { mutableStateOf(false) }
    var showRestoreSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // File picker launcher for backup export
    val backupLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        isCreatingBackup = true
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        
                        musicViewModel.ensurePlaylistsSaved()
                        val backupJson = appSettings.createBackup()
                        
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(backupJson.toByteArray())
                            outputStream.flush()
                        }
                        
                        appSettings.setLastBackupTimestamp(System.currentTimeMillis())
                        appSettings.setBackupLocation(uri.toString())
                        
                        showBackupSuccess = true
                        
                        // Also copy to clipboard
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Rhythm Backup", backupJson)
                        clipboard.setPrimaryClip(clip)
                    } catch (e: Exception) {
                        errorMessage = "Failed to create backup: ${e.message}"
                        showError = true
                    } finally {
                        isCreatingBackup = false
                    }
                }
            }
        } else {
            isCreatingBackup = false
        }
    }
    
    // File picker launcher for restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        isRestoringFromFile = true
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val backupJson = inputStream?.bufferedReader()?.use { it.readText() }
                        
                        if (!backupJson.isNullOrEmpty()) {
                            if (appSettings.restoreFromBackup(backupJson)) {
                                musicViewModel.reloadPlaylistsFromSettings()
                                showRestoreSuccess = true
                            } else {
                                errorMessage = "Invalid backup format or corrupted data"
                                showError = true
                            }
                        } else {
                            errorMessage = "Unable to read the backup file"
                            showError = true
                        }
                    } catch (e: Exception) {
                        errorMessage = "Failed to restore from file: ${e.message}"
                        showError = true
                    } finally {
                        isRestoringFromFile = false
                    }
                }
            }
        } else {
            isRestoringFromFile = false
        }
    }
    
    // Restore from clipboard logic
    fun restoreFromClipboard() {
        scope.launch {
            try {
                isRestoringFromClipboard = true
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                
                // Get backup from clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val backupJson = clip.getItemAt(0).text.toString()
                    
                    if (appSettings.restoreFromBackup(backupJson)) {
                        // Reload playlists from restored settings
                        musicViewModel.reloadPlaylistsFromSettings()
                        showRestoreSuccess = true
                    } else {
                        errorMessage = "Invalid backup format or corrupted data"
                        showError = true
                    }
                } else {
                    errorMessage = "No backup data found in clipboard. Please copy a backup first."
                    showError = true
                }
            } catch (e: Exception) {
                errorMessage = "Failed to restore backup: ${e.message}"
                showError = true
            } finally {
                isRestoringFromClipboard = false
            }
        }
    }
    
    CollapsibleHeaderScreen(
        title = "Backup & Restore",
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
                Text(
                    text = "Safeguard your personalized settings, playlists, and preferences.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Backup Status Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Last Backup Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lastBackupTimestamp > 0) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (lastBackupTimestamp > 0) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (lastBackupTimestamp > 0) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (lastBackupTimestamp > 0) {
                                    val sdf = SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                                    sdf.format(Date(lastBackupTimestamp))
                                } else "Never",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (lastBackupTimestamp > 0) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Last Backup",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (lastBackupTimestamp > 0) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Success/Error Dialogs for BackupRestoreSettingsScreen
                    if (showBackupSuccess) {
                        AlertDialog(
                            onDismissRequest = { showBackupSuccess = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            title = { Text("Backup Created Successfully") },
                            text = {
                                Text("Your complete Rhythm backup has been created including:\n\n" +
                                        "• All app settings and preferences\n" +
                                        "• Your playlists and favorite songs\n" +
                                        "• Blacklisted/whitelisted songs and folders\n" +
                                        "• Pinned folders and library customization\n" +
                                        "• Theme settings (colors, fonts, album art colors)\n" +
                                        "• Audio preferences and API settings\n" +
                                        "• Recently played history and statistics\n\n" +
                                        "The backup has been saved and copied to your clipboard for easy sharing.")
                            },
                            confirmButton = {
                                Button(onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                    showBackupSuccess = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("OK")
                                }
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                    }

                    if (showRestoreSuccess) {
                        AlertDialog(
                            onDismissRequest = { showRestoreSuccess = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            title = { Text("Restore Completed Successfully") },
                            text = {
                                Text("Your Rhythm data has been restored successfully including:\n\n" +
                                        "• All app settings and preferences\n" +
                                        "• Your playlists and favorite songs\n" +
                                        "• Blacklisted songs and folders\n" +
                                        "• Theme and audio preferences\n\n" +
                                        "Please restart the app for all changes to take full effect.")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                        showRestoreSuccess = false

                                        // Restart the app
                                        val packageManager = context.packageManager
                                        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                                        val componentName = intent?.component
                                        val mainIntent = Intent.makeRestartActivityTask(componentName)
                                        context.startActivity(mainIntent)
                                        (context as? Activity)?.finish()
                                        exitProcess(0)
                                    }
                                ) {
                                    Text("Restart Now")
                                }
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                    }

                    if (showError) {
                        AlertDialog(
                            onDismissRequest = { showError = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            title = { Text("Error") },
                            text = { Text(errorMessage) },
                            confirmButton = {
                                Button(onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                    showError = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("OK")
                                }
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                    
                    // Auto Backup Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (autoBackupEnabled) 
                                MaterialTheme.colorScheme.tertiaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (autoBackupEnabled) Icons.Filled.Autorenew else Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (autoBackupEnabled) "Enabled" else "Manual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Auto Backup",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (autoBackupEnabled) 
                                    MaterialTheme.colorScheme.onTertiaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Backup location info if available
            backupLocation?.let { location ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = location.substringAfterLast("/"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            // Auto-backup toggle
            item {
                TunerSettingCard(
                    title = "Auto-backup",
                    description = "Automatically backup settings weekly",
                    icon = Icons.Filled.Autorenew,
                    checked = autoBackupEnabled,
                    onCheckedChange = { 
                        appSettings.setAutoBackupEnabled(it)
                        if (it) appSettings.triggerImmediateBackup()
                    }
                )
            }
            
            // Backup action button
            item {
                Button(
                    onClick = {
                        if (!isCreatingBackup) {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                                putExtra(Intent.EXTRA_TITLE, "rhythm_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())}.json")
                            }
                            backupLocationLauncher.launch(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreatingBackup,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCreatingBackup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isCreatingBackup) "Creating Backup..." else "Create Backup to File")
                }
            }
            
            // Restore buttons row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Restore from Clipboard Button
                    OutlinedButton(
                        onClick = {
                            if (!isRestoringFromClipboard && !isRestoringFromFile && !isCreatingBackup) {
                                restoreFromClipboard()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isRestoringFromClipboard && !isRestoringFromFile && !isCreatingBackup,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isRestoringFromClipboard) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isRestoringFromClipboard) "Restoring..." else "From Clipboard")
                    }
                    
                    // Restore from File Button
                    OutlinedButton(
                        onClick = {
                            if (!isRestoringFromFile && !isRestoringFromClipboard && !isCreatingBackup) {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "application/json"
                                }
                                filePickerLauncher.launch(intent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isRestoringFromFile && !isRestoringFromClipboard && !isCreatingBackup,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isRestoringFromFile) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Filled.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isRestoringFromFile) "Restoring..." else "From File")
                    }
                }
            }
            
            // Tips/Information Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "What's Included in Backup?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        BackupInfoItem(
                            icon = Icons.Filled.Save,
                            text = "All settings, playlists, themes, and library customization"
                        )
                        BackupInfoItem(
                            icon = Icons.Filled.RestoreFromTrash,
                            text = "Restore from files or clipboard with one tap"
                        )
                        BackupInfoItem(
                            icon = Icons.Filled.Security,
                            text = "Backups stored locally on your device for privacy"
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// Library Tab Order Screen (merged from LibraryTabOrderBottomSheet)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryTabOrderSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    val tabOrder by appSettings.libraryTabOrder.collectAsState()
    var reorderableList by remember { mutableStateOf(tabOrder.toList()) }
    
    // Helper function to get display name and icon for tab
    fun getTabInfo(tabId: String): Pair<String, ImageVector> {
        return when (tabId) {
            "SONGS" -> Pair("Songs", RhythmIcons.Relax)
            "PLAYLISTS" -> Pair("Playlists", RhythmIcons.PlaylistFilled)
            "ALBUMS" -> Pair("Albums", RhythmIcons.Music.Album)
            "ARTISTS" -> Pair("Artists", RhythmIcons.Artist)
            "EXPLORER" -> Pair("Explorer", Icons.Default.Folder)
            else -> Pair(tabId, RhythmIcons.Music.Song)
        }
    }
    
    CollapsibleHeaderScreen(
        title = "Library Tab Order",
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
                Text(
                    text = "Reorder tabs to customize your library experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Reorderable list
            itemsIndexed(
                items = reorderableList,
                key = { _, item -> item }
            ) { index, tabId ->
                val (tabName, tabIcon) = getTabInfo(tabId)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .animateItem(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Position indicator
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            // Tab icon
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            // Tab name
                            Text(
                                text = tabName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Reorder buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Move up button
                            FilledIconButton(
                                onClick = {
                                    if (index > 0) {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                        val newList = reorderableList.toMutableList()
                                        val item = newList.removeAt(index)
                                        newList.add(index - 1, item)
                                        reorderableList = newList
                                    }
                                },
                                enabled = index > 0,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                ),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move up",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Move down button
                            FilledIconButton(
                                onClick = {
                                    if (index < reorderableList.size - 1) {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                        val newList = reorderableList.toMutableList()
                                        val item = newList.removeAt(index)
                                        newList.add(index + 1, item)
                                        reorderableList = newList
                                    }
                                },
                                enabled = index < reorderableList.size - 1,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                ),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move down",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reset button
                    OutlinedButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            appSettings.resetLibraryTabOrder()
                            reorderableList = listOf("SONGS", "PLAYLISTS", "ALBUMS", "ARTISTS", "EXPLORER")
                            Toast.makeText(context, "Tab order reset to default", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset")
                    }
                    
                    // Save button
                    Button(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            appSettings.setLibraryTabOrder(reorderableList)
                            Toast.makeText(context, "Tab order saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ✅ FULLY INTEGRATED Theme Customization Screen
// Data classes and enums for theme customization
data class ColorSchemeOption(
    val name: String,
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color
)

data class FontOption(
    val name: String,
    val displayName: String,
    val description: String
)

enum class ColorSource(val displayName: String, val description: String, val icon: ImageVector) {
    ALBUM_ART("Album Art", "Extract colors from currently playing album artwork", Icons.Filled.Image),
    MONET("System Colors", "Use Material You colors from your wallpaper", Icons.Filled.ColorLens),
    CUSTOM("Custom Scheme", "Choose from predefined color schemes", Icons.Filled.Palette)
}

enum class FontSource(val displayName: String, val description: String, val icon: ImageVector) {
    SYSTEM("System Font", "Use the device's default font", Icons.Filled.PhoneAndroid),
    CUSTOM("Custom Font", "Import and use a custom font file", Icons.Filled.TextFields)
}

// HSL Color conversion utilities
data class HSLColor(val hue: Float, val saturation: Float, val lightness: Float)

fun Color.toHSL(): HSLColor {
    val r = red
    val g = green
    val b = blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val diff = max - min

    val lightness = (max + min) / 2f

    val saturation = if (diff == 0f) 0f else diff / (1f - kotlin.math.abs(2f * lightness - 1f))

    val hue = when (max) {
        min -> 0f
        r -> ((g - b) / diff) % 6
        g -> (b - r) / diff + 2
        b -> (r - g) / diff + 4
        else -> 0f
    } * 60f

    return HSLColor(
        hue = if (hue < 0) hue + 360f else hue,
        saturation = saturation,
        lightness = lightness
    )
}

fun HSLColor.toColor(): Color {
    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f

    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f),
        alpha = 1f
    )
}

@Composable
fun ThemeCustomizationSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val haptic = LocalHapticFeedback.current

    // Theme states
    val useSystemTheme by appSettings.useSystemTheme.collectAsState()
    val darkMode by appSettings.darkMode.collectAsState()
    val useDynamicColors by appSettings.useDynamicColors.collectAsState()
    val customColorScheme by appSettings.customColorScheme.collectAsState()
    val colorSource by appSettings.colorSource.collectAsState()
    val extractedAlbumColors by appSettings.extractedAlbumColors.collectAsState()

    // Font states
    val fontSource by appSettings.fontSource.collectAsState()
    val customFontPath by appSettings.customFontPath.collectAsState()
    val customFontFamily by appSettings.customFontFamily.collectAsState()

    // Festive theme states
    val festiveThemeEnabled by appSettings.festiveThemeEnabled.collectAsState()
    val festiveThemeSelected by appSettings.festiveThemeSelected.collectAsState()
    val festiveThemeAutoDetect by appSettings.festiveThemeAutoDetect.collectAsState()
    val festiveThemeShowParticles by appSettings.festiveThemeShowParticles.collectAsState()
    val festiveThemeShowDecorations by appSettings.festiveThemeShowDecorations.collectAsState()
    val festiveThemeParticleIntensity by appSettings.festiveThemeParticleIntensity.collectAsState()
    val festiveThemeApplyToSplash by appSettings.festiveThemeApplyToSplash.collectAsState()
    val festiveThemeApplyToMainUI by appSettings.festiveThemeApplyToMainUI.collectAsState()

    // Color source state - initialize based on saved setting
    var selectedColorSource by remember(colorSource) {
        mutableStateOf(
            when (colorSource) {
                "ALBUM_ART" -> ColorSource.ALBUM_ART
                "MONET" -> ColorSource.MONET
                "CUSTOM" -> ColorSource.CUSTOM
                else -> ColorSource.CUSTOM
            }
        )
    }

    // Font source state - initialize based on saved setting
    var selectedFontSource by remember(fontSource) {
        mutableStateOf(
            when (fontSource) {
                "CUSTOM" -> FontSource.CUSTOM
                "SYSTEM" -> FontSource.SYSTEM
                else -> FontSource.SYSTEM
            }
        )
    }

    // Font file picker launcher
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copy font to internal storage
            val fontPath = FontLoader.copyFontToInternalStorage(context, it)
            if (fontPath != null) {
                // Validate that the font can be loaded
                val testFont = FontLoader.loadCustomFont(context, fontPath)
                if (testFont != null) {
                    // Save to settings
                    appSettings.setCustomFontPath(fontPath)
                    appSettings.setFontSource("CUSTOM")

                    // Extract and save font name
                    val fontName = FontLoader.getFontFileName(fontPath) ?: "Custom Font"
                    appSettings.setCustomFontFamily(fontName)

                    // Show success feedback
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Font imported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Font file copied but can't be loaded
                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.Reject)
                    Toast.makeText(context, "Invalid font file format", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Failed to copy font file
                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.Reject)
                Toast.makeText(context, "Failed to import font file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Color schemes - expanded list matching bottomsheet
    val colorSchemes = remember {
        listOf(
            ColorSchemeOption(
                name = "Default",
                displayName = "Default Purple",
                description = "The classic Rhythm experience with vibrant purple tones",
                primaryColor = Color(0xFF5C4AD5),
                secondaryColor = Color(0xFF5D5D6B),
                tertiaryColor = Color(0xFFFFDDB6)
            ),
            ColorSchemeOption(
                name = "Warm",
                displayName = "Warm Sunset",
                description = "Cozy orange and red tones for a warm atmosphere",
                primaryColor = Color(0xFFFF6B35),
                secondaryColor = Color(0xFFF7931E),
                tertiaryColor = Color(0xFFFFC857)
            ),
            ColorSchemeOption(
                name = "Cool",
                displayName = "Cool Ocean",
                description = "Refreshing blue and teal tones for a calming vibe",
                primaryColor = Color(0xFF1E88E5),
                secondaryColor = Color(0xFF00897B),
                tertiaryColor = Color(0xFF80DEEA)
            ),
            ColorSchemeOption(
                name = "Forest",
                displayName = "Forest Green",
                description = "Natural green tones inspired by nature",
                primaryColor = Color(0xFF2E7D32),
                secondaryColor = Color(0xFF558B2F),
                tertiaryColor = Color(0xFF9CCC65)
            ),
            ColorSchemeOption(
                name = "Rose",
                displayName = "Rose Pink",
                description = "Elegant pink and magenta tones",
                primaryColor = Color(0xFFE91E63),
                secondaryColor = Color(0xFFC2185B),
                tertiaryColor = Color(0xFFF8BBD0)
            ),
            ColorSchemeOption(
                name = "Monochrome",
                displayName = "Monochrome",
                description = "Minimalist grayscale for a clean, modern look",
                primaryColor = Color(0xFF424242),
                secondaryColor = Color(0xFF616161),
                tertiaryColor = Color(0xFF9E9E9E)
            ),
            ColorSchemeOption(
                name = "Lavender",
                displayName = "Lavender",
                description = "Calming purple and lavender tones for relaxation",
                primaryColor = Color(0xFF7C4DFF),
                secondaryColor = Color(0xFF9575CD),
                tertiaryColor = Color(0xFFBA68C8)
            ),
            ColorSchemeOption(
                name = "Ocean",
                displayName = "Deep Ocean",
                description = "Deep blues and aquamarines for oceanic serenity",
                primaryColor = Color(0xFF006064),
                secondaryColor = Color(0xFF00838F),
                tertiaryColor = Color(0xFF00ACC1)
            ),
            ColorSchemeOption(
                name = "Aurora",
                displayName = "Northern Lights",
                description = "Vibrant greens and blues inspired by the aurora borealis",
                primaryColor = Color(0xFF00C853),
                secondaryColor = Color(0xFF00E676),
                tertiaryColor = Color(0xFF69F0AE)
            ),
            ColorSchemeOption(
                name = "Amber",
                displayName = "Golden Amber",
                description = "Rich amber and gold tones for a luxurious feel",
                primaryColor = Color(0xFFFF6F00),
                secondaryColor = Color(0xFFFF8F00),
                tertiaryColor = Color(0xFFFFC107)
            ),
            ColorSchemeOption(
                name = "Crimson",
                displayName = "Deep Crimson",
                description = "Bold burgundy and crimson shades for drama",
                primaryColor = Color(0xFFB71C1C),
                secondaryColor = Color(0xFFC62828),
                tertiaryColor = Color(0xFFD32F2F)
            ),
            ColorSchemeOption(
                name = "Emerald",
                displayName = "Emerald Dream",
                description = "Fresh emerald greens with natural forest hues",
                primaryColor = Color(0xFF2E7D32),
                secondaryColor = Color(0xFF388E3C),
                tertiaryColor = Color(0xFF4CAF50)
            ),
            ColorSchemeOption(
                name = "Mint",
                displayName = "Mint",
                description = "Fresh and clean cyan and mint green tones",
                primaryColor = Color(0xFF0097A7),
                secondaryColor = Color(0xFF00ACC1),
                tertiaryColor = Color(0xFF00BCD4)
            )
        )
    }

    // Font options - matching bottomsheet
    val fontOptions = remember {
        listOf(
            FontOption(
                name = "System",
                displayName = "System Default",
                description = "Use your device's default font"
            ),
            FontOption(
                name = "Slate",
                displayName = "Slate",
                description = "Elegant serif font with a classic, traditional appearance"
            ),
            FontOption(
                name = "Inter",
                displayName = "Inter",
                description = "Clean and modern sans-serif font, highly readable"
            ),
            FontOption(
                name = "JetBrains",
                displayName = "JetBrains Mono",
                description = "Monospace font perfect for technical content"
            ),
            FontOption(
                name = "Quicksand",
                displayName = "Quicksand",
                description = "Rounded font with a softer, friendlier appearance"
            )
        )
    }
    val currentFont by appSettings.customFont.collectAsState()

    // Dialog states
    var showColorSourceDialog by remember { mutableStateOf(false) }
    var showFontSourceDialog by remember { mutableStateOf(false) }
    var showColorSchemesDialog by remember { mutableStateOf(false) }
    var showCustomColorsDialog by remember { mutableStateOf(false) }
    var showFontSelectionDialog by remember { mutableStateOf(false) }
    var showParticleIntensityDialog by remember { mutableStateOf(false) }

    CollapsibleHeaderScreen(
        title = "Theme Customization",
        showBackButton = true,
        onBackClick = onBackClick
    ) { modifier ->
        val settingGroups = listOf(
            SettingGroup(
                title = "Overview",
                items = listOf(
                    SettingItem(
                        Icons.Default.Brightness6,
                        "Dynamic Colors",
                        "Use system colors for theme",
                        toggleState = useDynamicColors,
                        onToggleChange = { appSettings.setUseDynamicColors(it) }
                    ),
                    SettingItem(
                        Icons.Default.Settings,
                        "System Theme",
                        "Follow system light/dark mode",
                        toggleState = useSystemTheme,
                        onToggleChange = { appSettings.setUseSystemTheme(it) }
                    ),
                    SettingItem(
                        Icons.Default.DarkMode,
                        "Dark Mode",
                        "Enable dark theme",
                        toggleState = darkMode,
                        onToggleChange = { appSettings.setDarkMode(it) }
                    ),
                    SettingItem(
                        Icons.Default.Palette,
                        "Color Source",
                        when (selectedColorSource) {
                            ColorSource.ALBUM_ART -> "Album Art (extracts from artwork)"
                            ColorSource.MONET -> "System Colors (Material You)"
                            ColorSource.CUSTOM -> "Custom Scheme (${customColorScheme})"
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            showColorSourceDialog = true
                        }
                    ),
                    SettingItem(
                        Icons.Default.TextFields,
                        "Font Source",
                        when (selectedFontSource) {
                            FontSource.SYSTEM -> "System (${currentFont})"
                            FontSource.CUSTOM -> "Custom (${customFontFamily ?: "No font imported"})"
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            showFontSourceDialog = true
                        }
                    )
                )
            ),
            SettingGroup(
                title = "Colors",
                items = listOf(
                    SettingItem(
                        Icons.Default.ColorLens,
                        "Color Schemes",
                        if (selectedColorSource == ColorSource.CUSTOM) 
                            "Current: ${customColorScheme}" 
                        else 
                            "Available when using Custom color source",
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            showColorSchemesDialog = true
                        }
                    ),
                    SettingItem(
                        Icons.Default.Brush,
                        "Custom Colors",
                        if (selectedColorSource == ColorSource.CUSTOM) 
                            "Create your own color palette" 
                        else 
                            "Available when using Custom color source",
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            showCustomColorsDialog = true
                        }
                    )
                )
            ),
            SettingGroup(
                title = "Fonts",
                items = listOf(
                    SettingItem(
                        Icons.Default.TextFields,
                        "Font Selection",
                        if (selectedFontSource == FontSource.SYSTEM) 
                            "Current: ${currentFont}" 
                        else 
                            "Available when using System font source",
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            showFontSelectionDialog = true
                        }
                    ),
                    SettingItem(
                        Icons.Default.FileUpload,
                        "Import Font",
                        if (customFontPath != null) 
                            "Custom font imported: ${customFontFamily}" 
                        else 
                            "Import custom font file (.ttf, .otf)",
                        onClick = {
                            HapticUtils.performHapticFeedback(
                                context,
                                haptic,
                                HapticFeedbackType.LongPress
                            )
                            fontPickerLauncher.launch("font/*")
                        }
                    )
                )
            ),
            SettingGroup(
                title = "Festive",
                items = listOf(
                    SettingItem(
                        Icons.Default.Celebration,
                        "Festive Theme",
                        "Enable festive theme",
                        toggleState = festiveThemeEnabled,
                        onToggleChange = { appSettings.setFestiveThemeEnabled(it) }
                    ),
                    SettingItem(
                        Icons.Default.Schedule,
                        "Auto Detect",
                        "Automatically detect festive seasons",
                        toggleState = festiveThemeAutoDetect,
                        onToggleChange = { appSettings.setFestiveThemeAutoDetect(it) }
                    ),
                    SettingItem(
                        Icons.Default.Brightness7,
                        "Show Particles",
                        "Display festive particles",
                        toggleState = festiveThemeShowParticles,
                        onToggleChange = { appSettings.setFestiveThemeShowParticles(it) }
                    ),
                    SettingItem(
                        Icons.Default.Celebration,
                        "Show Decorations",
                        "Display festive decorations",
                        toggleState = festiveThemeShowDecorations,
                        onToggleChange = { appSettings.setFestiveThemeShowDecorations(it) }
                    ),
                    SettingItem(
                        Icons.Default.Speed,
                        "Particle Intensity",
                        "Adjust particle effect intensity",
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                            showParticleIntensityDialog = true
                        }
                    ),
                    SettingItem(
                        Icons.Default.Flare,
                        "Apply to Splash",
                        "Use festive theme on app launch",
                        toggleState = festiveThemeApplyToSplash,
                        onToggleChange = { appSettings.setFestiveThemeApplyToSplash(it) }
                    ),
                    SettingItem(
                        Icons.Default.Home,
                        "Apply to Main UI",
                        "Use festive theme in main interface",
                        toggleState = festiveThemeApplyToMainUI,
                        onToggleChange = { appSettings.setFestiveThemeApplyToMainUI(it) }
                    )
                )
            )
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            items(settingGroups) { group ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        group.items.forEachIndexed { index, item ->
                            TunerSettingRow(item = item)
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

            // Tips Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Good to Know",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        ThemeTipItem(
                            icon = Icons.Filled.Palette,
                            text = "Album Art colors adapt dynamically to your music"
                        )
                        ThemeTipItem(
                            icon = Icons.Filled.Wallpaper,
                            text = "Material You uses system wallpaper colors (Android 12+)"
                        )
                        ThemeTipItem(
                            icon = Icons.Filled.FontDownload,
                            text = "Import custom fonts to personalize typography"
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    ColorSourceDialog(
        showDialog = showColorSourceDialog,
        onDismiss = { showColorSourceDialog = false },
        selectedColorSource = selectedColorSource,
        onColorSourceSelected = { selectedColorSource = it },
        appSettings = appSettings,
        context = context,
        haptic = haptic
    )

    FontSourceDialog(
        showDialog = showFontSourceDialog,
        onDismiss = { showFontSourceDialog = false },
        selectedFontSource = selectedFontSource,
        onFontSourceSelected = { selectedFontSource = it },
        appSettings = appSettings,
        customFontPath = customFontPath,
        context = context,
        haptic = haptic
    )

    ColorSchemesDialog(
        showDialog = showColorSchemesDialog,
        onDismiss = { showColorSchemesDialog = false },
        colorSchemes = colorSchemes,
        currentScheme = customColorScheme,
        selectedColorSource = selectedColorSource,
        onSchemeSelected = { scheme ->
            appSettings.setCustomColorScheme(scheme)
            showColorSchemesDialog = false
        },
        appSettings = appSettings,
        context = context,
        haptic = haptic
    )

    CustomColorsDialog(
        showDialog = showCustomColorsDialog,
        onDismiss = { showCustomColorsDialog = false },
        currentScheme = customColorScheme,
        selectedColorSource = selectedColorSource,
        onApply = { primary, secondary, tertiary ->
            val primaryHex = String.format("%06X", (primary.toArgb() and 0xFFFFFF))
            val secondaryHex = String.format("%06X", (secondary.toArgb() and 0xFFFFFF))
            val tertiaryHex = String.format("%06X", (tertiary.toArgb() and 0xFFFFFF))
            val customScheme = "custom_${primaryHex}_${secondaryHex}_${tertiaryHex}"
            appSettings.setCustomColorScheme(customScheme)
        },
        appSettings = appSettings,
        context = context,
        haptic = haptic
    )

    FontSelectionDialog(
        showDialog = showFontSelectionDialog,
        onDismiss = { showFontSelectionDialog = false },
        fontOptions = fontOptions,
        currentFont = currentFont,
        selectedFontSource = selectedFontSource,
        onFontSelected = { selectedFont ->
            appSettings.setCustomFont(selectedFont)
            showFontSelectionDialog = false
        },
        appSettings = appSettings,
        context = context,
        haptic = haptic
    )

    ParticleIntensityDialog(
        showDialog = showParticleIntensityDialog,
        onDismiss = { showParticleIntensityDialog = false },
        currentIntensity = festiveThemeParticleIntensity,
        onIntensityChanged = { /* handled in dialog */ },
        appSettings = appSettings,
        context = context,
        haptic = haptic
    )
}

// Color Source and Font Source Dialogs for Theme Customization
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSourceDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    selectedColorSource: ColorSource,
    onColorSourceSelected: (ColorSource) -> Unit,
    appSettings: AppSettings,
    context: Context,
    haptic: HapticFeedback
) {
    if (showDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        // Animation states
        var showContent by remember { mutableStateOf(false) }

        val contentAlpha by animateFloatAsState(
            targetValue = if (showContent) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "contentAlpha"
        )

        LaunchedEffect(Unit) {
            delay(100)
            showContent = true
        }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .graphicsLayer(alpha = contentAlpha)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Color Source",
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
                                text = "Choose how colors are extracted",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ColorSource.entries.forEach { source ->
                        val isSelected = selectedColorSource == source
                        Card(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                onColorSourceSelected(source)
                                when (source) {
                                    ColorSource.MONET -> {
                                        appSettings.setUseDynamicColors(true)
                                        appSettings.setColorSource("MONET")
                                    }
                                    ColorSource.ALBUM_ART -> {
                                        appSettings.setUseDynamicColors(false)
                                        appSettings.setColorSource("ALBUM_ART")
                                    }
                                    ColorSource.CUSTOM -> {
                                        appSettings.setUseDynamicColors(false)
                                        appSettings.setColorSource("CUSTOM")
                                    }
                                }
                                onDismiss()
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = source.icon,
                                            contentDescription = null,
                                            tint = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = source.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = source.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSourceDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    selectedFontSource: FontSource,
    onFontSourceSelected: (FontSource) -> Unit,
    appSettings: AppSettings,
    customFontPath: String?,
    context: Context,
    haptic: HapticFeedback
) {
    if (showDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        // Animation states
        var showContent by remember { mutableStateOf(false) }

        val contentAlpha by animateFloatAsState(
            targetValue = if (showContent) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "contentAlpha"
        )

        LaunchedEffect(Unit) {
            delay(100)
            showContent = true
        }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .graphicsLayer(alpha = contentAlpha)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Font Source",
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
                                text = "Choose where to load fonts from",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FontSource.entries.forEach { source ->
                        val isSelected = selectedFontSource == source
                        Card(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                onFontSourceSelected(source)
                                when (source) {
                                    FontSource.SYSTEM -> {
                                        appSettings.setFontSource("SYSTEM")
                                        if (customFontPath == null) {
                                            appSettings.setCustomFont("System")
                                        }
                                    }
                                    FontSource.CUSTOM -> {
                                        appSettings.setFontSource("CUSTOM")
                                    }
                                }
                                onDismiss()
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = source.icon,
                                            contentDescription = null,
                                            tint = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = source.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = source.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
//                                    modifier = Modifier.size(40.dp)
//                                ) {
//                                    Box(
//                                        contentAlignment = Alignment.Center,
//                                        modifier = Modifier.fillMaxSize()
//                                    ) {
//                                        Icon(
//                                            imageVector = source.icon,
//                                            contentDescription = null,
//                                            tint = if (isSelected)
//                                                MaterialTheme.colorScheme.onPrimary
//                                            else
//                                                MaterialTheme.colorScheme.onSurfaceVariant,
//                                            modifier = Modifier.size(20.dp)
//                                        )
//                                    }
//                                }
//
//                                Spacer(modifier = Modifier.width(16.dp))
//
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(
//                                        text = source.displayName,
//                                        style = MaterialTheme.typography.titleMedium,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = if (isSelected)
//                                            MaterialTheme.colorScheme.onPrimaryContainer
//                                        else
//                                            MaterialTheme.colorScheme.onSurface
//                                    )
//                                    Spacer(modifier = Modifier.height(4.dp))
//                                    Text(
//                                        text = source.description,
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = if (isSelected)
//                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
//                                        else
//                                            MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                }
//
//                                if (isSelected) {
//                                    Icon(
//                                        imageVector = Icons.Filled.CheckCircle,
//                                        contentDescription = "Selected",
//                                        tint = MaterialTheme.colorScheme.primary,
//                                        modifier = Modifier.size(24.dp)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

// Color Schemes Dialog for Theme Customization
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSchemesDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    colorSchemes: List<ColorSchemeOption>,
    currentScheme: String,
    selectedColorSource: ColorSource,
    onSchemeSelected: (String) -> Unit,
    appSettings: AppSettings,
    context: Context,
    haptic: HapticFeedback
) {
    if (showDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        // Animation states
        var showContent by remember { mutableStateOf(false) }

        val contentAlpha by animateFloatAsState(
            targetValue = if (showContent) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "contentAlpha"
        )

        LaunchedEffect(Unit) {
            delay(100)
            showContent = true
        }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .graphicsLayer(alpha = contentAlpha)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Color Schemes",
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
                                text = "Choose from preset color schemes",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedColorSource != ColorSource.CUSTOM) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Color schemes are only available when using Custom color source.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Switch to Custom in the Color Source setting to access predefined schemes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Featured schemes
                        item {
                            Text(
                                text = "FEATURED SCHEMES",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        val featuredSchemes = colorSchemes.filter {
                            it.name in listOf("Default", "Warm", "Cool", "Forest", "Rose", "Monochrome")
                        }

                        items(featuredSchemes) { option ->
                            ColorSchemeCard(
                                option = option,
                                isSelected = currentScheme == option.name,
                                onSelect = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    onSchemeSelected(option.name)
                                }
                            )
                        }

                        // More schemes
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "MORE SCHEMES",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        val otherSchemes = colorSchemes.filter {
                            it.name !in listOf("Default", "Warm", "Cool", "Forest", "Rose", "Monochrome")
                        }

                        items(otherSchemes) { option ->
                            ColorSchemeCard(
                                option = option,
                                isSelected = currentScheme == option.name,
                                onSelect = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    onSchemeSelected(option.name)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSchemeCard(
    option: ColorSchemeOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced color preview with better visibility
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = option.primaryColor,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = option.secondaryColor,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = option.tertiaryColor,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                ) {}
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// Custom Colors Dialog for Theme Customization
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomColorsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentScheme: String,
    selectedColorSource: ColorSource,
    onApply: (Color, Color, Color) -> Unit,
    appSettings: AppSettings,
    context: Context,
    haptic: HapticFeedback
) {
    if (showDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        // Animation states
        var showContent by remember { mutableStateOf(false) }

        val contentAlpha by animateFloatAsState(
            targetValue = if (showContent) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "contentAlpha"
        )

        LaunchedEffect(Unit) {
            delay(100)
            showContent = true
        }
        
        // Parse current custom colors from the scheme name, or use defaults
        val customScheme = parseCustomColorScheme(currentScheme, false)

        var primaryColor by remember(currentScheme) {
            if (customScheme != null) {
                mutableStateOf(customScheme.primary)
            } else {
                mutableStateOf(Color(0xFF5C4AD5)) // Default purple
            }
        }
        var secondaryColor by remember(currentScheme) {
            if (customScheme != null) {
                mutableStateOf(customScheme.secondary)
            } else {
                mutableStateOf(Color(0xFF5D5D6B))
            }
        }
        var tertiaryColor by remember(currentScheme) {
            if (customScheme != null) {
                mutableStateOf(customScheme.tertiary)
            } else {
                mutableStateOf(Color(0xFFFFDDB6))
            }
        }

        var selectedColorType by remember { mutableStateOf(ColorType.PRIMARY) }

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .graphicsLayer(alpha = contentAlpha)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Custom Color Picker",
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
                                text = "Create your own color scheme",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedColorSource != ColorSource.CUSTOM) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Custom colors are only available when using Custom color source.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Switch to Custom in the Color Source setting to create your own colors.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Color preview row with selection
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ColorPreviewItem(
                                    label = "Primary",
                                    color = primaryColor,
                                    isSelected = selectedColorType == ColorType.PRIMARY,
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                        selectedColorType = ColorType.PRIMARY
                                    }
                                )
                                ColorPreviewItem(
                                    label = "Secondary",
                                    color = secondaryColor,
                                    isSelected = selectedColorType == ColorType.SECONDARY,
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                        selectedColorType = ColorType.SECONDARY
                                    }
                                )
                                ColorPreviewItem(
                                    label = "Tertiary",
                                    color = tertiaryColor,
                                    isSelected = selectedColorType == ColorType.TERTIARY,
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                        selectedColorType = ColorType.TERTIARY
                                    }
                                )
                            }
                        }

                        // Color picker controls
                        item {
                            when (selectedColorType) {
                                ColorType.PRIMARY -> ColorPickerControls(
                                    color = primaryColor,
                                    onColorChange = { primaryColor = it }
                                )
                                ColorType.SECONDARY -> ColorPickerControls(
                                    color = secondaryColor,
                                    onColorChange = { secondaryColor = it }
                                )
                                ColorType.TERTIARY -> ColorPickerControls(
                                    color = tertiaryColor,
                                    onColorChange = { tertiaryColor = it }
                                )
                            }
                        }

                        // Preset colors section
                        item {
                            Text(
                                text = "Quick Presets",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val presetColors = listOf(
                            Color(0xFF5C4AD5), Color(0xFFFF6B35), Color(0xFF1E88E5),
                            Color(0xFF2E7D32), Color(0xFFE91E63), Color(0xFF424242),
                            Color(0xFF7C4DFF), Color(0xFF006064), Color(0xFF00C853),
                            Color(0xFFFF6F00), Color(0xFFB71C1C), Color(0xFF0097A7)
                        )

                        // Preset color grid
                        presetColors.chunked(6).forEach { rowColors ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowColors.forEach { presetColor ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = presetColor,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .weight(1f)
                                                .clickable {
                                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                                    when (selectedColorType) {
                                                        ColorType.PRIMARY -> primaryColor = presetColor
                                                        ColorType.SECONDARY -> secondaryColor = presetColor
                                                        ColorType.TERTIARY -> tertiaryColor = presetColor
                                                    }
                                                }
                                        ) {}
                                    }
                                    // Fill remaining space if row is not full
                                    repeat(6 - rowColors.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        // Buttons
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                        onApply(primaryColor, secondaryColor, tertiaryColor)
                                        val primaryHex = String.format("%06X", (primaryColor.toArgb() and 0xFFFFFF))
                                        val secondaryHex = String.format("%06X", (secondaryColor.toArgb() and 0xFFFFFF))
                                        val tertiaryHex = String.format("%06X", (tertiaryColor.toArgb() and 0xFFFFFF))
                                        val customScheme = "custom_${primaryHex}_${secondaryHex}_${tertiaryHex}"
                                        appSettings.setCustomColorScheme(customScheme)
                                        Toast.makeText(context, "Custom colors applied!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Apply")
                                }
                            }
                        }

                        // Bottom padding
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewItem(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = color,
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            } else {
                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            },
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorPickerControls(
    color: Color,
    onColorChange: (Color) -> Unit
) {
    val hsl = remember(color) { color.toHSL() }

    var hue by remember(color) { mutableStateOf(hsl.hue) }
    var saturation by remember(color) { mutableStateOf(hsl.saturation) }
    var lightness by remember(color) { mutableStateOf(hsl.lightness) }

    var showAdvanced by remember { mutableStateOf(false) }

    // Update color when HSL values change
    LaunchedEffect(hue, saturation, lightness) {
        val newColor = HSLColor(hue, saturation, lightness).toColor()
        onColorChange(newColor)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Current color display with hex code
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("#%06X", (color.toArgb() and 0xFFFFFF)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.9f) else Color.White,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hue Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hue",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "${hue.toInt()}°",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hue slider with color gradient
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = (0..360 step 20).map { h ->
                                HSLColor(h.toFloat(), 1f, 0.5f).toColor()
                            }
                        )
                    )
            ) {
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saturation Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saturation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "${(saturation * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.LightGray,
                                HSLColor(hue, 1f, lightness).toColor()
                            )
                        )
                    )
            ) {
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lightness Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lightness",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "${(lightness * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                HSLColor(hue, saturation, 0.5f).toColor(),
                                Color.White
                            )
                        )
                    )
            ) {
                Slider(
                    value = lightness,
                    onValueChange = { lightness = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Advanced RGB controls toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Advanced RGB Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = showAdvanced,
                onCheckedChange = { showAdvanced = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        // Advanced RGB controls
        AnimatedVisibility(
            visible = showAdvanced,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                val red = (color.red * 255).toInt()
                val green = (color.green * 255).toInt()
                val blue = (color.blue * 255).toInt()

                var redValue by remember(color) { mutableStateOf(red.toFloat()) }
                var greenValue by remember(color) { mutableStateOf(green.toFloat()) }
                var blueValue by remember(color) { mutableStateOf(blue.toFloat()) }

                // Update HSL when RGB changes
                LaunchedEffect(redValue, greenValue, blueValue) {
                    val rgbColor = Color(
                        red = redValue / 255f,
                        green = greenValue / 255f,
                        blue = blueValue / 255f
                    )
                    val newHsl = rgbColor.toHSL()
                    hue = newHsl.hue
                    saturation = newHsl.saturation
                    lightness = newHsl.lightness
                }

                ColorSlider(
                    label = "Red",
                    value = redValue,
                    onValueChange = { redValue = it },
                    color = Color.Red,
                    valueRange = 0f..255f
                )

                Spacer(modifier = Modifier.height(16.dp))

                ColorSlider(
                    label = "Green",
                    value = greenValue,
                    onValueChange = { greenValue = it },
                    color = Color.Green,
                    valueRange = 0f..255f
                )

                Spacer(modifier = Modifier.height(16.dp))

                ColorSlider(
                    label = "Blue",
                    value = blueValue,
                    onValueChange = { blueValue = it },
                    color = Color.Blue,
                    valueRange = 0f..255f
                )
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = value.toInt().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.3f),
                activeTickColor = color,
                inactiveTickColor = color.copy(alpha = 0.3f)
            )
        )
    }
}

private enum class ColorType {
    PRIMARY, SECONDARY, TERTIARY
}

// Font Selection Dialog for Theme Customization
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    fontOptions: List<FontOption>,
    currentFont: String,
    selectedFontSource: FontSource,
    onFontSelected: (String) -> Unit,
    appSettings: AppSettings,
    context: Context,
    haptic: HapticFeedback
) {
    if (showDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        // Animation states
        var showContent by remember { mutableStateOf(false) }

        val contentAlpha by animateFloatAsState(
            targetValue = if (showContent) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "contentAlpha"
        )

        LaunchedEffect(Unit) {
            delay(100)
            showContent = true
        }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .graphicsLayer(alpha = contentAlpha)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Font Selection",
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
                                text = "Choose from available system fonts",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedFontSource != FontSource.SYSTEM) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "System fonts are only available when using System font source.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Switch to System in the Font Source setting to choose from available fonts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(fontOptions) { option ->
                            FontCard(
                                option = option,
                                isSelected = currentFont == option.name,
                                onSelect = {
                                    HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                                    onFontSelected(option.name)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FontCard(
    option: FontOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font preview text
            Surface(
                color = if (isSelected)
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "The quick brown fox jumps over the lazy dog",
                    style = getFontPreviewStyle(option.name),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// Particle Intensity Dialog for Theme Customization
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticleIntensityDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentIntensity: Float,
    onIntensityChanged: (Float) -> Unit,
    appSettings: AppSettings,
    context: Context,
    haptic: HapticFeedback
) {
    if (showDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        // Animation states
        var showContent by remember { mutableStateOf(false) }

        val contentAlpha by animateFloatAsState(
            targetValue = if (showContent) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "contentAlpha"
        )

        LaunchedEffect(Unit) {
            delay(100)
            showContent = true
        }
        
        var intensity by remember { mutableStateOf(currentIntensity) }

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .graphicsLayer(alpha = contentAlpha)
            ) {
                // Header 
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Particle Intensity",
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
                                text = "Control particle effect density",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(16.dp))
//
                Column(modifier = Modifier.fillMaxWidth()) {
//                    Text(
//                        text = "Adjust the intensity of festive particles. Higher values create more particles.",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.padding(bottom = 24.dp)
//                    )
//
//                    // Current intensity display
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Intensity: ${(intensity * 100).toInt()}%",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.SemiBold,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Intensity slider
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Particle Density",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${(intensity * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Slider(
                                value = intensity,
                                onValueChange = { intensity = it },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtle",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Intense",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Buttons
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.LongPress)
                                onIntensityChanged(intensity)
                                appSettings.setFestiveThemeParticleIntensity(intensity)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

// API Management Screen
@Composable
fun ApiManagementSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val appSettings = AppSettings.getInstance(context)
    val scope = rememberCoroutineScope()
    
    // API states
    val deezerApiEnabled by appSettings.deezerApiEnabled.collectAsState()
    val canvasApiEnabled by appSettings.canvasApiEnabled.collectAsState()
    val lrclibApiEnabled by appSettings.lrclibApiEnabled.collectAsState()
    val ytMusicApiEnabled by appSettings.ytMusicApiEnabled.collectAsState()
    val spotifyApiEnabled by appSettings.spotifyApiEnabled.collectAsState()
    val spotifyClientId by appSettings.spotifyClientId.collectAsState()
    val spotifyClientSecret by appSettings.spotifyClientSecret.collectAsState()
    val appleMusicApiEnabled by appSettings.appleMusicApiEnabled.collectAsState()
    
    // Spotify API dialog state
    var showSpotifyConfigDialog by remember { mutableStateOf(false) }

    CollapsibleHeaderScreen(
        title = "API Management",
        showBackButton = true,
        onBackClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackClick()
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
           
            
            // API Services
            item {
                Text(
                    text = "External Services",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        // Deezer API
                        ApiServiceRow(
                            title = "Deezer",
                            description = "Free artist images and album artwork - no setup needed",
                            status = "Ready",
                            isConfigured = true,
                            isEnabled = deezerApiEnabled,
                            icon = Icons.Default.Public,
                            showToggle = true,
                            onToggle = { enabled -> appSettings.setDeezerApiEnabled(enabled) },
                            onClick = { /* No configuration needed */ }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Spotify Canvas API
                        ApiServiceRow(
                            title = "Spotify Canvas",
                            description = if (spotifyClientId.isNotEmpty() && spotifyClientSecret.isNotEmpty()) {
                                "Spotify integration for Canvas videos (High Data Usage)"
                            } else {
                                "Canvas videos from Spotify (Please use your own key!)"
                            },
                            status = if (spotifyClientId.isNotEmpty() && spotifyClientSecret.isNotEmpty()) {
                                "Active"
                            } else {
                                "Need Setup"
                            },
                            isConfigured = true,
                            isEnabled = canvasApiEnabled && (spotifyApiEnabled || true),
                            icon = RhythmIcons.Song,
                            showToggle = true,
                            onToggle = { enabled -> 
                                appSettings.setCanvasApiEnabled(enabled)
                                // Auto-clear canvas cache when disabled
                                if (!enabled) {
                                    scope.launch {
                                        try {
                                            val canvasRepository = chromahub.rhythm.app.data.CanvasRepository(context, appSettings)
                                            canvasRepository.clearCache()
                                            Log.d("ApiManagement", "Canvas cache cleared due to API being disabled")
                                        } catch (e: Exception) {
                                            Log.e("ApiManagement", "Error clearing canvas cache", e)
                                        }
                                    }
                                }
                            },
                            onClick = { 
                                showSpotifyConfigDialog = true 
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Apple Music API
                        ApiServiceRow(
                            title = "Apple Music",
                            description = "Word-by-word synchronized lyrics (Highest Quality)",
                            status = "Ready",
                            isConfigured = true,
                            isEnabled = appleMusicApiEnabled,
                            icon = RhythmIcons.Queue,
                            showToggle = true,
                            onToggle = { enabled -> appSettings.setAppleMusicApiEnabled(enabled) },
                            onClick = { /* No configuration needed */ }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // LRCLib API
                        ApiServiceRow(
                            title = "LRCLib",
                            description = "Free line-by-line synced lyrics (Fallback)",
                            status = "Ready",
                            isConfigured = true,
                            isEnabled = lrclibApiEnabled,
                            icon = RhythmIcons.Queue,
                            showToggle = true,
                            onToggle = { enabled -> appSettings.setLrcLibApiEnabled(enabled) },
                            onClick = { /* No configuration needed */ }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // YouTube Music API
                        ApiServiceRow(
                            title = "YouTube Music",
                            description = "Fallback for artist images and album artwork",
                            status = "Ready",
                            isConfigured = true,
                            isEnabled = ytMusicApiEnabled,
                            icon = RhythmIcons.Album,
                            showToggle = true,
                            onToggle = { enabled -> appSettings.setYTMusicApiEnabled(enabled) },
                            onClick = { /* No configuration needed */ }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // GitHub API
                        ApiServiceRow(
                            title = "GitHub",
                            description = "App updates and release information",
                            status = "Ready",
                            isConfigured = true,
                            isEnabled = true, // Always enabled for updates
                            icon = RhythmIcons.Download,
                            showToggle = false, // Can't disable update checks
                            onClick = { /* No configuration needed */ }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }

             item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "API Services",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        Text(
                            text = "Control which external API services are active to enhance your music experience. Toggle services on/off to manage data usage and functionality.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
    
    // Spotify API Configuration Dialog
    if (showSpotifyConfigDialog) {
        SpotifyApiConfigDialog(
            currentClientId = spotifyClientId,
            currentClientSecret = spotifyClientSecret,
            onDismiss = { showSpotifyConfigDialog = false },
            onSave = { clientId, clientSecret ->
                appSettings.setSpotifyClientId(clientId)
                appSettings.setSpotifyClientSecret(clientSecret)
                // Auto-enable API if credentials are provided
                if (clientId.isNotEmpty() && clientSecret.isNotEmpty()) {
                    appSettings.setSpotifyApiEnabled(true)
                }
                showSpotifyConfigDialog = false
            },
            appSettings = appSettings
        )
    }
}

// Equalizer Settings Screen
@Composable
fun EqualizerSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val appSettings = AppSettings.getInstance(context)

    CollapsibleHeaderScreen(
        title = "Equalizer",
        showBackButton = true,
        onBackClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackClick()
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Audio Equalization",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Equalizer controls will be implemented here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        // TODO: Implement equalizer controls
                        Text(
                            text = "Coming Soon",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Sleep Timer Settings Screen
@Composable
fun SleepTimerSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val appSettings = AppSettings.getInstance(context)

    CollapsibleHeaderScreen(
        title = "Sleep Timer",
        showBackButton = true,
        onBackClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackClick()
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sleep timer controls will be implemented here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        // TODO: Implement sleep timer controls
                        Text(
                            text = "Coming Soon",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Crash Log History Settings Screen
@Composable
fun CrashLogHistorySettingsScreen(onBackClick: () -> Unit, appSettings: AppSettings) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    CollapsibleHeaderScreen(
        title = "Crash Log History",
        showBackButton = true,
        onBackClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackClick()
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Crash Reports",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Crash log history will be displayed here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        // TODO: Implement crash log display
                        Text(
                            text = "Coming Soon",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeTipItem(
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
            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
