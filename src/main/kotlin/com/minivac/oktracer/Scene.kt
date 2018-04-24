package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.window
import kotlin.math.cos
import kotlin.math.sin

const val MAX_LIGHTS = 1

class Scene {
  var cameraX = 0f
  var cameraY = 0f
  var cameraZ = -10f

  private val lights = Array(MAX_LIGHTS, { _ -> Light() })

  init {
    window.onkeyup = {
      val event = it as? KeyboardEvent
      event?.run {
        when (event.key) {
          "q" -> meshes.forEach { it.material = Materials.metal }
          "w" -> meshes.forEach { it.material = Materials.rock }
          "e" -> meshes.forEach { it.material = Materials.stone }
        }
      }
    }
  }

  var t = 0f
  val axisWidth = 0.005f
  val axisLength = 10f

  private val meshes: List<Mesh<*>> = listOf(
      Sphere().transform {
        translation.x = 2.2f
        material = Materials.metal
      },
      Sphere().transform {
        translation.x = -2.2f
        material = Materials.rock
      },
      Sphere().transform {
        translation.x = 0f
        material = Materials.stone
      }
//      Cube().transform {
//        material = Materials.metal
//        translation.x = 2f
//      }
  )

  private val axis: List<Mesh<*>> = listOf(
      Cube().transform {
        scale.set(axisLength, axisWidth, axisWidth)
        translation.x = axisLength
      },
      Cube().transform {
        scale.set(axisWidth, axisLength, axisWidth)
        translation.y = axisLength
      },
      Cube().transform {
        scale.set(axisWidth, axisWidth, axisLength)
        translation.z = axisLength
      }
  )

  fun render() {
    t += PI / 360f

    program.useProgram()

    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)

    loadCameraTransforms()
    loadLights()

    val d = 10
//    cameraX = d * cos(t)
//    cameraZ = d * sin(t)

    val intensity = 6f
    val distance = 2f
    lights[0].color.set(intensity, intensity, intensity)
    lights[0].ambientCoefficient = 5 * intensity / 255f
    lights[0].position.set(2 * cos(4 * t), 0f, -3f)

    meshes.forEach {
      it.transform {
        //        rotation.x = t
        rotation.y = 2 * t
//        translation.x = sin(t)
//        translation.y = sin(t)
      }
    }

    //axis.forEach { it.render() }
    meshes.forEach { it.render() }
  }

  private fun loadCameraTransforms() {
    mat4.perspective(program.projectionMatrix, 0.5f, canvas.width.toFloat() / canvas.height, 1f, 100f)
    val eye = vec3.fromValues(cameraX, cameraY, cameraZ)
    mat4.lookAt(
        out = program.cameraMatrix,
        eye = eye,
        center = vec3.fromValues(0f, cameraY, 0f), //Look at the origin in a straight line
        up = vec3.fromValues(0f, 1f, 0f))
    gl.uniformMatrix4fv(program.projectionMatrixLocation, false, program.projectionMatrix)
    gl.uniformMatrix4fv(program.cameraMatrixLocation, false, program.cameraMatrix)
    gl.uniform3fv(program.eyePosition, eye)
  }

  private fun loadLights() {
    val position = lights.flatMap { it.position.toFloatArray().toList() }.toTypedArray()
    val color = lights.flatMap { it.color.toFloatArray().toList() }.toTypedArray()
    val ambient = lights.map { it.ambientCoefficient }.toTypedArray()
    gl.uniform3fv(program.lightPositionLocation, position)
    gl.uniform3fv(program.lightColorLocation, color)
    gl.uniform1fv(program.lightAmbientCoefficient, ambient)
  }

  fun clear() {
    gl.clearColor(0.3f, 0.3f, 0.3f, 1.0f)
    gl.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)
  }
}
