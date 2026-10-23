package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoLocation
import com.example.ui.theme.*

@Composable
fun InteractiveRouteMap(
    origin: GeoLocation,
    destination: GeoLocation,
    waypoints: List<GeoLocation> = emptyList(),
    pickupPoint: GeoLocation? = null,
    dropoffPoint: GeoLocation? = null,
    detourKm: Double = 0.0,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isDark = MaterialTheme.colorScheme.background == NavyDark
    val mapBgColor = if (isDark) SurfaceDark else Color(0xFFF1F5F9)
    val gridColor = if (isDark) BorderDark.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
    val primaryRouteColor = PrimaryBlue
    val detourColor = ElectricCyanGlow

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(mapBgColor)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Decorative subtle grid
            val gridSpacing = 36f
            var x = 0f
            while (x < w) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            var y = 0f
            while (y < h) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // Route Coordinates mapped across canvas
            val startP = Offset(w * 0.15f, h * 0.78f)
            val wp1 = Offset(w * 0.38f, h * 0.58f)
            val wp2 = Offset(w * 0.62f, h * 0.38f)
            val endP = Offset(w * 0.85f, h * 0.20f)

            // 2. Corridor buffer corridor zone (shaded buffer)
            val corridorPath = Path().apply {
                moveTo(startP.x - 18, startP.y + 12)
                cubicTo(wp1.x - 20, wp1.y + 10, wp2.x - 16, wp2.y + 8, endP.x - 14, endP.y + 12)
                lineTo(endP.x + 14, endP.y - 12)
                cubicTo(wp2.x + 16, wp2.y - 8, wp1.x + 20, wp1.y - 10, startP.x + 18, startP.y - 12)
                close()
            }
            drawPath(
                path = corridorPath,
                color = primaryRouteColor.copy(alpha = 0.10f)
            )

            // 3. Primary Route Line
            val mainPath = Path().apply {
                moveTo(startP.x, startP.y)
                cubicTo(wp1.x, wp1.y + 10, wp2.x, wp2.y - 10, endP.x, endP.y)
            }
            drawPath(
                path = mainPath,
                color = primaryRouteColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // 4. Detour Route Line (if detour > 0)
            if (detourKm > 0.0) {
                val detourPoint = Offset(w * 0.48f, h * 0.26f)
                val detourPath = Path().apply {
                    moveTo(wp1.x, wp1.y)
                    lineTo(detourPoint.x, detourPoint.y)
                    lineTo(wp2.x, wp2.y)
                }
                drawPath(
                    path = detourPath,
                    color = detourColor,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // Draw Detour Waypoint Marker
                drawCircle(
                    color = detourColor.copy(alpha = pulseAlpha),
                    radius = 12f,
                    center = detourPoint
                )
                drawCircle(
                    color = detourColor,
                    radius = 6f,
                    center = detourPoint
                )
            }

            // 5. Waypoints along path
            listOf(wp1, wp2).forEach { wp ->
                drawCircle(
                    color = primaryRouteColor,
                    radius = 5f,
                    center = wp
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = wp
                )
            }

            // 6. Origin Node (Green)
            drawCircle(
                color = StatusSuccess.copy(alpha = 0.3f),
                radius = 16f,
                center = startP
            )
            drawCircle(
                color = StatusSuccess,
                radius = 8f,
                center = startP
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = startP
            )

            // 7. Destination Node (Primary Blue)
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.35f),
                radius = 16f,
                center = endP
            )
            drawCircle(
                color = PrimaryBlue,
                radius = 8f,
                center = endP
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = endP
            )
        }

        // Overlay: Origin & Destination Pill Cards
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StatusSuccess)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = origin.city,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = " → ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = destination.city,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Overlay: Detour indicator pill
        if (detourKm > 0.0) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AltRoute,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${detourKm}km corridor deviation",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )
            }
        }
    }
}
