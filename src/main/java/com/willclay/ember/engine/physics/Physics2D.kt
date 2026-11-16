package com.willclay.ember.engine.physics

import com.willclay.ember.engine.components.BoxCollider
import com.willclay.ember.engine.components.RigidBody
import com.willclay.ember.engine.core.GameObject
import com.willclay.ember.engine.core.Scene
import com.willclay.ember.engine.math.Vec2
import kotlin.math.min

/**
 * Minimal AABB physics: integrates [RigidBody]s and pushes them out of solid [BoxCollider]s
 * along the axis of least penetration. Good enough for platformers and top-down games;
 * swap this out for something sturdier (swept tests, a broadphase, Box2D, ...) when needed.
 */
object Physics2D {
    fun step(scene: Scene, dt: Float) {
        val colliders = scene.objects
            .filter { it.active }
            .flatMap { go -> go.getAll<BoxCollider>().filter { it.enabled } }

        for (go in scene.objects) {
            if (!go.active) continue
            val body = go.get<RigidBody>()?.takeIf { it.enabled } ?: continue

            body.grounded = false
            if (!body.isKinematic) {
                body.velocity += scene.gravity * (body.gravityScale * dt)
            }

            if (body.drag > 0f) {
                body.velocity *= (1f - body.drag * dt).coerceAtLeast(0f)
            }

            go.transform.position += body.velocity * dt

            val own = go.get<BoxCollider>()?.takeIf { it.enabled } ?: continue
            for (other in colliders) {
                if (other.gameObject === go) {
                    continue
                }

                val a = own.aabb()
                val b = other.aabb()

                if (!a.overlaps(b)) {
                    continue
                }

                if (own.isTrigger || other.isTrigger) {
                    dispatchTrigger(go, other.gameObject)
                    dispatchTrigger(other.gameObject, go)
                    continue
                }

                if (body.isKinematic) {
                    continue
                }

                val dx = min(a.right  - b.left, b.right  - a.left)
                val dy = min(a.bottom - b.top,  b.bottom - a.top)
                val normal: Vec2

                if (dx < dy) {
                    val sign = if (a.center.x < b.center.x) {
                        -1f
                    }
                    else {
                        1f
                    }

                    go.transform.position += Vec2(sign * dx, 0f)

                    if (body.velocity.x * sign < 0f) {
                        body.velocity = body.velocity.copy(x = 0f)
                    }

                    normal = Vec2(sign, 0f)
                }
                else {
                    val sign = if (a.center.y < b.center.y) {
                        -1f
                    }
                    else {
                        1f
                    }

                    go.transform.position += Vec2(0f, sign * dy)

                    if (body.velocity.y * sign < 0f) {
                        body.velocity = body.velocity.copy(y = 0f)
                    }

                    if (sign < 0f) {
                        body.grounded = true
                    }

                    normal = Vec2(0f, sign)
                }

                dispatchCollision(go, other.gameObject, normal)
                dispatchCollision(other.gameObject, go, -normal)
            }
        }
    }

    private fun dispatchCollision(target: GameObject, other: GameObject, normal: Vec2) {
        for (c in target.components.toList()) if (c.enabled) c.onCollision(other, normal)
    }

    private fun dispatchTrigger(target: GameObject, other: GameObject) {
        for (c in target.components.toList()) if (c.enabled) c.onTrigger(other)
    }
}
