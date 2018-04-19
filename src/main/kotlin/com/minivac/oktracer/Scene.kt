package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.math.cos
import kotlin.math.sin

class Scene {
    val cameraX = 0f
    val cameraY = 0f
    val cameraZ = 10f

    private val lights = Array(MAX_LIGHTS, { _ -> Light() })

    var t = 0f

    private val meshes: List<Mesh<*>> = listOf(
            Cube().transform {
                scale.setXYZ(0.6f)
            },
            Sphere().transform {
                translation[0] = -2.05f
            },
            Sphere().transform {
                translation[0] = 2.05f
            }

    )

    init {
        lights[0].position[0] = 1.5f
        lights[0].color.set(arrayOf(1f, 1f, 1f))
    }

    fun render() {
        program.useProgram()

        loadCameraTransforms()
        loadLights()

        t += FPI / 360f

        val d1 = 1.5f
        lights[0].position.set(arrayOf(d1 * cos(t), d1 * cos(t * 4), d1 * sin(t)))
        lights[0].color.set(arrayOf(1f, 0f, 0.33f))
        lights[0].ambient = 0.3f

        val d2 = 1.1f
        lights[1].position.set(arrayOf(d2 * cos(2 * t), 0f, d2 * sin(2 * t)))
        lights[1].color.set(arrayOf(0f, 0.8f, 0f))
        lights[1].ambient = 0.1f

        meshes.filter { it is Cube }.forEach {
            it.transform {
                rotation.setX(2 * t)
                rotation.setY(t)
                //rotation = vec3.fromValues(t * 2, t, t * 4)
            }
        }
        meshes.forEach { it.render() }
    }

    private fun loadCameraTransforms() {
        mat4.perspective(program.projectionMatrix, 0.5f, canvas.width.toFloat() / canvas.height, 1f, 100f)
        mat4.lookAt(
                out = program.cameraMatrix,
                eye = vec3.fromValues(cameraX, cameraY, cameraZ),
                center = vec3.fromValues(0f, cameraY, 0f), //Look at the origin in a straight line
                up = vec3.fromValues(0f, 1f, 0f))
        gl.uniformMatrix4fv(program.projectionMatrixLocation, false, program.projectionMatrix)
        gl.uniformMatrix4fv(program.cameraMatrixLocation, false, program.cameraMatrix)
    }

    private fun loadLights() {
        val position = lights.flatMap { it.position.asFloatArray().toList() }.toTypedArray()
        val color = lights.flatMap { it.color.asFloatArray().toList() }.toTypedArray()
        val ambient = lights.flatMap { light -> light.color.asFloatArray().map { it * light.ambient }.toList() }.toTypedArray()
        gl.uniform3fv(program.lightPositionLocation, position)
        gl.uniform3fv(program.lightColorLocation, color)
        gl.uniform3fv(program.lightAmbientLocation, ambient)
    }

    fun clear() {
        gl.clearColor(0.3f, 0.3f, 0.3f, 1.0f)
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT or WebGLRenderingContext.DEPTH_BUFFER_BIT)
    }
}
