package com.minivac.oktracer.mesh

import com.minivac.oktracer.Material
import com.minivac.oktracer.Materials
import com.minivac.oktracer.gl
import com.minivac.oktracer.matrix.*
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.get

data class MeshData(
    val vertices: List<vec3>,
    val normals: List<vec3>,
    val texCoords: List<vec2>,
    val elements: List<Short>
)

abstract class Mesh {
  val modelMatrix: mat4 = mat4.create()
  val normalMatrix: mat3 = mat3.create()

  var scale: vec3 = vec3(1f, 1f, 1f)
  var rotation: vec3 = vec3.create()
  var translation: vec3 = vec3.create()

  val verticesBuffer: WebGLBuffer = gl.createBuffer()!!
  val tangentBuffer: WebGLBuffer = gl.createBuffer()!!
  val bitangentBuffer: WebGLBuffer = gl.createBuffer()!!
  val elementsBuffer: WebGLBuffer = gl.createBuffer()!!
  val normalBuffer: WebGLBuffer = gl.createBuffer()!!
  val texCoordBuffer: WebGLBuffer = gl.createBuffer()!!

  var elementsCount = 0
  var material: Material = Materials.metal

  @Suppress("UNCHECKED_CAST")
  fun <T> transform(fn: Mesh.() -> Unit): T {
    this.fn()
    updateTransform()
    return this as T
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

  protected fun bindData(meshData: MeshData) {
    bindVerticesBuffer(meshData.vertices.toFloatArray())
    bindNormalsBuffer(meshData.normals.toFloatArray())
    bindTexCoordBuffer(meshData.texCoords.toFloatArray())
    bindElements(meshData.elements.toTypedArray())
    val (tangents, bitangents) = computeTangentsAndBiTangents(meshData)
    bindTangentsBuffer(tangents.toFloatArray())
    bindBitangentsBuffer(bitangents.toFloatArray())
  }

  protected fun bindVerticesBuffer(vertices: Array<Float>) {
    gl.bindBuffer(ARRAY_BUFFER, verticesBuffer)
    gl.bufferData(ARRAY_BUFFER, Float32Array(vertices), STATIC_DRAW)
  }

  protected fun bindTangentsBuffer(tangents: Array<Float>) {
    gl.bindBuffer(ARRAY_BUFFER, tangentBuffer)
    gl.bufferData(ARRAY_BUFFER, Float32Array(tangents), STATIC_DRAW)
  }

  protected fun bindBitangentsBuffer(bitangents: Array<Float>) {
    gl.bindBuffer(ARRAY_BUFFER, bitangentBuffer)
    gl.bufferData(ARRAY_BUFFER, Float32Array(bitangents), STATIC_DRAW)
  }

  protected fun bindNormalsBuffer(normals: Array<Float>) {
    gl.bindBuffer(ARRAY_BUFFER, normalBuffer)
    gl.bufferData(ARRAY_BUFFER, Float32Array(normals), STATIC_DRAW)
  }

  protected fun bindTexCoordBuffer(texCoords: Array<Float>) {
    gl.bindBuffer(ARRAY_BUFFER, texCoordBuffer)
    gl.bufferData(ARRAY_BUFFER, Float32Array(texCoords), STATIC_DRAW)
  }

  protected fun bindElements(indexes: Array<Short>) {
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementsBuffer)
    gl.bufferData(ELEMENT_ARRAY_BUFFER, Uint16Array(indexes), STATIC_DRAW)
    elementsCount = indexes.count()
  }
}


