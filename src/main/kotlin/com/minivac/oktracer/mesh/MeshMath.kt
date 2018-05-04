package com.minivac.oktracer.mesh

import com.minivac.oktracer.matrix.*

/**
 * Split every triangle into 4 new triangles from the mid point.
 */
fun tesselate(iterations: Int, meshData: MeshData): MeshData {

  val newVertices = meshData.vertices.toMutableList()
  val newNormals = meshData.normals.toMutableList()
  val newTextCoords = meshData.texCoords.toMutableList()
  var newElements = meshData.elements

  for (i in 0 until iterations) {
    newElements = newElements
        .windowed(3, 3)
        .flatMap { (i1, i2, i3) ->

          val v1 = newVertices[i1.toInt()]
          val v2 = newVertices[i2.toInt()]
          val v3 = newVertices[i3.toInt()]

          val t1 = newTextCoords[i1.toInt()]
          val t2 = newTextCoords[i2.toInt()]
          val t3 = newTextCoords[i3.toInt()]

          val n1 = newNormals[i1.toInt()]
          val n2 = newNormals[i2.toInt()]
          val n3 = newNormals[i3.toInt()]

          //Bisect sides
          val v12 = v1 midPoint v2
          val v13 = v1 midPoint v3
          val v23 = v2 midPoint v3

          //Bisect textures
          val t12 = t1 midPoint t2
          val t13 = t1 midPoint t3
          val t23 = t2 midPoint t3

          //Normals
          val n12 = (n1 midPoint n2).normalize()
          val n13 = (n1 midPoint n3).normalize()
          val n23 = (n2 midPoint n3).normalize()

          //New vertices indexes
          val i12 = (newVertices.size + 0).toShort()
          val i13 = (newVertices.size + 1).toShort()
          val i23 = (newVertices.size + 2).toShort()

          //Add the new triangle data
          newVertices.add(v12)
          newVertices.add(v13)
          newVertices.add(v23)

          newTextCoords.add(t12)
          newTextCoords.add(t13)
          newTextCoords.add(t23)

          newNormals.add(n12)
          newNormals.add(n13)
          newNormals.add(n23)

          //Replace the triangle with the new 4
          listOf(
              i1, i12, i13,
              i12, i2, i23,
              i13, i23, i3,
              i12, i23, i13
          )
        }
  }

  return MeshData(
      vertices = newVertices,
      normals = newNormals,
      texCoords = newTextCoords,
      elements = newElements
  )
}

/**
 * Compute the tangents and bitangets from vertices and texture coordinates.
 */
fun computeTangentsAndBiTangents(meshData: MeshData): Pair<List<vec3>, List<vec3>> {

  val vertices = meshData.vertices
  val normals = meshData.normals
  val texCoords = meshData.texCoords
  val indexes = meshData.elements

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