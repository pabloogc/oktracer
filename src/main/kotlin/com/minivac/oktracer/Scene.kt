package com.minivac.oktracer

import com.minivac.oktracer.matrix.*
import com.minivac.oktracer.mesh.Cube
import com.minivac.oktracer.mesh.Grid
import com.minivac.oktracer.mesh.Mesh
import com.minivac.oktracer.mesh.Sphere
import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import kotlin.math.cos
import kotlin.math.sin

const val MAX_LIGHTS = 2

class Scene {

  private val camera = Camera()
  private val projectionMatrix = mat4.create()
  private val lights = Array(MAX_LIGHTS, { _ -> Light() })

  var t = 0f
  val axisWidth = 0.005f
  val axisLength = 10f

  private val meshes: List<Mesh> = listOf(
      Sphere().transform {
        translation.x = 2.2f
        material = Materials.metal
      },
      Sphere().transform {
        translation.x = -2.2f
        material = Materials.debug
      },
      Sphere().transform {
        translation.x = 0f
        material = Materials.stone
      }
//      Grid(xc = 1, yc = 1, tc = 2).transform {
//        translation.x = 0f
//        material = Materials.debug
//      }
//      Grid(xc = 1, yc = 1, tc = 2).transform {
//        translation.y = -1f
//        scale.set(100f, 1f, 100f)
//        rotation.x = 90f.toRad()
//        material = Materials.debug
//      }

  )

  private val axis: List<Mesh> = listOf(
      Cube().transform {
        material = Materials.debug
        scale.set(axisLength, axisWidth, axisWidth)
        translation.x = axisLength
      },
      Cube().transform {
        material = Materials.debug
        scale.set(axisWidth, axisLength, axisWidth)
        translation.y = axisLength
      },
      Cube().transform {
        material = Materials.debug
        scale.set(axisWidth, axisWidth, axisLength)
        translation.z = axisLength
      }
  )

  fun render() {
    t += PI / 360f

    camera.update()

    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)

    val intensity = 6f
    val distance = 2f
    lights[0].color.set(intensity, intensity, intensity)
    lights[0].ambientCoefficient = 5 * intensity / 255f
    lights[0].position.set(distance * cos(4 * t), 0f, -3f)

    lights[1].color.set(intensity, intensity, intensity)
    lights[1].ambientCoefficient = 0f
    lights[1].position.set(0f, distance * cos(t), distance * sin(t))
//    lights[1].position.set(camera.position)

    meshes.forEach {
      it.transform {
        //rotation.y = 2 * t
      }
    }

    defaultProgram.render(camera, meshes, lights)

  }

  fun clear() {
    gl.clearColor(0.3f, 0.3f, 0.3f, 1.0f)
    gl.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)
  }
}
