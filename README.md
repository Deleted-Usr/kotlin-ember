# Ember

A small 2D game engine + editor written in Kotlin with Compose Multiplatform (Desktop).

This project was originally meant to be [Forge2D](github.com/Deleted-Usr/cpprust-forge2d). The engine was going to be
named Forge Ember, and was going to be written with Swing instead of Compose. I thought a game engine would be
a really good place to learn OpenGL, refine my C++ skills, and practise Rust, and so this project was left for a while
until I wanted to learn Compose Multiplatform.

```bash
./gradlew run     # launch the editor with the demo platformer
./gradlew test    # engine tests
```

**Editor:** Play / Pause / Step / Stop (Stop restores the scene as it was before Play), hierarchy,
inspector with live values, console, scene viewport (click to select, drag to move, scroll to zoom,
drag empty space to pan, Ctrl or *Snap* to snap to grid). Click the viewport to give it keyboard focus.

## Layout

```
engine/
  math/Vec2.kt                 immutable 2D vector (world space is Y-down)
  core/Engine.kt               play state, frame tick, edit snapshot, log
  core/Scene.kt                object list, update/draw, deferred add/destroy, picking
  core/GameObject.kt           name + Transform + components
  core/Component.kt            base class: onStart/onUpdate/onDraw/onCollision/onTrigger, properties(), copy()
  core/Property.kt             inspector-editable value types
  core/Input.kt                keyboard state (isDown / wasPressed / horizontal())
  core/ComponentRegistry.kt    what "Add Component" offers
  render/Camera2D.kt, Renderer.kt
  physics/Physics2D.kt         AABB physics: gravity, push-out resolution, grounded, triggers
  components/                  ShapeRenderer, RigidBody, BoxCollider
editor/                        Compose UI (toolbar, hierarchy, viewport, inspector, console)
demo/                          demo scene + example scripts (PlayerController, Spinner, Collectible)
```

## Writing a component

```kotlin
class Bouncer(var height: Float = 40f) : Component() {
    override fun onUpdate(dt: Float) {
        transform.position = transform.position.copy(y = sin(time * 3f) * height)
    }
    override fun properties() = listOf(FloatProperty("Height", { height }, { height = it }))
    override fun copy() = Bouncer(height)   // used for the Play/Stop snapshot
}

ComponentRegistry.register("Bouncer") { Bouncer() }
```

## Next steps

- Scene save/load (kotlinx.serialization over `properties()` or a dedicated schema)
- Sprites / textures and an asset browser
- Undo/redo for editor edits
- Sturdier physics (swept AABB, broadphase, or a Box2D binding)
- Camera follow component, audio, tilemaps
