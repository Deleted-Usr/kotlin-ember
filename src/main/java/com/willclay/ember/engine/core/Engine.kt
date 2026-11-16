package com.willclay.ember.engine.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.willclay.ember.engine.render.Camera2D

/** What components can reach while running. */
interface EngineContext {
    val input: Input
    /** Seconds since Play was pressed. */
    val time: Float
    fun log(message: String)
}

enum class PlayState { Editing, Playing, Paused }

data class LogEntry(val time: Float, val message: String)

/**
 * Owns the active [Scene], the play state and the editor camera, and advances the game each frame.
 * The UI drives it by calling [tick] once per frame (see the editor's frame loop).
 */
class Engine(initialScene: Scene) : EngineContext {
    override val input = Input()
    val camera         = Camera2D()
    val logs           = mutableStateListOf<LogEntry>()

    var scene: Scene by mutableStateOf(initialScene.also { it.context = this })
        private set
    var state by mutableStateOf(PlayState.Editing)
        private set

    /** Incremented every frame. Read it in UI that should refresh each frame. */
    var frame by mutableLongStateOf(0L)
        private set
    var fps by mutableFloatStateOf(0f)
        private set
    override var time = 0f
        private set

    private var editSnapshot: Scene? = null
    private var stepOnce             = false

    fun play() {
        when (state) {
            PlayState.Editing -> {
                editSnapshot = scene.copy()
                time         = 0f

                log("Play")

                state = PlayState.Playing
            }

            PlayState.Paused  -> state = PlayState.Playing
            PlayState.Playing -> Unit
        }
    }

    fun pause() {
        if (state == PlayState.Playing) {
            state = PlayState.Paused
        }
    }

    /** Advance a single frame while paused. */
    fun step() {
        if (state == PlayState.Paused) {
            stepOnce = true
        }
    }

    /** Stop playing and restore the scene to how it was before Play. */
    fun stop() {
        if (state == PlayState.Editing) {
            return
        }

        editSnapshot?.let(::activate)
        editSnapshot = null

        input.clear()
        state = PlayState.Editing

        log("Stop")
    }

    fun loadScene(newScene: Scene) {
        editSnapshot = null

        input.clear()
        state = PlayState.Editing

        activate(newScene)
    }

    private fun activate(s: Scene) {
        s.context = this
        scene     = s
    }

    fun tick(dt: Float) {
        if (state == PlayState.Playing || stepOnce) {
            val step = if (stepOnce) {
                1f / 60f
            }
            else {
                dt
            }

            stepOnce  = false
            time     += step

            try {
                scene.update(step)
            }
            catch (e: Exception) {
                log("Error: ${e.message ?: e::class.simpleName} (paused)")
                e.printStackTrace()

                state = PlayState.Paused
            }
        }

        input.endFrame()
        if (dt > 0f) {
            fps = if (fps == 0f) {
                1f / dt
            }
            else {
                fps * 0.95f + (1f / dt) * 0.05f
            }
        }

        frame++
    }

    override fun log(message: String) {
        logs += LogEntry(time, message)
        if (logs.size > 500) {
            logs.removeRange(0, logs.size - 500)
        }
    }
}
