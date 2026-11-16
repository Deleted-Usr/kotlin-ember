package com.willclay.ember.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willclay.ember.engine.core.GameObject
import com.willclay.ember.engine.core.PlayState
import com.willclay.ember.engine.math.Vec2
import com.willclay.ember.engine.math.toVec2
import com.willclay.ember.engine.render.Camera2D
import com.willclay.ember.engine.render.Renderer
import kotlin.math.floor
import kotlin.math.roundToInt

private val GridColor = Color(0x14FFFFFF)
private val AxisColor = Color(0x33FFFFFF)
private val SelectionColor = Color(0xFFFFB74D)

/**
 * The scene view. Draws the scene through the camera, forwards keyboard input to the engine,
 * and handles editor mouse interaction:
 *  - click to select, drag a selected object to move it (edit mode only; hold Ctrl or enable Snap to snap)
 *  - drag empty space (or with right/middle button) to pan, scroll to zoom
 */
@Composable
fun Viewport(editor: EditorState, modifier: Modifier = Modifier) {
    val engine       = editor.engine
    val textMeasurer = rememberTextMeasurer()
    val focus        = remember { FocusRequester() }

    Box(modifier.clipToBounds()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onKeyEvent { engine.input.onKeyEvent(it) && engine.state != PlayState.Editing }
                .focusRequester(focus)
                .focusable()
                .pointerInput(engine) {
                    awaitPointerEventScope {
                        var dragging: GameObject? = null
                        var dragOffset            = Vec2.ZERO
                        var panning               = false
                        var last                  = Offset.Zero

                        while (true) {
                            val event  = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue
                            val cam    = engine.camera

                            when (event.type) {
                                PointerEventType.Press -> {
                                    focus.requestFocus()
                                    last = change.position

                                    val world = cam.screenToWorld(change.position)
                                    val hit   = if (event.buttons.isPrimaryPressed) engine.scene.pick(world) else null

                                    if (event.buttons.isPrimaryPressed) {
                                        editor.select(hit)
                                    }

                                    if (hit != null && engine.state == PlayState.Editing) {
                                        dragging   = hit
                                        dragOffset = hit.transform.position - world
                                    }
                                    else {
                                        panning = true
                                    }
                                }
                                PointerEventType.Move -> {
                                    val target = dragging

                                    if (target != null) {
                                        var p = cam.screenToWorld(change.position) + dragOffset

                                        if (editor.snapToGrid || event.keyboardModifiers.isCtrlPressed) {
                                            val g = editor.gridSize
                                            p     = Vec2((p.x / g).roundToInt() * g, (p.y / g).roundToInt() * g)
                                        }

                                        target.transform.position = p
                                    }
                                    else if (panning) {
                                        cam.position -= (change.position - last).toVec2() / cam.zoom
                                    }

                                    last = change.position
                                }
                                PointerEventType.Release -> {
                                    dragging = null
                                    panning  = false
                                }
                                PointerEventType.Scroll -> {
                                    val factor = if (change.scrollDelta.y > 0) 1f / 1.1f else 1.1f
                                    cam.zoomAt(change.position, factor)
                                }
                            }
                        }
                    }
                }
        ) {
            engine.frame // read so the canvas redraws every frame

            val cam          = engine.camera
            cam.viewportSize = size

            drawRect(engine.scene.background)

            withTransform({
                translate(size.width / 2f, size.height / 2f)
                scale(cam.zoom, cam.zoom, pivot = Offset.Zero)
                translate(-cam.position.x, -cam.position.y)
            }) {
                val renderer = Renderer(this, cam, textMeasurer, showGizmos = editor.showGizmos)
                if (engine.state == PlayState.Editing || editor.showGizmos) drawGrid(cam, editor.gridSize * 2)
                engine.scene.draw(renderer)
                editor.selected?.bounds()?.let { renderer.outline(it.inflate(3f / cam.zoom), SelectionColor, 2f) }
            }
        }

        val hint = when (engine.state) {
            PlayState.Editing -> "EDIT  ·  drag to move · scroll to zoom · drag empty space to pan · Ctrl = snap"
            PlayState.Playing -> "PLAYING  ·  click here for keyboard focus"
            PlayState.Paused  -> "PAUSED"
        }
        Text(
            hint,
            Modifier.align(Alignment.TopStart).padding(8.dp)
                .background(Color(0x99000000), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            color = Color(0xCCFFFFFF),
            fontSize = 11.sp,
        )
    }
}

private fun DrawScope.drawGrid(cam: Camera2D, step: Float) {
    val view = cam.visibleWorld()
    val w    = 1f / cam.zoom
    var x    = floor(view.left / step) * step

    while (x <= view.right) {
        drawLine(if (x == 0f) AxisColor else GridColor, Offset(x, view.top), Offset(x, view.bottom), w)
        x += step
    }

    var y = floor(view.top / step) * step
    while (y <= view.bottom) {
        drawLine(if (y == 0f) AxisColor else GridColor, Offset(view.left, y), Offset(view.right, y), w)
        y += step
    }
}
