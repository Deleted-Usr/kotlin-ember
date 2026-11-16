package com.willclay.ember.demo

import androidx.compose.ui.graphics.Color
import com.willclay.ember.engine.components.BoxCollider
import com.willclay.ember.engine.components.RigidBody
import com.willclay.ember.engine.components.ShapeRenderer
import com.willclay.ember.engine.core.Scene
import com.willclay.ember.engine.math.Vec2

/** A tiny platformer showing off rendering, physics, triggers, input and scripting. */
object DemoScene {
    private val platformColor = Color(0xFF3A3D4A)

    fun create(): Scene = Scene("Demo Platformer").apply {
        fun platform(name: String, x: Float, y: Float, w: Float, h: Float) = create(name) {
            transform.position = Vec2(x, y)
            add(ShapeRenderer(size = Vec2(w, h), color = platformColor))
            add(BoxCollider(size = Vec2(w, h)))
        }

        platform("Ground", 0f, 300f, 1400f, 60f)
        platform("Platform A", -300f, 140f, 220f, 24f)
        platform("Platform B", 50f, 20f, 200f, 24f)
        platform("Platform C", 380f, -100f, 220f, 24f)
        platform("Wall Left", -720f, 100f, 40f, 460f)
        platform("Wall Right", 720f, 100f, 40f, 460f)

        create("Spinning Gear") {
            transform.position = Vec2(380f, -220f)
            add(ShapeRenderer(size = Vec2(60f, 60f), color = Color(0xFFFFB74D)))
            add(Spinner(120f))
        }

        listOf(Vec2(-300f, 100f), Vec2(50f, -20f), Vec2(380f, -140f), Vec2(550f, 240f)).forEachIndexed { i, p ->
            create("Coin ${i + 1}") {
                transform.position = p
                add(ShapeRenderer(ShapeRenderer.Shape.Ellipse, Vec2(24f, 24f), Color(0xFFFFD54F)))
                add(BoxCollider(Vec2(24f, 24f), isTrigger = true))
                add(Collectible())
            }
        }

        create("Crate") {
            transform.position = Vec2(200f, 150f)
            add(ShapeRenderer(size = Vec2(56f, 56f), color = Color(0xFF8D6E63)))
            add(BoxCollider(Vec2(56f, 56f)))
            add(RigidBody())
        }

        create("Player") {
            transform.position = Vec2(-500f, 200f)
            add(ShapeRenderer(size = Vec2(40f, 56f), color = Color(0xFFFF8A3D)))
            add(BoxCollider(Vec2(40f, 56f)))
            add(RigidBody())
            add(PlayerController())
        }
    }
}
