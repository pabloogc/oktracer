package com.minivac.oktracer.matrix

import org.khronos.webgl.Float32Array

external class mat3 : Float32Array {
  companion object {
    fun create(): mat3
    fun normalFromMat4(out: mat3, a: mat4)
  }
}