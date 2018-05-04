package com.minivac.oktracer

import com.minivac.oktracer.render.DefaultProgram
import com.minivac.oktracer.render.ShadowProgram
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
lateinit var defaultProgram: DefaultProgram
lateinit var shadowProgram: ShadowProgram
lateinit var canvas: HTMLCanvasElement
lateinit var scene: Scene

fun main(args: Array<String>) {
  initGL()
  loadTexturesAndRender()
  console.log("Scene ready! $defaultProgram")
}

private fun initGL() {

  canvas = document.getElementById("canvas") as HTMLCanvasElement
  window.addEventListener("resize", EventListener {
    resizeCanvas()
  })
  resizeCanvas()
  gl = canvas.getContext("webgl") as WebGLRenderingContext
  defaultProgram = DefaultProgram()
  shadowProgram = ShadowProgram()
}

private fun resizeCanvas() {
  val bufferWidth = 1920
  val bufferHeight = 1440
  val canvasWidth = 640
  val canvasHeight = 480

  canvas.width = bufferWidth
  canvas.height = bufferHeight
  canvas.style.width = "${canvasWidth}px"
  canvas.style.height = "${canvasHeight}px"
}

private fun loadTexturesAndRender() {
  Materials.load()
  scene = Scene()
  render()
}

private fun render() {
  scene.clear()
  scene.render()
  window.requestAnimationFrame { render() }
}
