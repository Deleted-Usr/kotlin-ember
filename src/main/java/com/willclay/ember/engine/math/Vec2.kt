package com.willclay.ember.engine.math

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

/** Immutable 2D vector. World space is Y-down (screen-like): +Y points towards the bottom of the screen. */
data class Vec2(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(o: Vec2)   = Vec2(x + o.x, y + o.y)
    operator fun minus(o: Vec2)  = Vec2(x - o.x, y - o.y)
    operator fun times(s: Float) = Vec2(x * s, y * s)
    operator fun times(o: Vec2)  = Vec2(x * o.x, y * o.y)
    operator fun div(s: Float)   = Vec2(x / s, y / s)
    operator fun unaryMinus()    = Vec2(-x, -y)

    val length: Float get() = sqrt(x * x + y * y)
    fun normalized(): Vec2  = length.let { if (it == 0f) ZERO else this / it }
    fun dot(o: Vec2): Float = x * o.x + y * o.y

    fun toOffset() = Offset(x, y)

    companion object {
        val ZERO = Vec2(0f, 0f)
        val ONE  = Vec2(1f, 1f)
    }
}

fun Offset.toVec2() = Vec2(x, y)
