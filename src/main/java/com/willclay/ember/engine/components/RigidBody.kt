package com.willclay.ember.engine.components

import com.willclay.ember.engine.core.BoolProperty
import com.willclay.ember.engine.core.Component
import com.willclay.ember.engine.core.FloatProperty
import com.willclay.ember.engine.core.Property
import com.willclay.ember.engine.core.ReadOnlyProperty
import com.willclay.ember.engine.core.Vec2Property
import com.willclay.ember.engine.math.Vec2

/**
 * Makes an object move under physics. Objects with a [BoxCollider] but no RigidBody are static.
 * Kinematic bodies move by [velocity] only: no gravity and no collision response.
 */
class RigidBody(
    var velocity: Vec2      = Vec2.ZERO,
    var gravityScale: Float = 1f,
    /** Linear damping per second (0 = none). */
    var drag: Float          = 0f,
    var isKinematic: Boolean = false,
) : Component() {
    /** True if the body rested on something below it during the last physics step. */
    var grounded: Boolean = false
        internal set

    fun addImpulse(impulse: Vec2) {
        velocity += impulse
    }

    override fun properties(): List<Property> = listOf(
        Vec2Property  ("Velocity",       { velocity },     { velocity     = it }),
        FloatProperty ("Gravity Scale",  { gravityScale }, { gravityScale = it }),
        FloatProperty ("Drag",           { drag },         { drag         = it }),
        BoolProperty  ("Kinematic",      { isKinematic },  { isKinematic  = it }),

        ReadOnlyProperty("Grounded") { grounded.toString() },
    )

    override fun copy() = RigidBody(velocity, gravityScale, drag, isKinematic)
}
