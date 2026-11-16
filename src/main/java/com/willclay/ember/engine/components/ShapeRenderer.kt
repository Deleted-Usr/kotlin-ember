package com.willclay.ember.engine.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.willclay.ember.engine.core.Bounded
import com.willclay.ember.engine.core.ChoiceProperty
import com.willclay.ember.engine.core.ColorProperty
import com.willclay.ember.engine.core.Component
import com.willclay.ember.engine.core.Property
import com.willclay.ember.engine.core.Vec2Property
import com.willclay.ember.engine.math.Vec2
import com.willclay.ember.engine.render.Renderer
import kotlin.math.abs

/** Draws a filled rectangle or ellipse, sized by [size] times the transform's scale. */
class ShapeRenderer(
    var shape: Shape = Shape.Rectangle,
    var size: Vec2   = Vec2(64f, 64f),
    var color: Color = Color(0xFFE8833A),
) : Component(), Bounded {
    enum class Shape { Rectangle, Ellipse }

    private val worldSize get() = size * transform.scale

    override fun onDraw(renderer: Renderer) {
        when (shape) {
            Shape.Rectangle -> renderer.rect(transform.position,   worldSize, color, transform.rotation)
            Shape.Ellipse  -> renderer.ellipse(transform.position, worldSize, color, transform.rotation)
        }
    }

    override fun bounds(): Rect {
        val s = worldSize
        val p = transform.position

        return Rect(p.x - abs(s.x) / 2, p.y - abs(s.y) / 2, p.x + abs(s.x) / 2, p.y + abs(s.y) / 2)
    }

    override fun properties(): List<Property> = listOf(
        ChoiceProperty ("Shape", Shape.entries.map { it.name }, { shape.ordinal }, { shape = Shape.entries[it] }),

        Vec2Property   ("Size",  { size },  { size  = it }),
        ColorProperty  ("Color", { color }, { color = it }),
    )

    override fun copy() = ShapeRenderer(shape, size, color)
}
