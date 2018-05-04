package com.minivac.oktracer.mesh

import com.minivac.oktracer.matrix.vec2
import com.minivac.oktracer.matrix.vec3


private object CubeData {
  val vertices = listOf(
      vec3(-1.0f, -1.0f, +1.0f),
      vec3(+1.0f, -1.0f, +1.0f),
      vec3(+1.0f, +1.0f, +1.0f),
      vec3(-1.0f, +1.0f, +1.0f),
      vec3(-1.0f, -1.0f, -1.0f),
      vec3(-1.0f, +1.0f, -1.0f),
      vec3(+1.0f, +1.0f, -1.0f),
      vec3(+1.0f, -1.0f, -1.0f),
      vec3(-1.0f, +1.0f, -1.0f),
      vec3(-1.0f, +1.0f, +1.0f),
      vec3(+1.0f, +1.0f, +1.0f),
      vec3(+1.0f, +1.0f, -1.0f),
      vec3(-1.0f, -1.0f, -1.0f),
      vec3(+1.0f, -1.0f, -1.0f),
      vec3(+1.0f, -1.0f, +1.0f),
      vec3(-1.0f, -1.0f, +1.0f),
      vec3(+1.0f, -1.0f, -1.0f),
      vec3(+1.0f, +1.0f, -1.0f),
      vec3(+1.0f, +1.0f, +1.0f),
      vec3(+1.0f, -1.0f, +1.0f),
      vec3(-1.0f, -1.0f, -1.0f),
      vec3(-1.0f, -1.0f, +1.0f),
      vec3(-1.0f, +1.0f, +1.0f),
      vec3(-1.0f, +1.0f, -1.0f)
  )
  val normals = listOf(
      vec3(0.0f, 0.0f, 1.0f),
      vec3(0.0f, 0.0f, 1.0f),
      vec3(0.0f, 0.0f, 1.0f),
      vec3(0.0f, 0.0f, 1.0f),
      vec3(0.0f, 0.0f, -1.0f),
      vec3(0.0f, 0.0f, -1.0f),
      vec3(0.0f, 0.0f, -1.0f),
      vec3(0.0f, 0.0f, -1.0f),
      vec3(0.0f, 1.0f, 0.0f),
      vec3(0.0f, 1.0f, 0.0f),
      vec3(0.0f, 1.0f, 0.0f),
      vec3(0.0f, 1.0f, 0.0f),
      vec3(0.0f, -1.0f, 0.0f),
      vec3(0.0f, -1.0f, 0.0f),
      vec3(0.0f, -1.0f, 0.0f),
      vec3(0.0f, -1.0f, 0.0f),
      vec3(1.0f, 0.0f, 0.0f),
      vec3(1.0f, 0.0f, 0.0f),
      vec3(1.0f, 0.0f, 0.0f),
      vec3(1.0f, 0.0f, 0.0f),
      vec3(-1.0f, 0.0f, 0.0f),
      vec3(-1.0f, 0.0f, 0.0f),
      vec3(-1.0f, 0.0f, 0.0f),
      vec3(-1.0f, 0.0f, 0.0f)
  )

  val texture = listOf(
      vec2(0.0f, 0.0f),
      vec2(1.0f, 0.0f),
      vec2(1.0f, 1.0f),
      vec2(0.0f, 1.0f),
      vec2(0.0f, 0.0f),
      vec2(1.0f, 0.0f),
      vec2(1.0f, 1.0f),
      vec2(0.0f, 1.0f),
      vec2(0.0f, 0.0f),
      vec2(1.0f, 0.0f),
      vec2(1.0f, 1.0f),
      vec2(0.0f, 1.0f),
      vec2(0.0f, 0.0f),
      vec2(1.0f, 0.0f),
      vec2(1.0f, 1.0f),
      vec2(0.0f, 1.0f),
      vec2(0.0f, 0.0f),
      vec2(1.0f, 0.0f),
      vec2(1.0f, 1.0f),
      vec2(0.0f, 1.0f),
      vec2(0.0f, 0.0f),
      vec2(1.0f, 0.0f),
      vec2(1.0f, 1.0f),
      vec2(0.0f, 1.0f)
  )
  val elements = listOf<Short>(
      0, 1, 2, 0, 2, 3,    // front
      4, 5, 6, 4, 6, 7,    // back
      8, 9, 10, 8, 10, 11,   // top
      12, 13, 14, 12, 14, 15,   // bottom
      16, 17, 18, 16, 18, 19,   // right
      20, 21, 22, 20, 22, 23   // left
  )
}


/**
 * Front and back, bottom left first, counter clockwise
 */
class Cube : Mesh() {
  init {
    bindData(MeshData(
        vertices = CubeData.vertices,
        normals = CubeData.normals,
        texCoords = CubeData.texture,
        elements = CubeData.elements
    ))
  }
}

