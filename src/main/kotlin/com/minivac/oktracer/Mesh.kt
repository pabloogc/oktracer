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
    val normalMatrix: mat3 = mat3.create()

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

        mat3.normalFromMat4(normalMatrix, modelMatrix)
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
        gl.uniformMatrix3fv(program.normalMatrixLocation, false, normalMatrix)

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
class Cube : Mesh<Cube>() {
    init {
        bindVerticesBuffer(CubeData.vertices)
        bindNormalsBuffer(CubeData.normals) //This normals are wrong for cubes
        bindDrawOrderBuffer(CubeData.drawOrder)
    }
}

/**
 * Split Octahedron in midpoints, after N iterations we got nice a sphere
 */
class Sphere(iterations: Int = 4) : Mesh<Sphere>() {
    init {
        val sphereVertices: MutableList<Float> = OctahedronData.vertices.asList().toMutableList()
        var sphereDrawOrder = OctahedronData.drawOrder.asList()

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
                        val v12 = (v1 midPoint v2).normalize()
                        val v13 = (v1 midPoint v3).normalize()
                        val v23 = (v2 midPoint v3).normalize()

                        val i12 = (sphereVertices.size / 3 + 0).toShort()
                        val i13 = (sphereVertices.size / 3 + 1).toShort()
                        val i23 = (sphereVertices.size / 3 + 2).toShort()

                        sphereVertices.addAll(v12.toFloatArray())
                        sphereVertices.addAll(v13.toFloatArray())
                        sphereVertices.addAll(v23.toFloatArray())

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