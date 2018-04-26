package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.EventListener
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.PI

const val PI = PI.toFloat()
const val TAU = 2 * PI.toFloat()
const val PI_HALF = PI.toFloat() / 2f

lateinit var gl: WebGLRenderingContext
lateinit var program: Program
lateinit var canvas: HTMLCanvasElement
lateinit var scene: Scene

fun main(args: Array<String>) {
  initGL()
  loadTexturesAndRender()
  console.log("Scene ready! $program")
}

private fun initGL() {
  canvas = document.getElementById("canvas") as HTMLCanvasElement
  window.addEventListener("resize", EventListener {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  })
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  gl = canvas.getContext("webgl") as WebGLRenderingContext
  program = Program(createProgram(VS, FS)!!)
}

private fun loadTexturesAndRender() {
  Materials.load()
  scene = Scene()
  render()
}

private fun render() {
  gl.viewport(0, 0, canvas.width, canvas.height)
  program.useProgram()
  scene.clear()
  scene.render()
  window.requestAnimationFrame { render() }
}
