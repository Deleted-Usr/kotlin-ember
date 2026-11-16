package com.willclay.ember

import com.willclay.ember.engine.components.BoxCollider
import com.willclay.ember.engine.components.RigidBody
import com.willclay.ember.engine.core.Engine
import com.willclay.ember.engine.core.Scene
import com.willclay.ember.engine.math.Vec2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EngineTest {
    private fun sceneWithFallingBox() = Scene().apply {
        create("Ground") {
            transform.position = Vec2(0f, 100f)
            add(BoxCollider(Vec2(1000f, 20f)))
        }
        create("Box") {
            add(BoxCollider(Vec2(20f, 20f)))
            add(RigidBody())
        }
    }

    @Test
    fun bodyLandsOnGroundAndIsGrounded() {
        val engine = Engine(sceneWithFallingBox())
        engine.play()
        repeat(120) { engine.tick(1f / 60f) }

        val box = engine.scene.find("Box")!!
        assertEquals(80f, box.transform.position.y, 0.5f) // ground top (90) - half box (10)
        assertTrue(box.get<RigidBody>()!!.grounded)
    }

    @Test
    fun stopRestoresEditSnapshot() {
        val engine = Engine(sceneWithFallingBox())
        engine.play()
        repeat(30) { engine.tick(1f / 60f) }
        engine.stop()

        assertEquals(Vec2.ZERO, engine.scene.find("Box")!!.transform.position)
    }
}
