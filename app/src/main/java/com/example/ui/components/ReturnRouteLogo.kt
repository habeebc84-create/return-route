package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PrimaryBlue

@Composable
fun ReturnRouteIcon(
    modifier: Modifier = Modifier.size(36.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer glow path
        val rPath = Path().apply {
            moveTo(w * 0.22f, h * 0.85f)
            lineTo(w * 0.22f, h * 0.18f)
            lineTo(w * 0.58f, h * 0.18f)
            cubicTo(
                w * 0.82f, h * 0.18f,
                w * 0.82f, h * 0.52f,
                w * 0.58f, h * 0.52f
            )
            lineTo(w * 0.22f, h * 0.52f)
        }

        drawPath(
            path = rPath,
            color = PrimaryBlue,
            style = Stroke(
                width = w * 0.14f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Diagonal return route leg
        drawLine(
            color = ElectricCyan,
            start = Offset(w * 0.50f, h * 0.52f),
            end = Offset(w * 0.82f, h * 0.85f),
            strokeWidth = w * 0.14f,
            cap = StrokeCap.Round
        )

        // Waypoint pin
        drawCircle(
            color = Color.White,
            radius = w * 0.08f,
            center = Offset(w * 0.72f, h * 0.75f)
        )
    }
}

@Composable
fun ReturnRouteBrandHeader(
    modifier: Modifier = Modifier,
    showTagline: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReturnRouteIcon(modifier = Modifier.size(34.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Return",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Route",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (showTagline) {
                Text(
                    text = "Smart Return-Trip Mobility",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
