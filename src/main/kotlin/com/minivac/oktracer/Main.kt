package com.minivac.oktracer

import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import org.khronos.webgl.WebGLUniformLocation
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.math.PI

const val FPI = PI.toFloat()

//language=GLSL
private const val VS = """
    attribute vec3 aVertexPosition;
    //attribute vec3 aVertexColor;

    uniform mat4 uCMatrix;
    uniform mat4 uPMatrix;
    uniform mat4 uMVMatrix;

    varying vec3 vColor;

    void main(void) {
        vec4 p4 = vec4(aVertexPosition, 1.0);
        vColor = (uMVMatrix * p4).xyz;
        gl_Position = uPMatrix * uCMatrix * uMVMatrix * p4;
    }
"""

//language=GLSL
private const val FS = """
    precision mediump float;

    varying vec3 vColor;

    void main(void) {
        gl_FragColor = vec4(vColor.xyz + 0.3, 1.0);
    }
"""

lateinit var gl: WebGLRenderingContext
lateinit var program: Program
lateinit var canvas: HTMLCanvasElement
lateinit var scene: Scene

class Program(val glProgram: WebGLProgram) {
    val cameraMatrix = mat4.create()
    val projectionMatrix = mat4.create()

    val cameraMatrixLocation: WebGLUniformLocation
    val projectionMatrixLocation: WebGLUniformLocation
    val modelViewMatrixLocation: WebGLUniformLocation
    val vertexPositionLocation: Int
    //val colorPositionLocation: Int

    init {
        gl.useProgram(glProgram)

        cameraMatrixLocation = gl.getUniformLocation(glProgram, "uCMatrix")!!
        projectionMatrixLocation = gl.getUniformLocation(glProgram, "uPMatrix")!!
        modelViewMatrixLocation = gl.getUniformLocation(glProgram, "uMVMatrix")!!

        vertexPositionLocation = gl.getAttribLocation(glProgram, "aVertexPosition")
        //colorPositionLocation = gl.getAttribLocation(glProgram, "aVertexColor")

        gl.enableVertexAttribArray(vertexPositionLocation)
        //gl.enableVertexAttribArray(colorPositionLocation)
    }

    fun useProgram() {
        gl.useProgram(glProgram)
    }
}

class Scene {
    val triangle1 = Triangle(Triangle.ISOSCELES_VERTICES)
    val triangle2 = Triangle(Triangle.ISOSCELES_VERTICES).apply {
        translation = vec3.fromValues(0f, -1f, 1f)
        rotation = vec3.fromValues(FPI / 2, 0f, 0f)
    }

    fun render() {
        program.useProgram()
        mat4.perspective(program.projectionMatrix, 0.5f, canvas.width.toFloat() / canvas.height, 1f, 100f)
        mat4.lookAt(
                out = program.cameraMatrix,
                eye = vec3.fromValues(0f, 0f, 10f),
                center = vec3.fromValues(0f, 0f, 0f),
                up = vec3.fromValues(0f, 1f, 0f))
        gl.uniformMatrix4fv(program.projectionMatrixLocation, false, program.projectionMatrix)
        gl.uniformMatrix4fv(program.cameraMatrixLocation, false, program.cameraMatrix)

        triangle1.render(program)
        triangle2.render(program)
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
    program.useProgram()
    gl.viewport(0, 0, canvas.width, canvas.height)
    scene.render()
}

private fun clear() {
    gl.clearColor(0.0f, 0.0f, 0.0f, 1.0f)
    gl.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)
}