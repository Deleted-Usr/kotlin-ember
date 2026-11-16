package com.willclay.ember.engine.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.willclay.ember.engine.core.BoolProperty
import com.willclay.ember.engine.core.Bounded
import com.willclay.ember.engine.core.Component
import com.willclay.ember.engine.core.Property
import com.willclay.ember.engine.core.Vec2Property
import com.willclay.ember.engine.math.Vec2
import com.willclay.ember.engine.render.Renderer
import kotlin.math.abs

/**
 * Axis-aligned box collider (rotation is ignored). [size] and [offset] are scaled by the transform.
 * Triggers report overlaps through `onTrigger` but don't block movement.
 */
class BoxCollider(
    var size: Vec2         = Vec2(64f, 64f),
    var offset: Vec2       = Vec2.ZERO,
    var isTrigger: Boolean = false,
) : Component(), Bounded {

    fun aabb(): Rect {
        val s  = size * transform.scale
        val c  = transform.position + offset * transform.scale
        val hx = abs(s.x) / 2f
        val hy = abs(s.y) / 2f

        return Rect(c.x - hx, c.y - hy, c.x + hx, c.y + hy)
    }

    override fun bounds(): Rect = aabb()

    override fun onDraw(renderer: Renderer) {
        if (renderer.showGizmos) {
            renderer.outline(aabb(), if (isTrigger) Color(0xFF4FC3F7) else Color(0xFF66BB6A), 1.5f)
        }
    }

    override fun properties(): List<Property> = listOf(
        Vec2Property("Size",    { size },      { size      = it }),
        Vec2Property("Offset",  { offset },    { offset    = it }),
        BoolProperty("Trigger", { isTrigger }, { isTrigger = it }),
    )

    override fun copy() = BoxCollider(size, offset, isTrigger)
}
