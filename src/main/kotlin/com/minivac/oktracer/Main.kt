package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.PI

const val FPI = PI.toFloat()

lateinit var gl: WebGLRenderingContext
lateinit var program: Program
lateinit var canvas: HTMLCanvasElement
lateinit var scene: Scene

fun main(args: Array<String>) {
    initGL()
    program = Program(createProgram(VS, FS)!!)
    scene = Scene()
    console.log("Scene ready! $program")
    render()
}

private fun initGL() {
    canvas = document.getElementById("canvas") as HTMLCanvasElement
    gl = canvas.getContext("webgl") as WebGLRenderingContext
    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)
}

private fun render() {
    gl.viewport(0, 0, canvas.width, canvas.height)
    program.useProgram()
    scene.clear()
    scene.render()
    window.requestAnimationFrame { render() }
}

