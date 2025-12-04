package chromahub.rhythm.app.ui.theme.festive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Complete Christmas decorations including lights, garland, and snow collection
 * Supports enabling/disabling individual decoration elements by position
 */
@Composable
fun ChristmasDecorations(
    intensity: Float = 0.5f,
    showTopLights: Boolean = true,
    showSideGarland: Boolean = true,
    showBottomSnow: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Top Christmas lights - below status bar
        if (showTopLights) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            ) {
                ChristmasLights(intensity = intensity)
            }
        }
        
        // Side decorations (garland with ornaments)
        if (showSideGarland) {
            SideDecorations(intensity = intensity)
        }
        
        // Bottom snow collection - anchored to bottom
        if (showBottomSnow) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                SnowCollection(intensity = intensity)
            }
        }
    }
}

/**
 * Christmas lights string at the top
 */
@Composable
fun ChristmasLights(
    intensity: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    
    // Blinking animation for lights
    val infiniteTransition = rememberInfiniteTransition(label = "lights")
    val blinkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blinkPhase"
    )
    
    // Generate light positions
    val lights = remember(screenWidth, intensity) {
        val count = (screenWidth / 80f * (0.5f + intensity * 0.5f)).toInt().coerceIn(8, 20)
        List(count) { index ->
            val x = (screenWidth / count) * index + (screenWidth / count / 2)
            val colorIndex = index % 4
            val color = when (colorIndex) {
                0 -> Color(0xFFFF4444) // Red
                1 -> Color(0xFF44FF44) // Green
                2 -> Color(0xFFFFFF44) // Yellow
                3 -> Color(0xFF4444FF) // Blue
                else -> Color.White
            }
            Triple(x, color, index * 90f) // x, color, phase offset
        }
    }
    
    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        // Draw wavy wire/string
        val wireY = 40f
        val waveAmplitude = 8f
        val waveFrequency = 3f
        val path = Path().apply {
            moveTo(0f, wireY)
            var x = 0f
            while (x <= size.width) {
                val y = wireY + sin((x / size.width) * waveFrequency * 2 * PI).toFloat() * waveAmplitude
                lineTo(x, y)
                x += 10f
            }
        }
        
        drawPath(
            path = path,
            color = Color(0xFF2C5530).copy(alpha = 0.6f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
        
        // Draw lights hanging from wavy wire
        lights.forEach { (x, color, phaseOffset) ->
            val brightness = (sin((blinkPhase + phaseOffset) * PI / 180) * 0.3f + 0.7f).toFloat()
            val wireYAtX = wireY + sin((x / size.width) * waveFrequency * 2 * PI).toFloat() * waveAmplitude
            
            // Wire connection
            drawLine(
                color = Color(0xFF2C5530).copy(alpha = 0.6f),
                start = Offset(x, wireYAtX),
                end = Offset(x, wireYAtX + 15f),
                strokeWidth = 2f
            )
            
            // Light bulb
            drawCircle(
                color = color.copy(alpha = brightness),
                radius = 12f,
                center = Offset(x, wireYAtX + 25f)
            )
            
            // Glow effect
            drawCircle(
                color = color.copy(alpha = brightness * 0.3f),
                radius = 18f,
                center = Offset(x, wireYAtX + 25f)
            )
        }
    }
}

/**
 * Side decorations - Simple ornaments and garland style
 */
@Composable
fun SideDecorations(
    intensity: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    
    // Gentle sway animation
    val infiniteTransition = rememberInfiniteTransition(label = "sideDecorations")
    val sway by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )
    
    // Generate simple ornament decorations with random distances
    val decorations = remember(screenHeight, intensity) {
        val count = (8 + (intensity * 5).toInt()).coerceIn(8, 15)
        val startY = 180f
        val endY = screenHeight - 300f
        
        List(count) { index ->
            // Random Y position instead of evenly spaced
            val y = startY + Random.nextFloat() * (endY - startY)
            // Larger size variation 1.5-2.5x
            val size = 1.5f + Random.nextFloat() * 1.0f
            val colorIndex = index % 5
            val color = when (colorIndex) {
                0 -> Color(0xFFE63946) // Red
                1 -> Color(0xFFFFD700) // Gold
                2 -> Color(0xFF2A9D8F) // Teal/Green
                3 -> Color(0xFF4169E1) // Blue
                4 -> Color(0xFFDC143C) // Crimson
                else -> Color(0xFFFF6B6B)
            }
            Triple(y, size, color)
        }.sortedBy { it.first } // Sort by Y position for better visual flow
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val leftX = 30f  // More inward for better visibility
        val rightX = size.width - 30f
        
        // Left side - vertical garland with ornaments
        drawVerticalGarland(leftX, 150f, screenHeight - 280f, sway)
        
        // Add larger bow at top of left garland
        drawBow(Offset(leftX + sway * 0.3f, 130f), Color(0xFFE63946), 2.0f)
        
        decorations.forEachIndexed { index, (y, scale, color) ->
            val swayOffset = sway * (0.5f + scale * 0.5f)
            
            // Alternate between ornaments and other decorations
            when (index % 4) {
                0, 1 -> drawSimpleOrnament(
                    Offset(leftX + swayOffset, y),
                    color,
                    scale
                )
                2 -> drawCandyCane(
                    Offset(leftX + swayOffset, y),
                    scale * 0.8f
                )
                3 -> drawHolly(
                    Offset(leftX + swayOffset, y),
                    scale * 0.9f
                )
            }
        }
        
        // Right side - vertical garland with ornaments
        drawVerticalGarland(rightX, 150f, screenHeight - 280f, -sway)
        
        // Add larger bow at top of right garland
        drawBow(Offset(rightX - sway * 0.3f, 130f), Color(0xFFFFD700), 2.0f)
        
        decorations.forEachIndexed { index, (y, scale, _) ->
            val colorIndex = (index + 2) % 5
            val color = when (colorIndex) {
                0 -> Color(0xFFE63946)
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFF2A9D8F)
                3 -> Color(0xFF4169E1)
                4 -> Color(0xFFDC143C)
                else -> Color(0xFFFF6B6B)
            }
            val swayOffset = -sway * (0.5f + scale * 0.5f)
            
            // Alternate between ornaments and other decorations
            when (index % 4) {
                0, 1 -> drawSimpleOrnament(
                    Offset(rightX + swayOffset, y),
                    color,
                    scale
                )
                2 -> drawCandyCane(
                    Offset(rightX + swayOffset, y),
                    scale * 0.8f
                )
                3 -> drawHolly(
                    Offset(rightX + swayOffset, y),
                    scale * 0.9f
                )
            }
        }
    }
}

/**
 * Draw a vertical garland strand
 */
private fun DrawScope.drawVerticalGarland(x: Float, startY: Float, endY: Float, sway: Float) {
    val segments = 30
    val path = Path()
    path.moveTo(x, startY)
    
    for (i in 0..segments) {
        val progress = i.toFloat() / segments
        val y = startY + (endY - startY) * progress
        val swayAmount = sin(progress * PI * 4).toFloat() * 8f + sway * 0.3f
        path.lineTo(x + swayAmount, y)
    }
    
    // Dark green garland strand
    drawPath(
        path = path,
        color = Color(0xFF1B4332),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )
}

/**
 * Draw a decorative bow
 */
private fun DrawScope.drawBow(center: Offset, color: Color, scale: Float = 1f) {
    val size = 18f * scale
    
    // Left loop
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0.7f)),
            center = Offset(center.x - size * 0.3f, center.y),
            radius = size * 0.4f
        ),
        radius = size * 0.4f,
        center = Offset(center.x - size * 0.3f, center.y)
    )
    
    // Right loop
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0.7f)),
            center = Offset(center.x + size * 0.3f, center.y),
            radius = size * 0.4f
        ),
        radius = size * 0.4f,
        center = Offset(center.x + size * 0.3f, center.y)
    )
    
    // Center knot
    drawCircle(
        color = color,
        radius = size * 0.25f,
        center = center
    )
    
    // Ribbons hanging down
    val ribbon1 = Path().apply {
        moveTo(center.x - size * 0.15f, center.y + size * 0.2f)
        lineTo(center.x - size * 0.1f, center.y + size * 0.8f)
        lineTo(center.x, center.y + size * 0.7f)
        close()
    }
    drawPath(ribbon1, color = color.copy(alpha = 0.8f))
    
    val ribbon2 = Path().apply {
        moveTo(center.x + size * 0.15f, center.y + size * 0.2f)
        lineTo(center.x + size * 0.1f, center.y + size * 0.8f)
        lineTo(center.x, center.y + size * 0.7f)
        close()
    }
    drawPath(ribbon2, color = color.copy(alpha = 0.8f))
}

/**
 * Draw a candy cane
 */
private fun DrawScope.drawCandyCane(center: Offset, scale: Float = 1f) {
    val height = 20f * scale
    val width = 6f * scale
    
    // Draw candy cane shape
    val path = Path().apply {
        moveTo(center.x - width / 2, center.y + height * 0.5f)
        lineTo(center.x - width / 2, center.y - height * 0.2f)
        // Hook at top
        cubicTo(
            center.x - width / 2, center.y - height * 0.5f,
            center.x + width * 1.5f, center.y - height * 0.5f,
            center.x + width * 1.5f, center.y - height * 0.2f
        )
        lineTo(center.x + width / 2, center.y - height * 0.2f)
        // Inner curve
        cubicTo(
            center.x + width / 2, center.y - height * 0.35f,
            center.x + width / 2, center.y - height * 0.35f,
            center.x + width / 2, center.y - height * 0.2f
        )
        lineTo(center.x + width / 2, center.y + height * 0.5f)
        close()
    }
    
    // White background
    drawPath(path, color = Color.White)
    
    // Red stripes
    for (i in 0..4) {
        val y = center.y + height * 0.5f - (i * height * 0.2f)
        if (y > center.y - height * 0.2f) {
            drawRect(
                color = Color(0xFFE63946),
                topLeft = Offset(center.x - width / 2, y - height * 0.08f),
                size = androidx.compose.ui.geometry.Size(width, height * 0.08f)
            )
        }
    }
}

/**
 * Draw holly leaves and berries
 */
private fun DrawScope.drawHolly(center: Offset, scale: Float = 1f) {
    val leafSize = 12f * scale
    
    // Left leaf
    val leftLeaf = Path().apply {
        moveTo(center.x - leafSize * 0.3f, center.y)
        cubicTo(
            center.x - leafSize * 0.8f, center.y - leafSize * 0.3f,
            center.x - leafSize * 0.9f, center.y + leafSize * 0.3f,
            center.x - leafSize * 0.3f, center.y
        )
    }
    drawPath(leftLeaf, color = Color(0xFF2D5016))
    
    // Right leaf
    val rightLeaf = Path().apply {
        moveTo(center.x + leafSize * 0.3f, center.y)
        cubicTo(
            center.x + leafSize * 0.8f, center.y - leafSize * 0.3f,
            center.x + leafSize * 0.9f, center.y + leafSize * 0.3f,
            center.x + leafSize * 0.3f, center.y
        )
    }
    drawPath(rightLeaf, color = Color(0xFF2D5016))
    
    // Red berries
    drawCircle(
        color = Color(0xFFE63946),
        radius = 3f * scale,
        center = Offset(center.x - leafSize * 0.15f, center.y - leafSize * 0.3f)
    )
    drawCircle(
        color = Color(0xFFE63946),
        radius = 3f * scale,
        center = Offset(center.x + leafSize * 0.15f, center.y - leafSize * 0.3f)
    )
    drawCircle(
        color = Color(0xFFDC143C),
        radius = 3f * scale,
        center = Offset(center.x, center.y - leafSize * 0.4f)
    )
}

/**
 * Draw a simple ornament ball
 */
private fun DrawScope.drawSimpleOrnament(center: Offset, color: Color, scale: Float = 1f) {
    val radius = 12f * scale
    
    // Main ornament ball with gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 1f),
                color.copy(alpha = 0.8f)
            ),
            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
            radius = radius
        ),
        radius = radius,
        center = center
    )
    
    // Shine effect
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = 4f * scale,
        center = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f)
    )
    
    // Subtle outline
    drawCircle(
        color = color.copy(alpha = 0.5f),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5f)
    )
}

/**
 * Snow collection at the bottom
 */
@Composable
fun SnowCollection(
    intensity: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    
    // Generate snow pile pattern
    val snowPile = remember(screenWidth, intensity) {
        val points = 50
        List(points) { index ->
            val x = (screenWidth / points) * index
            val baseHeight = 30f + (intensity * 40f)
            val variation = (sin((index.toFloat() / points) * PI * 4) * 15f).toFloat()
            Offset(x, baseHeight + variation)
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height((80f + intensity * 40f).dp)
    ) {
        // Draw snow pile
        val path = Path().apply {
            moveTo(0f, size.height)
            snowPile.forEach { point ->
                lineTo(point.x, size.height - point.y)
            }
            lineTo(size.width, size.height)
            close()
        }
        
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFAFA),
                    Color(0xFFF0F8FF)
                )
            )
        )
        
        // Add sparkles on snow
        val sparkleCount = (screenWidth / 100f * intensity).toInt()
        repeat(sparkleCount) { index ->
            val x = Random.nextFloat() * size.width
            val y = size.height - (30f + Random.nextFloat() * 40f * intensity)
            
            // Small sparkle
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 2f,
                center = Offset(x, y)
            )
            
            // Glow
            drawCircle(
                color = Color(0xFFE0F2FF).copy(alpha = 0.4f),
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}
