package com.minivac.oktracer.mesh

import com.minivac.oktracer.PI
import com.minivac.oktracer.TAU
import com.minivac.oktracer.matrix.vec2
import com.minivac.oktracer.matrix.vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * Split Octahedron in midpoints, after N iterations we got nice a sphere
 */
class Sphere(xc: Int = 64, yc: Int = 64) : Mesh() {

  init {
    val vertexCount = xc * yc
    val facesCount = vertexCount / 2

    val vertices: MutableList<vec3> = ArrayList(vertexCount)
    val textureCoords: MutableList<vec2> = ArrayList(vertexCount)
    val indexes = ArrayList<Short>(facesCount * 3)

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

        vertices.add(vec3(x, y, z))
        textureCoords.add(vec2(u, v))
      }
    }

    //3 elements per face
    for (i in 0 until xc) {
      for (j in 0 until yc) {
        val v1 = (i + (xc + 1) * j).toShort()
        val v2 = (1 + v1).toShort()
        val v3 = (i + (xc + 1) * (j + 1)).toShort()
        val v4 = (1 + v3).toShort()
        indexes.addAll(listOf(v1, v2, v4, v1, v4, v3))
      }
    }

    //Normals are just norm vectors pointing from the center
    //to the vertex, so essentially the same as the vertex
    bindData(
        MeshData(
            vertices = vertices,
            normals = vertices,
            texCoords = textureCoords,
            elements = indexes
        )
    )
  }
}