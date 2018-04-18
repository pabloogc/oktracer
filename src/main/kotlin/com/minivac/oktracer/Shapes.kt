package com.minivac.oktracer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.get

abstract class Shape {
    val transform: mat4 = mat4.create()
    var scale: vec3 = vec3.fromValues(1f, 1f, 1f)
    var rotation: vec3 = vec3.create()
    var translation: vec3 = vec3.create()
    //fun render()

    fun calculateTransform() {
        mat4.identity(transform)
        mat4.scale(transform, transform, scale)
        mat4.translate(transform, transform, translation)
        mat4.rotateX(transform, transform, rotation[0])
        mat4.rotateY(transform, transform, rotation[1])
        mat4.rotateZ(transform, transform, rotation[2])
    }
}

class Triangle(vertices: Array<Float>) : Shape() {

    companion object {
        val ISOSCELES_VERTICES = arrayOf(
                0.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f
        )
    }

    private val verticesBuffer: WebGLBuffer

    init {
        if (vertices.size != 9) throw IllegalArgumentException()
        verticesBuffer = gl.createBuffer()!!
        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        val vertexFloat32Array = Float32Array(vertices)
        gl.bufferData(ARRAY_BUFFER, vertexFloat32Array, STATIC_DRAW)
    }

    fun render(program: Program) {
        calculateTransform()
        gl.uniformMatrix4fv(program.modelViewMatrixLocation, false, transform)

        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.vertexAttribPointer(program.vertexPositionLocation, 3, FLOAT, false, 0, 0)
        gl.drawArrays(TRIANGLES, 0, 3)
    }
}

class Sphere(val radius: Float)