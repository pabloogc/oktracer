package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import kotlin.math.cos
import kotlin.math.sin

const val MAX_LIGHTS = 1

class Scene {
  var cameraX = 0f
  var cameraY = 1f
  var cameraZ = 10f

  private val lights = Array(MAX_LIGHTS, { _ -> Light() })

  var t = 0f
  val axisWidth = 0.005f
  val axisLength = 10f

  private val meshes: List<Mesh<*>> = listOf(
      Sphere().transform {
        translation.y = 0f
      },
      //Axis
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
      },
      Sphere().transform {
        scale.setXYZ(0.01f)
      }
  )

  fun render() {
    program.useProgram()

    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.depthFunc(WebGLRenderingContext.LEQUAL)

    loadCameraTransforms()
    loadLights()

    t += PI / 360f

    val d = 10
    cameraX = d * cos(t)
    cameraZ = d * sin(t)

    val intensity = 1f
    //lights[0].position.set(cos(t) * 2, cos(4 * t), cos(t * 3.3f) * 5f)
    lights[0].color.set(intensity, intensity, intensity)
    //lights[0].ambientCoefficient = (cos(t) + 1) / 16f
    lights[0].ambientCoefficient = 0.3f
    lights[0].position.set(cameraX, 3f, cameraZ)
    meshes.last().transform {
      translation.set(lights[0].position)
    }

    meshes.dropLast(4).forEach {
      it.transform {
        //rotation.y = PI - PI / 4
        //rotation.z = t
        translation.y = 0f
      }
    }

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
