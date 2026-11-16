package com.willclay.ember.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.willclay.ember.engine.core.Engine

private val EmberColors = darkColorScheme(
    primary                 = Color(0xFFFF8A3D),
    onPrimary               = Color(0xFF2A1300),
    secondary               = Color(0xFFFFB74D),
    background              = Color(0xFF111216),
    surface                 = Color(0xFF17181D),
    surfaceContainer        = Color(0xFF1C1D23),
    surfaceContainerHigh    = Color(0xFF22232A),
    surfaceContainerHighest = Color(0xFF2B2C34),
)

/** Root of the editor window: toolbar, hierarchy, viewport, inspector and console. */
@Composable
fun EditorApp(engine: Engine) {
    val editor = remember(engine) { EditorState(engine) }

    // Game loop: one engine tick per display frame.
    LaunchedEffect(engine) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) {
                    0f
                }
                else {
                    (now - last) / 1_000_000_000f
                }

                last = now
                engine.tick(dt.coerceAtMost(1f / 20f))
            }
        }
    }

    MaterialTheme(colorScheme = EmberColors) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                Toolbar(editor)
                HorizontalDivider()

                Row(Modifier.weight(1f)) {
                    HierarchyPanel(editor, Modifier.width(230.dp).fillMaxHeight())
                    VerticalDivider()

                    Column(Modifier.weight(1f)) {
                        Viewport(editor, Modifier.weight(1f).fillMaxWidth())
                        HorizontalDivider()
                        ConsolePanel(editor, Modifier.height(150.dp).fillMaxWidth())
                    }

                    VerticalDivider()
                    InspectorPanel(editor, Modifier.width(320.dp).fillMaxHeight())
                }
            }
        }
    }
}
