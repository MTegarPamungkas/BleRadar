package com.example.bleradar.presentation.feature.radar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.bleradar.core.designsystem.color
import com.example.bleradar.domain.model.SignalZone
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val MAX_RANGE_METERS = 30.0
private val RingDistances = listOf(1.0, 3.0, 10.0, 20.0, 30.0)

private fun radiusFraction(distanceMeters: Double): Float =
    sqrt(distanceMeters / MAX_RANGE_METERS).toFloat().coerceIn(0.1f, 1f)

@Composable
fun RadarView(
    deviceId: String,
    distanceMeters: Double?,
    zone: SignalZone,
    modifier: Modifier = Modifier,
) {
    val zoneColor by animateColorAsState(zone.color(), tween(500))
    val fraction by animateFloatAsState(distanceMeters?.let(::radiusFraction) ?: 1f, tween(600))
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "pulse",
    )
    val ringColor = MaterialTheme.colorScheme.outlineVariant
    val centerColor = MaterialTheme.colorScheme.primary
    val angle = remember(deviceId) { (deviceId.hashCode().absoluteValue % 360) * PI / 180 }

    Canvas(modifier.aspectRatio(1f)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2

        RingDistances.forEach { distance ->
            drawCircle(
                color = ringColor,
                radius = maxRadius * radiusFraction(distance),
                center = center,
                style = Stroke(1.dp.toPx()),
            )
        }

        drawCircle(zoneColor.copy(alpha = 0.25f * (1f - pulse)), maxRadius * pulse, center)

        if (distanceMeters != null) {
            val radius = maxRadius * fraction
            val blip = Offset(
                x = center.x + (radius * cos(angle)).toFloat(),
                y = center.y + (radius * sin(angle)).toFloat(),
            )
            drawCircle(zoneColor.copy(alpha = 0.3f), 18.dp.toPx(), blip)
            drawCircle(zoneColor, 9.dp.toPx(), blip)
        }

        drawCircle(centerColor, 6.dp.toPx(), center)
    }
}

@Composable
fun RssiSparkline(samples: List<Int>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (samples.size < 2) return@Canvas
        val minRssi = -100f
        val maxRssi = -20f
        val stepX = size.width / (samples.size - 1)
        val path = Path()
        samples.forEachIndexed { index, rssi ->
            val y = size.height * (1f - ((rssi - minRssi) / (maxRssi - minRssi)).coerceIn(0f, 1f))
            val x = stepX * index
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}