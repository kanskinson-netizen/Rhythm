package chromahub.rhythm.app.widget.glance

import android.content.Context
import androidx.glance.appwidget.updateAll
import chromahub.rhythm.app.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Utility object for updating the Glance-based widget
 * 
 * This handles updating widget state when playback changes
 */
object GlanceWidgetUpdater {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    /**
     * Update widget with current playback state
     */
    fun updateWidget(
        context: Context,
        song: Song?,
        isPlaying: Boolean,
        hasPrevious: Boolean = false,
        hasNext: Boolean = false
    ) {
        // Update SharedPreferences for widget data synchronously
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (song != null) {
                putString(RhythmMusicWidget.KEY_SONG_TITLE, song.title)
                putString(RhythmMusicWidget.KEY_ARTIST_NAME, song.artist)
                putString(RhythmMusicWidget.KEY_ALBUM_NAME, song.album)
                putString(RhythmMusicWidget.KEY_ARTWORK_URI, song.artworkUri?.toString())
            } else {
                putString(RhythmMusicWidget.KEY_SONG_TITLE, "No song playing")
                putString(RhythmMusicWidget.KEY_ARTIST_NAME, "Unknown artist")
                putString(RhythmMusicWidget.KEY_ALBUM_NAME, "")
                remove(RhythmMusicWidget.KEY_ARTWORK_URI)
            }
            putBoolean(RhythmMusicWidget.KEY_IS_PLAYING, isPlaying)
            putBoolean(RhythmMusicWidget.KEY_HAS_PREVIOUS, hasPrevious)
            putBoolean(RhythmMusicWidget.KEY_HAS_NEXT, hasNext)
            commit() // Use commit for immediate write
        }
        
        // Trigger widget update immediately
        scope.launch {
            try {
                RhythmMusicWidget().updateAll(context)
            } catch (e: Exception) {
                android.util.Log.e("GlanceWidgetUpdater", "Error updating widget", e)
            }
        }
    }
    
    /**
     * Update widget to show "No song playing" state
     */
    fun updateWidgetEmpty(context: Context) {
        updateWidget(
            context = context,
            song = null,
            isPlaying = false,
            hasPrevious = false,
            hasNext = false
        )
    }
    
    /**
     * Force update all widgets
     */
    fun forceUpdateAll(context: Context) {
        scope.launch {
            try {
                RhythmMusicWidget().updateAll(context)
            } catch (e: Exception) {
                android.util.Log.e("GlanceWidgetUpdater", "Error forcing widget update", e)
            }
        }
    }
}
