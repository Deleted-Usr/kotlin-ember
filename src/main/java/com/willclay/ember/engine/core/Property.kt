package com.willclay.ember.engine.core

import androidx.compose.ui.graphics.Color
import com.willclay.ember.engine.math.Vec2

/**
 * Editable values a [Component] exposes to the editor's inspector.
 * Add a new subclass here (and a matching field in the inspector) to support new value types.
 */
sealed class Property(val name: String)

class FloatProperty    (name: String, val get: () -> Float,      val set: (Float)   -> Unit) : Property(name)
class Vec2Property     (name: String, val get: () -> Vec2,       val set: (Vec2)    -> Unit) : Property(name)
class BoolProperty     (name: String, val get: () -> Boolean,    val set: (Boolean) -> Unit) : Property(name)
class ColorProperty    (name: String, val get: () -> Color,      val set: (Color)   -> Unit) : Property(name)
class ChoiceProperty   (name: String, val options: List<String>, val get: ()        -> Int, val set: (Int) -> Unit) : Property(name)
class ReadOnlyProperty (name: String, val get: () -> String) : Property(name)
