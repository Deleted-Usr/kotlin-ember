package com.willclay.ember.engine.core

import androidx.compose.ui.geometry.Rect
import com.willclay.ember.engine.math.Vec2
import com.willclay.ember.engine.render.Renderer

/**
 * Base class for all behaviour attached to a [GameObject].
 *
 * Subclass this to write game logic. Override the lifecycle hooks you need, expose editable
 * values through [properties], and implement [copy] so the editor can snapshot the scene on Play
 * and restore it on Stop.
 */
abstract class Component {
    lateinit var gameObject: GameObject
        internal set

    var enabled: Boolean = true
    internal var started = false

    val transform: Transform get()   = gameObject.transform
    protected val scene: Scene get() = gameObject.scene ?: error("${gameObject.name} is not in a scene")
    protected val input: Input get() = scene.context.input
    protected val time: Float get()  = scene.context.time

    protected fun log(message: String) = scene.context.log("[${gameObject.name}] $message")

    /** Name shown in the inspector. */
    open val typeName: String get() = this::class.simpleName ?: "Component"

    /** Called once, on the first frame this component is updated while the game is playing. */
    open fun onStart() {}

    /** Called every frame while playing. [dt] is in seconds. */
    open fun onUpdate(dt: Float) {}

    /** Called every frame (also in edit mode) to draw the component. */
    open fun onDraw(renderer: Renderer) {}

    /** Called when this object's solid collider hits another solid collider. [normal] points away from [other]. */
    open fun onCollision(other: GameObject, normal: Vec2) {}

    /** Called every physics step while this object overlaps a trigger collider (or is one). */
    open fun onTrigger(other: GameObject) {}

    /** Values shown and edited in the inspector. */
    open fun properties(): List<Property> = emptyList()

    /** Returns a fresh copy of this component with the same settings (not attached to any object). */
    abstract fun copy(): Component
}

/** Components with a visible footprint implement this so the editor can pick and outline them. */
interface Bounded {
    fun bounds(): Rect
}
