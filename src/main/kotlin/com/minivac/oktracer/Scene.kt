package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import kotlin.math.cos

const val MAX_LIGHTS = 2

class Scene {

  private val camera = Camera()
  private val projectionMatrix = mat4.create()
  private val lights = Array(MAX_LIGHTS, { _ -> Light() })

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

    updateCamera()
    updateLights()

    val intensity = 6f
    val distance = 2f
    lights[0].color.set(intensity, intensity, intensity)
    lights[0].ambientCoefficient = 5 * intensity / 255f
    lights[0].position.set(distance * cos(4 * t), 0f, -3f)

    lights[1].color.set(intensity, intensity, intensity)
    lights[1].ambientCoefficient = 0f
    lights[1].position.set(camera.position)

    meshes.forEach {
      it.transform {
        rotation.y = 2 * t
      }
    }

    //axis.forEach { it.render() }
    meshes.forEach { it.render() }
  }

  private fun updateCamera() {
    mat4.perspective(projectionMatrix, 0.5f, canvas.width.toFloat() / canvas.height, 1f, 100f)
    gl.uniformMatrix4fv(program.projectionMatrixLocation, false, projectionMatrix)
    gl.uniformMatrix4fv(program.cameraMatrixLocation, false, camera.update())
    gl.uniform3fv(program.eyePosition, camera.position)
    gl.uniform3fv(program.eyeDirection, camera.direction)
  }

  private fun updateLights() {
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
