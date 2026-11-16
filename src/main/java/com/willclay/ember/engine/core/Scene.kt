package com.willclay.ember.engine.core

import androidx.compose.ui.graphics.Color
import com.willclay.ember.engine.math.Vec2
import com.willclay.ember.engine.physics.Physics2D
import com.willclay.ember.engine.render.Renderer

/** A collection of [GameObject]s plus world settings. Objects are updated and drawn in list order. */
class Scene(var name: String = "Untitled") {
    var gravity    = Vec2(0f, 1400f)
    var background = Color(0xFF16171D)

    /** Set by the [Engine] when the scene is loaded. */
    lateinit var context: EngineContext

    private val _objects = mutableListOf<GameObject>()
    val objects: List<GameObject> get() = _objects

    private var updating      = false
    private val pendingAdd    = mutableListOf<GameObject>()
    private val pendingRemove = mutableListOf<GameObject>()

    /** Adds an object. Safe to call from inside a component update (applied at end of frame). */
    fun add(go: GameObject): GameObject {
        go.scene = this

        if (updating) {
            pendingAdd += go
        }
        else {
            _objects += go
        }

        return go
    }

    /** Convenience builder: `scene.create("Player") { add(ShapeRenderer()) }`. */
    fun create(name: String, block: GameObject.() -> Unit = {}): GameObject = add(GameObject(name).apply(block))

    /** Removes an object. Safe to call from inside a component update (applied at end of frame). */
    fun destroy(go: GameObject) {
        if (updating) {
            pendingRemove += go
        }
        else {
            _objects.remove(go)
            go.scene = null
        }
    }

    fun find(name: String): GameObject?  = _objects.firstOrNull { it.name == name }
    fun findById(id: Long?): GameObject? = id?.let { _objects.firstOrNull { o -> o.id == it } }

    /** Moves [go] by [delta] places in the draw/update order. */
    fun move(go: GameObject, delta: Int) {
        val i = _objects.indexOf(go)

        if (i < 0) {
            return
        }

        val j = (i + delta).coerceIn(0, _objects.lastIndex)

        _objects.removeAt(i)
        _objects.add(j, go)
    }

    fun update(dt: Float) {
        updating = true
        try {
            for (go in _objects) {
                if (!go.active) continue

                for (c in go.components.toList()) {
                    if (!c.enabled) {
                        continue
                    }

                    if (!c.started) {
                        c.started = true
                        c.onStart()
                    }

                    c.onUpdate(dt)
                }
            }

            Physics2D.step(this, dt)
        }
        finally {
            updating = false

            pendingRemove.forEach { _objects.remove(it); it.scene = null }
            pendingRemove.clear()

            _objects += pendingAdd

            pendingAdd.clear()
        }
    }

    fun draw(renderer: Renderer) {
        for (go in _objects) {
            if (!go.active) {
                continue
            }

            for (c in go.components) {
                if (c.enabled) {
                    c.onDraw(renderer)
                }
            }
        }
    }

    /** Topmost active object whose bounds contain [world]. */
    fun pick(world: Vec2): GameObject? =
        _objects.lastOrNull { it.active && it.bounds()?.contains(world.toOffset()) == true }

    fun copy(): Scene = Scene(name).also { s ->
        s.gravity    = gravity
        s.background = background

        _objects.forEach { s.add(it.copy()) }
    }
}
