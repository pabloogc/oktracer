package com.minivac.oktracer.mesh

import com.minivac.oktracer.matrix.vec2
import com.minivac.oktracer.matrix.vec3

class Grid(xc: Int = 10, yc: Int = 10, tc: Int = 3) : Mesh() {

  init {
    val vertexCount = xc * yc
    val facesCount = vertexCount / 2

    val vertices: MutableList<vec3> = ArrayList(vertexCount)
    val normals: MutableList<vec3> = ArrayList(vertexCount)
    val textureCoords: MutableList<vec2> = ArrayList(vertexCount)
    val indexes = ArrayList<Short>(facesCount * 3)

    val stepX = 2f / xc
    val stepY = 2f / yc

    for (i in 0..xc) {
      for (j in 0..yc) {
        val a = -1 + i * stepX
        val b = -1 + j * stepY
        //Sphere
        val x = a
        val y = b
        val z = 0f

        val u = (a + 1f) / 2f
        val v = (b + 1f) / 2f

        vertices.add(vec3(x, y, z))
        normals.add(vec3(0f, 0f, 1f))
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
        tesselate(tc, MeshData(
            vertices = vertices,
            normals = normals,
            texCoords = textureCoords,
            elements = indexes
        ))
    )
  }
}