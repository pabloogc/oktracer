package com.minivac.oktracer.matrix

import com.minivac.oktracer.PI
import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.math.max
import kotlin.math.min

val VEC_X = vec3(1f, 0f, 0f)
val VEC_Y = vec3(0f, 1f, 0f)
val VEC_Z = vec3(0f, 0f, 1f)

//Geometry

var Float32Array.x: Float
  get() = this[0]
  set(value) {
    this[0] = value
  }

var Float32Array.y: Float
  get() = this[1]
  set(value) {
    this[1] = value
  }

var Float32Array.z: Float
  get() = this[2]
  set(value) {
    this[2] = value
  }

var Float32Array.w: Float
  get() = this[3]
  set(value) {
    this[3] = value
  }

//Textures

var Float32Array.s: Float
  get() = this[0]
  set(value) {
    this[0] = value
  }

var Float32Array.t: Float
  get() = this[1]
  set(value) {
    this[1] = value
  }

var Float32Array.u: Float
  get() = this[0]
  set(value) {
    this[0] = value
  }

var Float32Array.v: Float
  get() = this[1]
  set(value) {
    this[1] = value
  }

//Colors

var Float32Array.r: Float
  get() = this[0]
  set(value) {
    this[0] = value
  }

var Float32Array.g: Float
  get() = this[1]
  set(value) {
    this[1] = value
  }

var Float32Array.b: Float
  get() = this[2]
  set(value) {
    this[2] = value
  }

var Float32Array.a: Float
  get() = this[3]
  set(value) {
    this[3] = value
  }

operator fun <T : Float32Array> T.times(scalar: Float): T {
  @Suppress("UNCHECKED_CAST")
  val out = Float32Array(this) as T
  for (i in 0..this.length) {
    out[i] = out[i] * scalar
  }
  return out
}

fun <T : Float32Array> Array<T>.toFloatArray(): Array<Float> {
  return this.asIterable().toFloatArray()
}

fun <T : Float32Array> Iterable<T>.toFloatArray(): Array<Float> {
  return this.flatMap { it.toFloatArray().asIterable() }.toTypedArray()
}

//Generic math

fun Float.clamp(min: Float, max: Float): Float {
  return min(max(min, this), max)
}

fun Float.toDeg(): Float {
  return 180f * this / PI
}

fun Float.toRad(): Float {
  return PI * this / 180f
}
