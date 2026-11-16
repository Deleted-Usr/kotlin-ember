package com.willclay.ember

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.willclay.ember.demo.DemoComponents
import com.willclay.ember.demo.DemoScene
import com.willclay.ember.editor.EditorApp
import com.willclay.ember.engine.core.Engine
import com.willclay.ember.engine.math.Vec2

fun main() {
    DemoComponents.register()
    application {
        val engine = remember {
            Engine(DemoScene.create()).apply {
                camera.zoom = 0.55f
                camera.position = Vec2(0f, 40f)
                log("Welcome to Ember. Press Play, click the viewport, then use A/D + Space.")
            }
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = "Ember",
            state = rememberWindowState(width = 1440.dp, height = 880.dp),
        ) {
            EditorApp(engine)
        }
    }
}
