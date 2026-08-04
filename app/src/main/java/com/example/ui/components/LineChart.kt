package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class LineChartLine(
    val values: List<Float?>,
    val color: Color,
    val label: String
)

@Composable
fun LineChartCanvas(
    lines: List<LineChartLine>,
    maxY: Float,
    minY: Float = 0f,
    yStep: Float,
    yLabelFormatter: (Float) -> String,
    xLabels: List<String>,
    dateStrs: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    gradientFill: Boolean = false,
    trailingAvg: Float? = null
) {
    var tooltipIndex by remember { mutableStateOf<Int?>(null) }
    val labelPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    val dataCount = lines.maxOfOrNull { it.values.size } ?: return

    if (dataCount == 0) {
        Text(
            text = "No data for this period.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(lines) {
                    detectTapGestures { offset ->
                        if (dataCount < 2) return@detectTapGestures
                        val leftPad = 40.dp.toPx()
                        val drawW = size.width - leftPad - 16.dp.toPx()
                        val stepW = drawW / (dataCount - 1)
                        val idx = ((offset.x - leftPad) / stepW).roundToInt()
                            .coerceIn(0, dataCount - 1)
                        tooltipIndex = if (tooltipIndex == idx) null else idx
                    }
                }
        ) {
            val leftPad = 40.dp.toPx()
            val rightPad = 16.dp.toPx()
            val topPad = 8.dp.toPx()
            val bottomPad = 24.dp.toPx()
            val drawW = size.width - leftPad - rightPad
            val drawH = size.height - topPad - bottomPad
            val rangeY = (maxY - minY).coerceAtLeast(0.01f)

            var yVal = minY
            while (yVal <= maxY + 0.001f) {
                val yPos = topPad + drawH - ((yVal - minY) / rangeY * drawH)
                drawLine(
                    color = surfaceVariant,
                    start = Offset(leftPad, yPos),
                    end = Offset(size.width - rightPad, yPos),
                    strokeWidth = 1.dp.toPx()
                )
                labelPaint.color = onSurfaceVariant.hashCode()
                labelPaint.textSize = 9.sp.toPx()
                drawContext.canvas.nativeCanvas.drawText(
                    yLabelFormatter(yVal),
                    leftPad - 8.dp.toPx(),
                    yPos + 3.dp.toPx(),
                    labelPaint
                )
                yVal += yStep
            }

            val allPoints = lines.map { line ->
                line.values.mapIndexed { idx, v ->
                    if (v != null) {
                        val x = leftPad + (idx.toFloat() / (dataCount - 1).coerceAtLeast(1)) * drawW
                        val y = topPad + drawH - ((v - minY) / rangeY * drawH)
                        Offset(x, y)
                    } else null
                }
            }

            if (dataCount >= 2) {
                if (gradientFill && lines.isNotEmpty() && allPoints.isNotEmpty()) {
                    val pts = allPoints[0]
                    if (pts.all { it != null }) {
                        val fillPath = Path().apply {
                            val first = pts.first()!!
                            moveTo(first.x, topPad + drawH)
                            pts.forEach { p ->
                                if (p != null) lineTo(p.x, p.y)
                            }
                            val last = pts.last()!!
                            lineTo(last.x, topPad + drawH)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lines[0].color.copy(alpha = 0.25f), Color.Transparent),
                                endY = topPad + drawH
                            )
                        )
                    }
                }

                lines.forEachIndexed { lineIdx, line ->
                    val pts = allPoints[lineIdx]
                    val isSecondary = lineIdx > 0
                    for (i in 0 until dataCount - 1) {
                        val p1 = pts[i]
                        val p2 = pts[i + 1]
                        if (p1 != null && p2 != null) {
                            drawLine(
                                color = line.color,
                                start = p1,
                                end = p2,
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = if (isSecondary) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null
                            )
                        }
                    }
                }

                if (trailingAvg != null && dataCount >= 7) {
                    val avgStartIdx = dataCount - 7
                    val avgXStart = leftPad + (avgStartIdx.toFloat() / (dataCount - 1)) * drawW
                    val avgY = topPad + drawH - ((trailingAvg - minY) / rangeY * drawH)
                    drawLine(
                        color = lines.first().color.copy(alpha = 0.5f),
                        start = Offset(avgXStart, avgY),
                        end = Offset(leftPad + drawW, avgY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )
                }
            }

            allPoints.forEachIndexed { lineIdx, pts ->
                val lineColor = lines[lineIdx].color
                pts.forEachIndexed { idx, pt ->
                    if (pt != null) {
                        val isSel = tooltipIndex == idx
                        val radius = if (isSel) 5.dp.toPx() else 3.dp.toPx()
                        drawCircle(color = lineColor, radius = radius, center = pt)
                        if (isSel) {
                            drawCircle(color = Color.White, radius = radius - 1.5.dp.toPx(), center = pt)
                            drawCircle(color = lineColor, radius = radius - 1.5.dp.toPx(), center = pt)
                        }
                    }
                }
            }

            val labelIndices = (0 until dataCount).filter { idx ->
                val day = xLabels.getOrElse(idx) { "" }
                idx == 0 || idx == dataCount - 1 || (day.toIntOrNull()?.let { d -> d % 5 == 0 || d == 1 } ?: false)
            }
            labelPaint.textSize = 9.sp.toPx()
            labelPaint.color = onSurfaceVariant.hashCode()
            labelIndices.forEach { idx ->
                val x = leftPad + (idx.toFloat() / (dataCount - 1).coerceAtLeast(1)) * drawW
                drawContext.canvas.nativeCanvas.drawText(
                    xLabels.getOrElse(idx) { "" },
                    x,
                    size.height - 4.dp.toPx(),
                    labelPaint
                )
            }

            tooltipIndex?.let { idx ->
                val firstNonNullLine = allPoints.indexOfFirst { pts -> pts.getOrNull(idx) != null }
                if (firstNonNullLine >= 0) {
                    val pt = allPoints[firstNonNullLine][idx]!!
                    val tipText = buildString {
                        append(dateStrs.getOrElse(idx) { "" })
                        lines.forEachIndexed { li, line ->
                            val v = line.values.getOrNull(idx)
                            if (v != null) {
                                append("  ${line.label}: ${yLabelFormatter(v)}")
                            }
                        }
                    }
                    labelPaint.textSize = 10.sp.toPx()
                    labelPaint.color = android.graphics.Color.WHITE
                    val textW = labelPaint.measureText(tipText)
                    val tipW = textW + 12.dp.toPx()
                    val tipH = 22.dp.toPx()
                    val tipX = (pt.x - tipW / 2f)
                        .coerceIn(4.dp.toPx(), size.width - tipW - 4.dp.toPx())
                    val tipY = pt.y - 12.dp.toPx() - tipH

                    drawRoundRect(
                        color = Color(0xDD333333),
                        topLeft = Offset(tipX, tipY),
                        size = Size(tipW, tipH),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        tipText,
                        tipX + tipW / 2f,
                        tipY + tipH - 5.dp.toPx(),
                        labelPaint
                    )
                }
            }
        }
    }
}
