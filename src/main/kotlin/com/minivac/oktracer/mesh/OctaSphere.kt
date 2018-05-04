package com.minivac.oktracer.mesh


private object OctahedronData {
  val vertices = arrayOf(
      //Top half
      +0f, +1f, +0f,

      +0f, +0f, -1f,
      +1f, +0f, +0f,
      +0f, +0f, +1f,
      -1f, +0f, +0f,

      +0f, -1f, +0f
  )

  val elements = arrayOf<Short>(
      0, 1, 2,
      0, 2, 3,
      0, 3, 4,
      0, 4, 1,

      5, 1, 2,
      5, 2, 3,
      5, 3, 4,
      5, 4, 1
  )
}

/**
 * Split Octahedron in midpoints, after N iterations we got nice a sphere
 */
class OctaSphere(val iterations: Int) : Mesh() {
  init {
    TODO()
  }
}