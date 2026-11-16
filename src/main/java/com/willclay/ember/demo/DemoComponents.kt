package com.willclay.ember.demo

import androidx.compose.ui.input.key.Key
import com.willclay.ember.engine.components.RigidBody
import com.willclay.ember.engine.core.Component
import com.willclay.ember.engine.core.ComponentRegistry
import com.willclay.ember.engine.core.FloatProperty
import com.willclay.ember.engine.core.GameObject
import com.willclay.ember.engine.core.Property
import com.willclay.ember.engine.math.Vec2
import kotlin.math.sin

/** Left/right with A/D or arrows, jump with Space/W/Up. Respawns if it falls off the world. */
class PlayerController(
    var speed: Float = 320f,
    var jumpSpeed: Float = 720f,
    var killY: Float = 1200f,
) : Component() {
    private var spawn = Vec2.ZERO
    private val jumpKeys = listOf(Key.Spacebar, Key.W, Key.DirectionUp)

    override fun onStart() {
        spawn = transform.position
    }

    override fun onUpdate(dt: Float) {
        val body = gameObject.get<RigidBody>() ?: return
        body.velocity = body.velocity.copy(x = input.horizontal() * speed)
        if (body.grounded && input.anyPressed(jumpKeys)) {
            body.velocity = body.velocity.copy(y = -jumpSpeed)
        }
        if (transform.position.y > killY) {
            log("Fell off the world, respawning")
            transform.position = spawn
            body.velocity = Vec2.ZERO
        }
    }

    override fun properties(): List<Property> = listOf(
        FloatProperty("Speed", { speed }, { speed = it }),
        FloatProperty("Jump Speed", { jumpSpeed }, { jumpSpeed = it }),
        FloatProperty("Kill Y", { killY }, { killY = it }),
    )

    override fun copy() = PlayerController(speed, jumpSpeed, killY)
}

/** Rotates the object at a constant rate. */
class Spinner(var degreesPerSecond: Float = 90f) : Component() {
    override fun onUpdate(dt: Float) {
        transform.rotation = (transform.rotation + degreesPerSecond * dt) % 360f
    }

    override fun properties(): List<Property> = listOf(FloatProperty("Speed (°/s)", { degreesPerSecond }, { degreesPerSecond = it }))
    override fun copy() = Spinner(degreesPerSecond)
}

/** Bobs up and down; destroyed when an object named "Player" touches its trigger. */
class Collectible(var bobHeight: Float = 8f) : Component() {
    private var baseY = 0f

    override fun onStart() {
        baseY = transform.position.y
    }

    override fun onUpdate(dt: Float) {
        transform.position = transform.position.copy(y = baseY + sin(time * 4f) * bobHeight)
    }

    override fun onTrigger(other: GameObject) {
        if (other.name == "Player") {
            log("Collected!")
            scene.destroy(gameObject)
        }
    }

    override fun properties(): List<Property> = listOf(FloatProperty("Bob Height", { bobHeight }, { bobHeight = it }))
    override fun copy() = Collectible(bobHeight)
}

object DemoComponents {
    fun register() {
        ComponentRegistry.register("Player Controller") { PlayerController() }
        ComponentRegistry.register("Spinner") { Spinner() }
        ComponentRegistry.register("Collectible") { Collectible() }
    }
}
