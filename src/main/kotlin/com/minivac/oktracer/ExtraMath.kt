package com.minivac.oktracer

fun computeTangentsAndBiTangents(vertices: List<vec3>,
                                 normals: List<vec3>,
                                 texCoords: List<vec2>,
                                 indexes: List<Short>): Pair<List<vec3>, List<vec3>> {

  if (vertices.size != normals.size || vertices.size != texCoords.size) {
    throw IllegalArgumentException()
  }
  val tangents = Array(vertices.size, { vec3.create() }).toMutableList()
  val bitangents = Array(vertices.size, { vec3.create() }).toMutableList()

  indexes.windowed(3, 3) { (i0, i1, i2) ->
    val v0 = vertices[i0.toInt()]
    val v1 = vertices[i1.toInt()]
    val v2 = vertices[i2.toInt()]

    val uv0 = texCoords[i0.toInt()]
    val uv1 = texCoords[i1.toInt()]
    val uv2 = texCoords[i2.toInt()]

    val n0 = normals[i0.toInt()]
    val n1 = normals[i1.toInt()]
    val n2 = normals[i2.toInt()]

    val deltaPos1 = v1 - v0
    val deltaPos2 = v2 - v0

    val deltaUV1 = uv1 - uv0
    val deltaUV2 = uv2 - uv0

    val r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x)
    val tangent = (deltaPos1 * deltaUV2.y - deltaPos2 * deltaUV1.y) * r
    val bitangent = (deltaPos2 * deltaUV1.x - deltaPos1 * deltaUV2.x) * r

    tangents[i0.toInt()] = tangent
    tangents[i1.toInt()] = tangent
    tangents[i2.toInt()] = tangent

    bitangents[i0.toInt()] = bitangent
    bitangents[i1.toInt()] = bitangent
    bitangents[i2.toInt()] = bitangent
    //Normalize

    //Dummy
    0
  }

  //Make them orthogonal
  tangents.forEachIndexed { i, t ->
    val n = normals[i]
    val b = bitangents[i]
    val o = (t - n * (n dot t)).normalize()
    val flipped = if ((n cross t) dot b < 0) {
      o * (-1f)
    } else {
      o
    }
    tangents[i] = flipped
  }

  return tangents to bitangents
}