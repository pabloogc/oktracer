package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import kotlin.browser.document

lateinit var gl: WebGLRenderingContext

fun main(args: Array<String>) {
    gl = initGL()
    createScene()
    clear()
    render()
}

private fun createScene() {

}

private fun render() {

}

private fun initGL(): WebGLRenderingContext {
    val canvas: dynamic = document.getElementById("canvas")
    val gl = canvas.getContext("webgl") as WebGLRenderingContext
    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)
    return gl
}

private fun clear() {
    gl.clearColor(0.0f, 0.0f, 0.0f, 1.0f)
    gl.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)
}