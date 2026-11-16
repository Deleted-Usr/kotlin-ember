package com.willclay.ember.engine.core

import com.willclay.ember.engine.math.Vec2

/** Position, rotation (degrees, clockwise) and scale of a [GameObject]. */
class Transform(
    var position: Vec2  = Vec2.ZERO,
    var rotation: Float = 0f,
    var scale: Vec2     = Vec2.ONE,
) {
    fun translate(delta: Vec2) {
        position += delta
    }

    fun setFrom(other: Transform) {
        position = other.position
        rotation = other.rotation
        scale    = other.scale
    }
}
