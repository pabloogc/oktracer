package com.minivac.oktracer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW

abstract class Shape

class Triangle(val vertex: Array<Float>) : Shape() {

    private val positionBuffer: WebGLBuffer

    init {
        if (vertex.size != 9) throw IllegalArgumentException()
        positionBuffer = gl.createBuffer()!!
        gl.bindBuffer(ARRAY_BUFFER, positionBuffer)
        val vertexFloat32Array = Float32Array(vertex)
        gl.bufferData(ARRAY_BUFFER, vertexFloat32Array, STATIC_DRAW)
    }
}

class Sphere(val radius: Float)