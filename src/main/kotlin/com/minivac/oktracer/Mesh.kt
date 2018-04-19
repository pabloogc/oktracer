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

abstract class Mesh<T : Mesh<T>> {
    val modelMatrix: mat4 = mat4.create()
    var scale: vec3 = vec3.fromValues(1f, 1f, 1f)
    var rotation: vec3 = vec3.create()
    var translation: vec3 = vec3.create()

    val verticesBuffer: WebGLBuffer = gl.createBuffer()!!
    val drawOrderBuffer: WebGLBuffer = gl.createBuffer()!!
    val normalBuffer: WebGLBuffer = gl.createBuffer()!!

    var verticesCount = 0
    var drawOrderCount = 0

    @Suppress("UNCHECKED_CAST")
    fun transform(fn: T.() -> Unit): T {
        fn(this as T)
        updateTransform()
        return this
    }

    private fun updateTransform() {
        mat4.identity(modelMatrix)
        mat4.translate(modelMatrix, modelMatrix, translation)
        mat4.scale(modelMatrix, modelMatrix, scale)
        mat4.rotateX(modelMatrix, modelMatrix, rotation[0])
        mat4.rotateY(modelMatrix, modelMatrix, rotation[1])
        mat4.rotateZ(modelMatrix, modelMatrix, rotation[2])
    }

    protected fun bindVerticesBuffer(vertices: Array<Float>) {
        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.bufferData(ARRAY_BUFFER, Float32Array(vertices), STATIC_DRAW)
        verticesCount = vertices.size
    }

    protected fun bindNormalsBuffer(normals: Array<Float>) {
        gl.bindBuffer(ARRAY_BUFFER, normalBuffer)
        gl.bufferData(ARRAY_BUFFER, Float32Array(normals), STATIC_DRAW)
    }

    protected fun bindDrawOrderBuffer(indexes: Array<Short>) {
        gl.bindBuffer(ELEMENT_ARRAY_BUFFER, drawOrderBuffer)
        gl.bufferData(ELEMENT_ARRAY_BUFFER, Uint16Array(indexes), STATIC_DRAW)
        drawOrderCount = indexes.count()
    }

    open fun render() {
        gl.uniformMatrix4fv(program.modelViewMatrixLocation, false, modelMatrix)

        gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
        gl.vertexAttribPointer(program.vertexPositionLocation, 3, FLOAT, false, 0, 0)

        gl.bindBuffer(ARRAY_BUFFER, normalBuffer)
        gl.vertexAttribPointer(program.normalLocation, 3, FLOAT, false, 0, 0)

        gl.bindBuffer(ELEMENT_ARRAY_BUFFER, drawOrderBuffer)
        gl.drawElements(TRIANGLES, drawOrderCount, UNSIGNED_SHORT, 0)
    }
}

class Triangle(vertices: Array<Float> = ISOSCELES_VERTICES) : Mesh<Triangle>() {

    companion object {
        val ISOSCELES_VERTICES = arrayOf(
                +0f, +1f, +0f,
                -1f, -1f, +0f,
                +1f, -1f, +0f
        )
    }

    init {
        if (vertices.size != 9) throw IllegalArgumentException()
        bindVerticesBuffer(vertices)
        bindDrawOrderBuffer(arrayOf(0, 1, 2))
    }
}

/**
 * Front and back, bottom left first, counter clockwise
 */
class Cube(vertices: Array<Float> = CUBE_VERTICES) : Mesh<Cube>() {
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
                0, 1, 2, //Front
                0, 2, 3,
                1, 5, 6, //Right
                1, 6, 2,
                5, 4, 7, //Back
                5, 7, 6,
                4, 3, 7, //Left
                4, 0, 3,
                0, 1, 5, //Bottom
                0, 5, 4,
                3, 2, 6, //Top
                3, 6, 7
        )
    }

    init {
        if (vertices.size != CUBE_VERTICES.size) throw IllegalArgumentException()
        bindVerticesBuffer(vertices)
        bindNormalsBuffer(vertices) //This normals are wrong for cubes
        bindDrawOrderBuffer(DRAW_ORDER)
    }
}

/**
 * Split Octahedron in midpoints, after N iterations we got nice a sphere
 */
class Sphere(iterations: Int = 4) : Mesh<Sphere>() {
    companion object {
        val OCTAHEDRON_VERTICES = arrayOf(
                +0f, +1f, +0f,
                +0f, +0f, -1f,
                +1f, +0f, +0f,
                +0f, +0f, +1f,
                -1f, +0f, +0f,
                +0f, -1f, +0f
        )

        private val OCTAHEDRON_DRAW_ORDER = arrayOf<Short>(
                0, 1, 2, //Top
                0, 2, 3,
                0, 3, 4,
                0, 4, 1,
                5, 1, 2, //Bottom
                5, 2, 3,
                5, 3, 4,
                5, 4, 1
        )
    }

    init {
        val sphereVertices: MutableList<Float> = OCTAHEDRON_VERTICES.asList().toMutableList()
        var sphereDrawOrder = OCTAHEDRON_DRAW_ORDER.asList()

        for (i in 0..iterations) {
            sphereDrawOrder = sphereDrawOrder
                    .windowed(3, 3)
                    .flatMap { (i1, i2, i3) ->

                        val v1 = vec3.fromValues(
                                sphereVertices[i1 * 3 + 0],
                                sphereVertices[i1 * 3 + 1],
                                sphereVertices[i1 * 3 + 2])
                        val v2 = vec3.fromValues(
                                sphereVertices[i2 * 3 + 0],
                                sphereVertices[i2 * 3 + 1],
                                sphereVertices[i2 * 3 + 2])
                        val v3 = vec3.fromValues(
                                sphereVertices[i3 * 3 + 0],
                                sphereVertices[i3 * 3 + 1],
                                sphereVertices[i3 * 3 + 2])

                        //Bisect sides
                        val v12 = v1 midPoint v2
                        val v13 = v1 midPoint v3
                        val v23 = v2 midPoint v3

                        vec3.normalize(v12, v12)
                        vec3.normalize(v13, v13)
                        vec3.normalize(v23, v23)

                        val i12 = (sphereVertices.size / 3 + 0).toShort()
                        val i13 = (sphereVertices.size / 3 + 1).toShort()
                        val i23 = (sphereVertices.size / 3 + 2).toShort()

                        sphereVertices.addAll(listOf(v12[0], v12[1], v12[2]))
                        sphereVertices.addAll(listOf(v13[0], v13[1], v13[2]))
                        sphereVertices.addAll(listOf(v23[0], v23[1], v23[2]))

                        val out = listOf(
                                i1, i12, i13,
                                i12, i2, i23,
                                i13, i23, i3,
                                i12, i23, i13
                        )

                        out
                    }
        }

        //Normals are just norm vectors pointing from the center
        //to the vertex, so essentially the same as the vertex
        bindVerticesBuffer(sphereVertices.toTypedArray())
        bindNormalsBuffer(sphereVertices.toTypedArray())
        bindDrawOrderBuffer(sphereDrawOrder.toTypedArray())
    }
}