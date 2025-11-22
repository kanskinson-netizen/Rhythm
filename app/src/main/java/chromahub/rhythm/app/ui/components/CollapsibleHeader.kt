package chromahub.rhythm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row // Added import for Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.ui.theme.RhythmTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleHeaderScreen(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {},
    filterDropdown: @Composable () -> Unit = {}, // New parameter for the filter dropdown
    scrollBehaviorKey: String? = null, // Key for preserving scroll behavior state
    content: @Composable (Modifier) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )


    val lazyListState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val collapsedFraction = scrollBehavior.state.collapsedFraction
            val fontSize = (24 + (32 - 24) * (1 - collapsedFraction)).sp // Interpolate between 24sp and 32sp
            val containerColor = Color.Transparent // Always transparent

            Column {
                Spacer(modifier = Modifier.height(5.dp)) // Add more padding before the header starts
                LargeTopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSize
                            ),
                            modifier = Modifier.padding(start = 14.dp) // Adjust start padding for title
                        )
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.padding(start = 8.dp) // Add padding to the back button
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp) // Adjust size as needed
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)), // Circular background
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            filterDropdown() // Place the filter dropdown here
                            actions()
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = containerColor,
                        scrolledContainerColor = containerColor
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollapsibleHeaderScreenPreview() {
    RhythmTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CollapsibleHeaderScreen(
                title = "Settings",
                showBackButton = true,
                onBackClick = { /* Handle back click in preview */ }
            ) { modifier ->
                LazyColumn(
                    modifier = modifier.padding(horizontal = 16.dp) // Consistent horizontal padding for content
                ) {
                    items(50) { index ->
                        Text(text = "Item $index", modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}
