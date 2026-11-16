package com.willclay.ember.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.willclay.ember.engine.core.Engine
import com.willclay.ember.engine.core.GameObject

/** Editor-only UI state (selection, toggles). Lives outside the engine so the engine stays UI-agnostic. */
class EditorState(val engine: Engine) {
    var selectedId by mutableStateOf<Long?>(null)
    var showGizmos by mutableStateOf(true)
    var snapToGrid by mutableStateOf(false)
    val gridSize = 25f

    val selected: GameObject? get() = engine.scene.findById(selectedId)

    fun select(go: GameObject?) {
        selectedId = go?.id
    }
}
