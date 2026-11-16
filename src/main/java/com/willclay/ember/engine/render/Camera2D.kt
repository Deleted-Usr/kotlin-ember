package com.willclay.ember.engine.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.willclay.ember.engine.math.Vec2
import com.willclay.ember.engine.math.toVec2

/** World-space camera. [position] is the world point shown at the centre of the viewport. */
class Camera2D(
    var position: Vec2 = Vec2.ZERO,
    var zoom: Float = 1f,
) {
    /** Updated by the viewport every frame. */
    var viewportSize: Size = Size.Zero

    fun screenToWorld(screen: Offset): Vec2 =
        position + (screen.toVec2() - Vec2(viewportSize.width / 2f, viewportSize.height / 2f)) / zoom

    fun worldToScreen(world: Vec2): Offset =
        ((world - position) * zoom + Vec2(viewportSize.width / 2f, viewportSize.height / 2f)).toOffset()

    /** The world-space rectangle currently visible. */
    fun visibleWorld(): Rect {
        val tl = screenToWorld(Offset.Zero)
        val br = screenToWorld(Offset(viewportSize.width, viewportSize.height))

        return Rect(tl.x, tl.y, br.x, br.y)
    }

    /** Zoom by [factor] keeping the world point under [screenAnchor] fixed. */
    fun zoomAt(screenAnchor: Offset, factor: Float) {
        val before = screenToWorld(screenAnchor)
        zoom = (zoom * factor).coerceIn(0.1f, 10f)

        val after = screenToWorld(screenAnchor)
        position += before - after
    }
}
