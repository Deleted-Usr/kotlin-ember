package com.willclay.ember.engine.core

import androidx.compose.ui.geometry.Rect
import java.util.concurrent.atomic.AtomicLong

/** An entity in a [Scene]: a name, a [Transform] and a list of [Component]s. */
class GameObject(var name: String, val id: Long = nextId()) {
    val transform       = Transform()
    var active: Boolean = true
    var scene: Scene?   = null
        internal set

    private val _components = mutableListOf<Component>()
    val components: List<Component> get() = _components

    fun <T : Component> add(component: T): T {
        component.gameObject = this
        _components += component

        return component
    }

    fun remove(component: Component) {
        _components.remove(component)
    }

    inline fun <reified T : Component> get(): T? = components.firstOrNull { it is T } as T?
    inline fun <reified T : Component> getAll(): List<T> = components.filterIsInstance<T>()

    /** Union of the bounds of every [Bounded] component, or null if there are none. */
    fun bounds(): Rect? = components
        .filter { it.enabled && it is Bounded }
        .map { (it as Bounded).bounds() }
        .reduceOrNull { a, b ->
            Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom))
        }

    /** Deep copy. Keeps the same [id] unless [newId] is true. */
    fun copy(newId: Boolean = false): GameObject = GameObject(name, if (newId) nextId() else id).also { copy ->
        copy.active = active
        copy.transform.setFrom(transform)

        components.forEach { c -> copy.add(c.copy()).enabled = c.enabled }
    }

    companion object {
        private val counter = AtomicLong()
        fun nextId(): Long  = counter.incrementAndGet()
    }
}
