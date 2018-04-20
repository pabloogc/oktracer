package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.get
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

const val MAX_LIGHTS = 1

class Scene {
    val cameraX = 0f
    val cameraY = 0f
    val cameraZ = 10f

    private val lights = Array(MAX_LIGHTS, { _ -> Light() })

    var t = 0f

    private val meshes: List<Mesh<*>> = listOf(
            Sphere().transform {
                scale.setXYZ(0.05f)
            },

            Sphere().transform {
                translation.set(-2f, 1f, 0f)
            },
            Sphere().transform {
                translation.set(2f, 1f, 0f)
            },
            Sphere(2).transform {
                scale.setXYZ(0.25f)
            },
            Cube().transform {
                translation.set(-0.9f, -2f, 0f)
                scale.setXYZ(0.3f)
            },
            Cube().transform {
                translation.set(0f, -2f, 0f)
                scale.setXYZ(0.3f)
            },
            Cube().transform {
                translation.set(+0.9f, -2f, 0f)
                scale.setXYZ(0.3f)
            }
    )

    fun render() {
        program.useProgram()

        loadCameraTransforms()
        loadLights()

        t += FPI / 360f

        val intensity = (cos(t * 5.5f) + 1) + 0.50f
        lights[0].position.set(cos(t) * 2, cos(4 * t), cos(t * 3.3f) * 5f)
        lights[0].color.set(intensity, intensity, intensity)
        //lights[0].ambientCoefficient = (cos(t) + 1) / 16f
        lights[0].ambientCoefficient = 0.3f

        meshes[0].transform {
            translation.set(lights[0].position)
        }

        meshes.drop(1).forEach {
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
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT or WebGLRenderingContext.DEPTH_BUFFER_BIT)
    }
}
