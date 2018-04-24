package com.minivac.oktracer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE0
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE1
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE2
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.get
import kotlin.math.cos
import kotlin.math.sin

abstract class Mesh<T : Mesh<T>> {
  val modelMatrix: mat4 = mat4.create()
  val normalMatrix: mat3 = mat3.create()

  var scale: vec3 = vec3.fromValues(1f, 1f, 1f)
  var rotation: vec3 = vec3.create()
  var translation: vec3 = vec3.create()

  val verticesBuffer: WebGLBuffer = gl.createBuffer()!!
  val drawOrderBuffer: WebGLBuffer = gl.createBuffer()!!
  val normalBuffer: WebGLBuffer = gl.createBuffer()!!
  val texCoordBuffer: WebGLBuffer = gl.createBuffer()!!

  var verticesCount = 0
  var drawOrderCount = 0

  var material: Material = Materials.metal

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

  protected fun bindTexCoordBuffer(texCoords: Array<Float>) {
    gl.bindBuffer(ARRAY_BUFFER, texCoordBuffer)
    gl.bufferData(ARRAY_BUFFER, Float32Array(texCoords), STATIC_DRAW)
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

    gl.bindBuffer(ARRAY_BUFFER, texCoordBuffer)
    gl.vertexAttribPointer(program.texCoordLocation, 2, FLOAT, false, 0, 0)

    gl.activeTexture(TEXTURE0)
    gl.bindTexture(TEXTURE_2D, material.color.texture)
    gl.uniform1i(program.colorSampler, 0)

    gl.activeTexture(TEXTURE1)
    gl.bindTexture(TEXTURE_2D, material.normal.texture)
    gl.uniform1i(program.normalSampler, 1)

    gl.activeTexture(TEXTURE2)
    gl.bindTexture(TEXTURE_2D, material.displacement.texture)
    gl.uniform1i(program.displacementSampler, 2)
    gl.uniform1f(program.displacementCoefficient, material.displacementCoefficient)

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
    bindNormalsBuffer(CubeData.normals)
    bindTexCoordBuffer(CubeData.texture)
    bindDrawOrderBuffer(CubeData.drawOrder)
  }
}


/**
 * Split Octahedron in midpoints, after N iterations we got nice a sphere
 */
class Sphere(val xc: Int = 64, val yc: Int = 64) : Mesh<Sphere>() {

  private var maxDrawOrder = -1

  init {
    val vertexCount = xc * yc
    val facesCount = vertexCount / 2

    var vertices: MutableList<Float> = ArrayList(vertexCount)
    val textureCoords: MutableList<Float> = ArrayList(vertexCount)
    val drawOrder = ArrayList<Short>(facesCount * 3)

    val stepX = 1f / xc
    val stepY = 1f / yc

    for (i in 0..xc) {
      for (j in 0..yc) {

        val a = TAU * i * stepX
        val b = PI * j * stepY

        //Sphere
        val x = cos(a) * sin(b)
        val y = cos(b)
        val z = sin(a) * sin(b)

        val u = 1f - a / TAU
        val v = b / PI

        vertices.addAll(listOf(x, y, z))
        textureCoords.addAll(listOf(u, v))
      }
    }

    //3 elements per face
    for (i in 0 until xc) {
      for (j in 0 until yc) {
        val v1 = (i + (xc + 1) * j).toShort()
        val v2 = (1 + v1).toShort()
        val v3 = (i + (xc + 1) * (j + 1)).toShort()
        val v4 = (1 + v3).toShort()
        drawOrder.addAll(listOf(v1, v2, v4, v1, v4, v3))
      }
    }

    maxDrawOrder = drawOrder.size
    //Normals are just norm vectors pointing from the center
    //to the vertex, so essentially the same as the vertex
    bindVerticesBuffer(vertices.toTypedArray())
    bindNormalsBuffer(vertices.toTypedArray())
    bindTexCoordBuffer(textureCoords.toTypedArray())
    bindDrawOrderBuffer(drawOrder.toTypedArray())
  }
}


//
////Compute polars
//val sphereTextures = sphereVertices
//    .windowed(3, 3)
//    .flatMap { (x, y, z) ->
//      listOf(atan2(y, x) / PI + 1f, asin(z) / PI + 0.5f)
//    }