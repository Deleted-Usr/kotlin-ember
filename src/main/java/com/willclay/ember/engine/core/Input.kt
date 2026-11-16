package com.willclay.ember.engine.core

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/** Keyboard state, fed by the viewport's key events and polled by components. */
class Input {
    private val down     = mutableSetOf<Key>()
    private val pressed  = mutableSetOf<Key>()
    private val released = mutableSetOf<Key>()

    /** True while [key] is held. */
    fun isDown(key: Key) = key in down

    /** True only on the frame [key] went down. */
    fun wasPressed(key: Key) = key in pressed

    /** True only on the frame [key] went up. */
    fun wasReleased(key: Key) = key in released

    // Key is a value class, so these take a collection rather than varargs.
    fun anyDown(keys: Collection<Key>)    = keys.any(::isDown)
    fun anyPressed(keys: Collection<Key>) = keys.any(::wasPressed)

    /** -1, 0 or 1 from a pair of keys. */
    fun axis(negative: Key, positive: Key): Float =
        (if (isDown(positive)) 1f else 0f) - (if (isDown(negative)) 1f else 0f)

    /** A/D or Left/Right. */
    fun horizontal(): Float = (axis(Key.A, Key.D) + axis(Key.DirectionLeft, Key.DirectionRight)).coerceIn(-1f, 1f)

    /** W/S or Up/Down (Y-down, so Down is positive). */
    fun vertical(): Float = (axis(Key.W, Key.S) + axis(Key.DirectionUp, Key.DirectionDown)).coerceIn(-1f, 1f)

    fun onKeyEvent(event: KeyEvent): Boolean {
        when (event.type) {
            KeyEventType.KeyDown -> if (down.add(event.key)) pressed += event.key
            KeyEventType.KeyUp   -> if (down.remove(event.key)) released += event.key
            else                 -> return false
        }
        return true
    }

    fun endFrame() {
        pressed.clear()
        released.clear()
    }

    fun clear() {
        down.clear()
        endFrame()
    }
}
