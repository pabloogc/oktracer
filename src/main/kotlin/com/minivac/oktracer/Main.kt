package com.minivac.oktracer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import org.khronos.webgl.WebGLUniformLocation
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document

//language=GLSL
private const val VS = """
    attribute vec3 aVertexPosition;

    uniform mat4 uPMatrix;
    uniform mat4 uMVMatrix;

    void main(void) {
        gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);
    }
"""

//language=GLSL
private const val FS = """
    precision mediump float;

    void main(void) {
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
"""

lateinit var gl: WebGLRenderingContext
lateinit var program: Program
lateinit var canvas: HTMLCanvasElement
lateinit var scene: Scene

class Program(val glProgram: WebGLProgram) {
    val proyectionMatrix = mat4.create()
    val modelViewMatrix = mat4.create()

    val proyectionMatrixLocation: WebGLUniformLocation
    val modelViewMatrixLocation: WebGLUniformLocation
    val vertexPositionLocation: Int

    init {
        useProgram()
        proyectionMatrixLocation = gl.getUniformLocation(glProgram, "uPMatrix")!!
        modelViewMatrixLocation = gl.getUniformLocation(glProgram, "uMVMatrix")!!
        vertexPositionLocation = gl.getAttribLocation(glProgram, "aVertexPosition")
        gl.useProgram(null)
    }

    fun useProgram() {
        gl.useProgram(glProgram)
    }
}

class Scene {
    val triangle = Triangle(Triangle.ISOSCELES_VERTICES)
    fun render() {
        program.useProgram()
        triangle.render(program)
    }
}


fun main(args: Array<String>) {
    initGL()
    program = Program(createProgram(VS, FS)!!)
    scene = Scene()
    console.log("Scene ready! $program")
    createScene()
    clear()
    render()
}

private fun initGL() {
    canvas = document.getElementById("canvas") as HTMLCanvasElement
    gl = canvas.getContext("webgl") as WebGLRenderingContext
    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)
}

private fun createScene() {

}

private fun render() {
    gl.viewport(0, 0, canvas.width, canvas.height)
    mat4.ortho(program.proyectionMatrix, -1f, 1f, 1f, 1f, -1f, 10f)
    gl.uniformMatrix4fv(program.proyectionMatrixLocation, false, mat4.unsafeCast<Float32Array>())
    console.log("did it work?")
    scene.render()
}

private fun clear() {
    gl.clearColor(0.0f, 0.0f, 0.0f, 1.0f)
    gl.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)
}