package com.minivac.oktracer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set

external class mat4 : Float32Array {
    companion object {
        fun add(out: mat4, a: mat4, b: mat4): mat4
        fun adjoint(out: mat4, a: mat4): mat4
        fun clone(a: mat4): mat4
        fun copy(out: mat4, a: mat4): mat4
        fun create(): mat4
        fun identity(out: mat4): mat4
        fun lookAt(out: mat4, eye: vec3, center: vec3, up: vec3): mat4
        fun mul()
        fun multiply(out: mat4, a: mat4, b: mat4): mat4
        fun multiplyScalar(out: mat4, a: mat4, b: Float): mat4
        fun ortho(out: mat4, left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): mat4
        fun perspective(out: mat4, fovy: Float, aspect: Float, near: Float, far: Float): mat4
        fun rotate(out: mat4, a: mat4, rad: Float, axis: vec3): mat4
        fun rotateX(out: mat4, a: mat4, rad: Float)
        fun rotateY(out: mat4, a: mat4, rad: Float)
        fun rotateZ(out: mat4, a: mat4, rad: Float)
        fun scale(out: mat4, a: mat4, v: vec3): mat4
        fun str(a: mat4): String
        fun subtract(out: mat4, a: mat4, b: mat4): mat4
        fun targetTo(out: mat4, eye: vec3, center: vec3, up: vec3): mat4
        fun translate(out: mat4, a: mat4, v: vec3): mat4
        fun transpose(out: mat4, a: mat4): mat4
    }
}

external class mat3 : Float32Array {
    companion object {
        fun create(): mat3
        fun normalFromMat4(out: mat3, a: mat4)
    }
}

//Vec3 Functions

external class vec3 : Float32Array {
    companion object {
        fun create(): vec3
        fun normalize(out: vec3, a: vec3): vec3
        fun fromValues(x: Float, y: Float, z: Float): vec3
    }
}

fun vec3.set(x: Float, y: Float, z: Float): vec3 {
    this[0] = x; this[1] = y; this[2] = z; return this
}

fun vec3.setXYZ(v: Float): vec3 {
    this[0] = v; this[1] = v; this[2] = v; return this
}

fun vec3.setX(x: Float): vec3 {
    this[0] = x; return this
}

fun vec3.setY(y: Float): vec3 {
    this[1] = y; return this
}

fun vec3.setZ(z: Float): vec3 {
    this[2] = z; return this
}

fun vec3.normalized(): vec3 {
    val out = vec3.create()
    vec3.normalize(out, this); return out
}

fun vec3.normalize(): vec3 {
    vec3.normalize(this, this); return this
}

infix fun vec3.midPoint(v: vec3): vec3 {
    return vec3.fromValues((this[0] + v[0]) / 2f, (this[1] + v[1]) / 2f, (this[2] + v[2]) / 2f)
}

fun Float32Array.toFloatArray(): Array<Float> {
    return Array(this.length, { i -> this[i] })
}
