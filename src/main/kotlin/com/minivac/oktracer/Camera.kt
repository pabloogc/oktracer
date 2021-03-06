package com.minivac.oktracer

import com.minivac.oktracer.matrix.*
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document
import kotlin.math.cos
import kotlin.math.sin

class Camera {

  val position: vec3 = vec3(0f, 1f, 10f)
  val direction: vec3 = vec3.create()
  val projectionMatrix = mat4.create()
  val lookMatrix: mat4 = mat4.create()

  private val up: vec3 = vec3.create()
  private val side: vec3 = vec3.create()
  private val center: vec3 = vec3.create()
  private var polar: Float = -PI_HALF
  private var azimuth: Float = PI_HALF

  //Mouse controls
  private val mouseEventListener: EventListener
  private val keyEventListener: EventListener
  private var sensitivity = 0.001f * PI
  private var speed = 0.1f
  private var pressedKeys = mutableMapOf<String, Boolean>()

  init {
    mouseEventListener = EventListener {
      val event = it.asDynamic()

      val dx = (event.movementX.unsafeCast<Float>()) * sensitivity
      val dy = (event.movementY.unsafeCast<Float>()) * sensitivity

      polar = (polar + dx) % (TAU)
      val maxUp = PI / 60f
      azimuth = (azimuth + dy).clamp(maxUp, PI - maxUp)
    }

    keyEventListener = EventListener {
      val event = it as KeyboardEvent
      val pressed = it.type == "keydown"
      pressedKeys[event.key] = pressed
    }

    canvas.addEventListener("click", EventListener {
      canvas.asDynamic().requestPointerLock()
      Unit
    })

    document.addEventListener("pointerlockchange", EventListener {
      val element = document.asDynamic()
      if (element.pointerLockElement == null) {
        canvas.removeEventListener("mousemove", mouseEventListener)
        document.removeEventListener("keydown", keyEventListener)
        document.removeEventListener("keyup", keyEventListener)
        pressedKeys.clear()
      } else {
        canvas.addEventListener("mousemove", mouseEventListener)
        document.addEventListener("keydown", keyEventListener)
        document.addEventListener("keyup", keyEventListener)

      }
      Unit
    })
  }


  fun update() {
    mat4.perspective(projectionMatrix,
        0.5f,
        canvas.width.toFloat() / canvas.height,
        1f,
        100f)

    direction.set(
        x = cos(polar) * sin(azimuth),
        y = cos(azimuth),
        z = sin(polar) * sin(azimuth)
    )

    up.set(
        x = 0f,
        y = 1f,
        z = 0f
    )

    side.set(
        cos(polar + PI_HALF),
        0f,
        sin(polar + PI_HALF)
    )

    pressedKeys.forEach {
      if (it.value) {
        when (it.key) {
          "w" -> vec3.add(position, position, direction * speed)
          "s" -> vec3.add(position, position, direction * -speed)
          "a" -> vec3.add(position, position, side * -speed)
          "d" -> vec3.add(position, position, side * speed)
          " " -> vec3.add(position, position, VEC_Y * speed)
          "Shift" -> vec3.add(position, position, VEC_Y * -speed)
        }
      }
    }

    vec3.add(center, position, direction)

    mat4.lookAt(
        out = lookMatrix,
        eye = position,
        center = center,
        up = up)
  }
}