package com.minivac.oktracer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.get

abstract class Shape<T : Shape<T>> {
    val modelMatrix: mat4 = mat4.create()
    var scale: vec3 = vec3.fromValues(1f, 1f, 1f)
    var rotation: vec3 = vec3.create()
    var translation: vec3 = vec3.create()

    abstract fun render()

    @Suppress("UNCHECKED_CAST")
    fun transform(fn: T.() -> Unit): T {
        fn(this as T)
        updateTransform()
        return this
    }

    fun updateTransform() {
        mat4.identity(modelMatrix)
        mat4.scale(modelMatrix, modelMatrix, scale)
        mat4.translate(modelMatrix, modelMatrix, translation)
        mat4.rotateX(modelMatrix, modelMatrix, rotation[0])
        mat4.rotateY(modelMatrix, modelMatrix, rotation[1])
        mat4.rotateZ(modelMatrix, modelMatrix, rotation[2])
    }
}

class Triangle(vertices: Array<Float> = ISOSCELES_VERTICES) : Shape<Triangle>() {

    companion object {
        val ISOSCELES_VERTICES = arrayOf(
                +0f, +1f, +0f,
                -1f, -1f, +0f,
                +1f, -1f, +0f
        )
    }

    private val verticesBuffer: WebGLBuffer

    init {
        if (vertices.size != 9) throw IllegalArgumentException()
        verticesBuffer = gl.createBuffer()!!
        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.bufferData(ARRAY_BUFFER, Float32Array(vertices), STATIC_DRAW)
    }

    override fun render() {
        gl.uniformMatrix4fv(program.modelViewMatrixLocation, false, modelMatrix)

        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.vertexAttribPointer(program.vertexPositionLocation, 3, FLOAT, false, 0, 0)
        gl.drawArrays(TRIANGLES, 0, 3)
    }
}

/**
 * Front and back, bottom left first, counter clockwise
 */
class Cube(vertices: Array<Float> = CUBE_VERTICES) : Shape<Cube>() {
    companion object {
        val CUBE_VERTICES = arrayOf(
                -1f, -1f, +1f, //front
                +1f, -1f, +1f,
                +1f, +1f, +1f,
                -1f, +1f, +1f,
                -1f, -1f, -1f, //back
                +1f, -1f, -1f,
                +1f, +1f, -1f,
                -1f, +1f, -1f
        )

        private val DRAW_ORDER = arrayOf<Short>(
                //Front
                0, 1, 2,
                0, 2, 3,
                //Right
                1, 5, 6,
                1, 6, 2,
                //Back
                5, 4, 7,
                5, 7, 6,
                //Left
                4, 3, 7,
                4, 0, 3,
                //Bottom
                0, 1, 5,
                0, 5, 4,
                //Top
                3, 2, 6,
                3, 6, 7

        )
    }


    private val verticesBuffer: WebGLBuffer
    private val drawOrderBuffer: WebGLBuffer

    init {
        if (vertices.size != CUBE_VERTICES.size) throw IllegalArgumentException()
        verticesBuffer = gl.createBuffer()!!
        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.bufferData(ARRAY_BUFFER, Float32Array(vertices), STATIC_DRAW)

        drawOrderBuffer = gl.createBuffer()!!
        gl.bindBuffer(ELEMENT_ARRAY_BUFFER, drawOrderBuffer)
        gl.bufferData(ELEMENT_ARRAY_BUFFER, Uint16Array(DRAW_ORDER), STATIC_DRAW)
    }

    override fun render() {
        gl.uniformMatrix4fv(program.modelViewMatrixLocation, false, modelMatrix)

        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.vertexAttribPointer(program.vertexPositionLocation, 3, FLOAT, false, 0, 0)
//        gl.drawArrays(TRIANGLES, 0, 3)

        gl.bindBuffer(ELEMENT_ARRAY_BUFFER, drawOrderBuffer)
        gl.drawElements(TRIANGLES, DRAW_ORDER.size, UNSIGNED_SHORT, 0)
    }
}

class Sphere(val radius: Float)