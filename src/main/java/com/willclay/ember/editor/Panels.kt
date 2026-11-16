package com.willclay.ember.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willclay.ember.engine.components.BoxCollider
import com.willclay.ember.engine.components.RigidBody
import com.willclay.ember.engine.components.ShapeRenderer
import com.willclay.ember.engine.core.BoolProperty
import com.willclay.ember.engine.core.ChoiceProperty
import com.willclay.ember.engine.core.ColorProperty
import com.willclay.ember.engine.core.Component
import com.willclay.ember.engine.core.ComponentRegistry
import com.willclay.ember.engine.core.FloatProperty
import com.willclay.ember.engine.core.GameObject
import com.willclay.ember.engine.core.PlayState
import com.willclay.ember.engine.core.Property
import com.willclay.ember.engine.core.ReadOnlyProperty
import com.willclay.ember.engine.core.Vec2Property
import com.willclay.ember.engine.math.Vec2
import java.util.Locale

@Composable
fun PanelHeader(title: String, actions: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(start = 10.dp, end = 4.dp).height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        actions()
    }
}

@Composable
private fun SmallButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
        Text(text, fontSize = 12.sp)
    }
}

// ---------------------------------------------------------------- Toolbar

@Composable
fun Toolbar(editor: EditorState) {
    val engine = editor.engine
    engine.frame
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Ember", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(engine.scene.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))

            val playing = engine.state == PlayState.Playing
            FilterChip(selected = playing, onClick = { if (playing) engine.pause() else engine.play() },        label = { Text(if (playing) "❚❚ Pause" else "▶ Play") })
            FilterChip(selected = false,   enabled = engine.state == PlayState.Paused,  onClick = engine::step, label = { Text("Step") })
            FilterChip(selected = false,   enabled = engine.state != PlayState.Editing, onClick = engine::stop, label = { Text("■ Stop") })

            Spacer(Modifier.weight(1f))
            FilterChip(selected = editor.showGizmos, onClick = { editor.showGizmos = !editor.showGizmos }, label = { Text("Gizmos") })
            FilterChip(selected = editor.snapToGrid, onClick = { editor.snapToGrid = !editor.snapToGrid }, label = { Text("Snap") })

            Text(
                String.format(Locale.ROOT, "%3.0f fps · %d objects", engine.fps, engine.scene.objects.size),
                fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

// ---------------------------------------------------------------- Hierarchy

private val newObjectTemplates: List<Pair<String, () -> GameObject>> = listOf(
    "Empty"       to { GameObject("Empty") },
    "Box"         to { GameObject("Box").apply { add(ShapeRenderer()); add(BoxCollider()) } },
    "Circle"      to { GameObject("Circle").apply { add(ShapeRenderer(ShapeRenderer.Shape.Ellipse, color = Color(0xFF4FC3F7))) } },
    "Physics Box" to {
        GameObject("Physics Box").apply { add(ShapeRenderer(color = Color(0xFFBA68C8))); add(BoxCollider()); add(RigidBody()) }
    },
)

@Composable
fun HierarchyPanel(editor: EditorState, modifier: Modifier = Modifier) {
    val engine = editor.engine
    engine.frame

    val scene = engine.scene

    Column(modifier) {
        var menu by remember { mutableStateOf(false) }
        PanelHeader("Hierarchy") {
            Box {
                SmallButton("+ Add") { menu = true }

                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    newObjectTemplates.forEach { (name, make) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            menu   = false
                            val go = make()

                            go.transform.position = engine.camera.position

                            scene.add(go)
                            editor.select(go)
                        })
                    }
                }
            }
        }

        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(scene.objects.toList(), key = { _, go -> go.id }) { _, go ->
                val selected = go.id == editor.selectedId

                Text(
                    go.name,
                    Modifier.fillMaxWidth()
                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { editor.select(go) }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    fontSize = 13.sp,
                    color = if (go.active) {
                        MaterialTheme.colorScheme.onSurface
                    }
                    else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                )
            }
        }
        val sel = editor.selected
        HorizontalDivider()
        Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            SmallButton("▲", sel != null) { sel?.let { scene.move(it, -1) } }
            SmallButton("▼", sel != null) { sel?.let { scene.move(it, 1) } }
            SmallButton("Copy", sel != null) {
                sel?.copy(newId = true)?.let {
                    it.name = "${sel.name} copy"
                    it.transform.translate(Vec2(20f, 20f))

                    scene.add(it)
                    editor.select(it)
                }
            }
            SmallButton("Del", sel != null) { sel?.let { scene.destroy(it); editor.select(null) } }
        }
    }
}

// ---------------------------------------------------------------- Inspector

@Composable
fun InspectorPanel(editor: EditorState, modifier: Modifier = Modifier) {
    val engine = editor.engine
    engine.frame // refresh live values every frame
    Column(modifier) {
        PanelHeader("Inspector")
        val go = editor.selected
        if (go == null) {
            Text(
                "Select an object in the hierarchy or viewport.",
                Modifier.padding(12.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        key(go.id) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Section("Object") {
                    FieldRow("Name") { TextValueField(go.name) { go.name = it } }
                    FieldRow("Active") { BoolField(go.active) { go.active = it } }
                    FieldRow("Id") { Text(go.id.toString(), fontSize = 12.sp) }
                }
                Section("Transform") {
                    val t = go.transform
                    FieldRow("Position") { Vec2Field(t.position) { t.position = it } }
                    FieldRow("Rotation") { FloatField(t.rotation, { t.rotation = it }) }
                    FieldRow("Scale") { Vec2Field(t.scale) { t.scale = it } }
                }
                go.components.toList().forEach { c -> ComponentSection(go, c) }
                AddComponentButton(go)
            }
        }
    }
}

@Composable
private fun Section(title: String, trailing: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
            trailing()
        }
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun ComponentSection(go: GameObject, c: Component) {
    Section(c.typeName, trailing = {
        BoolField(c.enabled) { c.enabled = it }
        SmallButton("✕") { go.remove(c) }
    }) {
        c.properties().forEach { PropertyRow(it) }
    }
}

@Composable
private fun PropertyRow(p: Property) {
    FieldRow(p.name) {
        when (p) {
            is FloatProperty -> FloatField(p.get(), p.set)
            is Vec2Property -> Vec2Field(p.get(), p.set)
            is BoolProperty -> BoolField(p.get(), p.set)
            is ColorProperty -> ColorField(p.get(), p.set)
            is ChoiceProperty -> ChoiceField(p.options, p.get(), p.set)
            is ReadOnlyProperty -> Text(p.get(), fontSize = 12.sp)
        }
    }
}

@Composable
private fun AddComponentButton(go: GameObject) {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(onClick = { open = true }) { Text("+ Add Component") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ComponentRegistry.entries.forEach { entry ->
                DropdownMenuItem(text = { Text(entry.name) }, onClick = { go.add(entry.factory()); open = false })
            }
        }
    }
}

// ---------------------------------------------------------------- Console

@Composable
fun ConsolePanel(editor: EditorState, modifier: Modifier = Modifier) {
    val logs = editor.engine.logs
    val listState = rememberLazyListState()
    LaunchedEffect(logs.size) { if (logs.isNotEmpty()) listState.scrollToItem(logs.lastIndex) }
    Column(modifier) {
        PanelHeader("Console") { SmallButton("Clear") { logs.clear() } }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 4.dp), state = listState) {
            itemsIndexed(logs) { _, entry ->
                Row {
                    Text(
                        String.format(Locale.ROOT, "%7.2fs", entry.time),
                        Modifier.width(64.dp), fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(entry.message, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
