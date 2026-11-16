package com.willclay.ember.engine.core

import com.willclay.ember.engine.components.BoxCollider
import com.willclay.ember.engine.components.RigidBody
import com.willclay.ember.engine.components.ShapeRenderer

/** Components the editor's "Add Component" menu can create. Register your own with [register]. */
object ComponentRegistry {
    data class Entry(val name: String, val factory: () -> Component)

    private val _entries = mutableListOf(
        Entry("Shape Renderer") { ShapeRenderer() },
        Entry("Rigid Body")     { RigidBody() },
        Entry("Box Collider")   { BoxCollider() },
    )
    val entries: List<Entry> get() = _entries

    fun register(name: String, factory: () -> Component) {
        _entries += Entry(name, factory)
    }
}
