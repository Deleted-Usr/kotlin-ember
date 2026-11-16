package com.willclay.ember.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willclay.ember.engine.math.Vec2
import java.util.Locale

private val labelWidth = 92.dp

fun formatFloat(v: Float): String =
    if (v == v.toLong().toFloat() && kotlin.math.abs(v) < 1e7f) {
        v.toLong().toString()
    }
    else {
        String.format(Locale.ROOT, "%.2f", v)
    }

/** A labelled row: label on the left, editor on the right. */
@Composable
fun FieldRow(label: String, content: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.width(labelWidth), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String?    = null,
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    BasicTextField(
        value         = value,
        onValueChange = onValueChange,
        singleLine    = true,
        textStyle     = MaterialTheme.typography.bodySmall.copy(color = colors.onSurface),
        cursorBrush   = SolidColor(colors.primary),
        modifier      = modifier.onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Row(
                Modifier
                    .background (colors.surfaceContainerHighest, RoundedCornerShape(4.dp))
                    .border     (1.dp, if (focused) colors.primary else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding    (horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (prefix != null) {
                    Text(prefix, fontSize = 11.sp, color = colors.primary)
                    Spacer(Modifier.width(4.dp))
                }

                Box(Modifier.weight(1f)) { inner() }
            }
        },
    )
}

/**
 * Text field bound to a float that may change every frame. While focused it keeps what the user
 * typed; otherwise it follows [value].
 */
@Composable
fun FloatField(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier, prefix: String? = null) {
    var text by remember { mutableStateOf(formatFloat(value)) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value, focused) {
        if (!focused && text.toFloatOrNull() != value) text = formatFloat(value)
    }
    CompactTextField(
        value = text,
        onValueChange = { t -> text = t; t.toFloatOrNull()?.let(onValueChange) },
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        prefix = prefix,
    )
}

@Composable
fun Vec2Field(value: Vec2, onValueChange: (Vec2) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        FloatField(value.x, { onValueChange(value.copy(x = it)) }, Modifier.weight(1f), prefix = "X")
        FloatField(value.y, { onValueChange(value.copy(y = it)) }, Modifier.weight(1f), prefix = "Y")
    }
}

@Composable
fun TextValueField(value: String, onValueChange: (String) -> Unit) {
    var text by remember { mutableStateOf(value) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value, focused) { if (!focused) text = value }
    CompactTextField(text, { text = it; onValueChange(it) }, Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused })
}

@Composable
fun BoolField(value: Boolean, onValueChange: (Boolean) -> Unit) {
    Checkbox(checked = value, onCheckedChange = onValueChange, modifier = Modifier.size(24.dp))
}

/** Hex colour field (#RRGGBB or #AARRGGBB) with a swatch. */
@Composable
fun ColorField(value: Color, onValueChange: (Color) -> Unit) {
    var text by remember { mutableStateOf(value.toHex()) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value, focused) { if (!focused) text = value.toHex() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(22.dp)
                .background(value, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(6.dp))
        CompactTextField(
            value = text,
            onValueChange = { t -> text = t; parseHexColor(t)?.let(onValueChange) },
            modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused },
        )
    }
}

@Composable
fun ChoiceField(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text(options.getOrElse(selected) { "?" } + "  ▾", fontSize = 12.sp) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEachIndexed { i, name ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(i); open = false })
            }
        }
    }
}

fun Color.toHex(): String = "#%08X".format(toArgb()).let { if (it.startsWith("#FF")) "#" + it.substring(3) else it }

fun parseHexColor(text: String): Color? {
    val hex = text.trim().removePrefix("#")
    val argb = when (hex.length) {
        6 -> hex.toLongOrNull(16)?.or(0xFF000000)
        8 -> hex.toLongOrNull(16)
        else -> null
    } ?: return null
    return Color(argb.toInt())
}
