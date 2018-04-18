package com.minivac.oktracer

import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLShader
import org.khronos.webgl.WebGLRenderingContext.Companion.COMPILE_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAGMENT_SHADER
import org.khronos.webgl.WebGLRenderingContext.Companion.LINK_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.VERTEX_SHADER


fun createProgram(vertexSource: String, fragmentSource: String): WebGLProgram? {
    val program = gl.createProgram()
    val vertexShader = createShader(VERTEX_SHADER, vertexSource)
    val fragmentShader = createShader(FRAGMENT_SHADER, fragmentSource)
    gl.attachShader(program, vertexShader)
    gl.attachShader(program, fragmentShader)
    gl.linkProgram(program)

    if (gl.getProgramParameter(program, LINK_STATUS) != true) {
        console.error("Could not initialise program")
    }

    return program
}

fun createShader(type: Int, source: String): WebGLShader? {
    val shader = gl.createShader(type)
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    val status = gl.getShaderParameter(shader, COMPILE_STATUS)

    if (status != true) {
        console.log(status)
        console.error(gl.getShaderInfoLog(shader))
        return null
    }
    return shader
}