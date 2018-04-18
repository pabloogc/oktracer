package com.minivac.oktracer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES

interface Shape {
    //fun render()
}

class Triangle(val vertices: Array<Float>) : Shape {

    companion object {
        val ISOSCELES_VERTICES = arrayOf<Float>(
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
        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.enableVertexAttribArray(program.vertexPositionLocation)
        gl.vertexAttribPointer(program.vertexPositionLocation, 3, FLOAT, false, 0, 0)
        gl.drawArrays(TRIANGLES, 0, 3)
    }
}

class Sphere(val radius: Float)