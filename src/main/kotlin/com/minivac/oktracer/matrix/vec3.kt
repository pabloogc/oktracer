package com.minivac.oktracer.matrix

import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set

external class vec3 : Float32Array {

  companion object {
    fun create(): vec3
    fun normalize(out: vec3, a: vec3): vec3
    fun fromValues(x: Float, y: Float, z: Float): vec3
    fun cross(out: vec3, a: vec3, b: vec3)
    fun dot(a: vec3, b: vec3): Float
    fun angle(a: vec3, b: vec3): Float
    fun sub(out: vec3, a: vec3, b: vec3)
    fun add(out: vec3, a: vec3, b: vec3)
  }
}

fun vec3(x: Float, y: Float, z: Float): vec3 {
  return vec3.fromValues(x, y, z)
}

fun vec3.set(x: Float, y: Float, z: Float): vec3 {
  this[0] = x; this[1] = y; this[2] = z; return this
}

fun vec3.setXYZ(v: Float): vec3 {
  this[0] = v; this[1] = v; this[2] = v; return this
}

fun vec3.normalize(): vec3 {
  val out = vec3.create()
  vec3.normalize(out, this); return out
}

operator fun vec3.plus(other: vec3): vec3 {
  val out = vec3.create()
  vec3.add(out, this, other); return out
}

operator fun vec3.minus(other: vec3): vec3 {
  val out = vec3.create()
  vec3.sub(out, this, other); return out
}

infix fun vec3.dot(other: vec3): Float {
  return vec3.dot(this, other)
}

infix fun vec3.midPoint(v: vec3): vec3 {
  return vec3((this[0] + v[0]) / 2f, (this[1] + v[1]) / 2f, (this[2] + v[2]) / 2f)
}

infix fun vec3.cross(other: vec3): vec3 {
  val out = vec3.create()
  vec3.cross(out, this, other); return out
}