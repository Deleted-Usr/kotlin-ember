package com.willclay.ember.engine.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.willclay.ember.engine.math.Vec2

/**
 * Thin drawing API handed to [com.willclay.ember.engine.core.Component.onDraw].
 * All coordinates are world space; the camera transform is already applied.
 * Stroke widths are in screen pixels so outlines stay crisp at any zoom.
 *
 * Need something not covered here? Use [scope] for raw Compose drawing.
 */
class Renderer(
    val scope: DrawScope,
    val camera: Camera2D,
    private val textMeasurer: TextMeasurer,
    /** True when editor gizmos (collider outlines, etc.) should be drawn. */
    val showGizmos: Boolean,
) {
    private fun px(width: Float) = width / camera.zoom

    fun rect(center: Vec2, size: Vec2, color: Color, rotation: Float = 0f, strokeWidth: Float? = null) =
        scope.rotate(rotation, center.toOffset()) {
            drawRect(
                color   = color,
                topLeft = Offset(center.x - size.x / 2f, center.y - size.y / 2f),
                size    = Size(size.x, size.y),
                style   = strokeWidth?.let { Stroke(px(it)) } ?: androidx.compose.ui.graphics.drawscope.Fill,
            )
        }

    fun ellipse(center: Vec2, size: Vec2, color: Color, rotation: Float = 0f, strokeWidth: Float? = null) =
        scope.rotate(rotation, center.toOffset()) {
            drawOval(
                color   = color,
                topLeft = Offset(center.x - size.x / 2f, center.y - size.y / 2f),
                size    = Size(size.x, size.y),
                style   = strokeWidth?.let { Stroke(px(it)) } ?: androidx.compose.ui.graphics.drawscope.Fill,
            )
        }

    fun circle(center: Vec2, radius: Float, color: Color, strokeWidth: Float? = null) =
        ellipse(center, Vec2(radius * 2, radius * 2), color, strokeWidth = strokeWidth)

    fun line(from: Vec2, to: Vec2, color: Color, width: Float = 1f) =
        scope.drawLine(color, from.toOffset(), to.toOffset(), px(width))

    fun outline(rect: Rect, color: Color, width: Float = 1f) =
        scope.drawRect(color, rect.topLeft, rect.size, style = Stroke(px(width)))

    fun text(text: String, position: Vec2, color: Color = Color.White, fontSize: Float = 16f) =
        scope.drawText(textMeasurer, text, position.toOffset(), TextStyle(color = color, fontSize = fontSize.sp))
}
