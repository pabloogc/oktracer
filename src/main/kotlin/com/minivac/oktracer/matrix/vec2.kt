package com.minivac.oktracer.matrix

import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import kotlin.math.max
import kotlin.math.min

external class vec2 : Float32Array {
  companion object {
    fun create(): vec2
    fun fromValues(x: Float, y: Float): vec2
    fun sub(out: vec2, a: vec2, b: vec2)
  }
}

fun vec2(x: Float, y: Float): vec2 {
  return vec2.fromValues(x, y)
}

infix fun vec2.midPoint(v: vec2): vec2 {
  return vec2((this[0] + v[0]) / 2f, (this[1] + v[1]) / 2f)
}

operator fun vec2.minus(other: vec2): vec2 {
  val out = vec2.create()
  vec2.sub(out, this, other); return out
}

fun Float32Array.toFloatArray(): Array<Float> {
  return Array(this.length, { i -> this[i] })
}

fun clamp(value: Float, low: Float, high: Float): Float {
  return max(low, min(value, high))
}