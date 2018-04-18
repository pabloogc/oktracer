package com.minivac.oktracer

import org.khronos.webgl.Float32Array

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

external class vec3 : Float32Array {
    companion object {
        fun create(): vec3
        fun fromValues(x: Float, y: Float, z: Float): vec3
    }
}