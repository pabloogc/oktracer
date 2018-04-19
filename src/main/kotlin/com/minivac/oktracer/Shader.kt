package com.minivac.oktracer

import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLUniformLocation

const val MAX_LIGHTS = 2

data class Light(val position: vec3 = vec3.create(),
                 val color: vec3 = vec3.create(),
                 var ambient: Float = 0.20f)

//language=GLSL
const val VS = """
    attribute vec3 aVertexPosition;
    attribute vec3 aVertexNormal;

    uniform mat4 uCMatrix;
    uniform mat4 uPMatrix;
    uniform mat4 uMVMatrix;

    varying vec3 vPosition;
    varying vec3 vNormal;

    void main(void) {
        vec4 p4 = vec4(aVertexPosition, 1.0);
        vec4 n4 = vec4(aVertexNormal, 1.0);
        vNormal = (uMVMatrix * n4).xyz;
        vPosition = (uMVMatrix * p4).xyz;
        gl_Position = uPMatrix * uCMatrix * uMVMatrix * p4;
    }
"""

//language=GLSL
const val FS = """
    #define MAX_LIGHTS $MAX_LIGHTS

    precision mediump float;

    //Light uniforms
    uniform vec3 uLightPosition[MAX_LIGHTS];
    uniform vec3 uLightColor[MAX_LIGHTS];
    uniform vec3 uLightAmbient[MAX_LIGHTS];

    //Material
    varying vec3 vPosition;
    varying vec3 vNormal;

    void main(void) {
        vec3 color = vec3(0.0, 0.0, 0.0);

        vec3 materialColor = vec3(1.0, 1.0, 1.0);

        for(int i = 0; i < MAX_LIGHTS; i++) {
            vec3 lightPosition = uLightPosition[i];
            vec3 lightColor = uLightColor[i];
            vec3 ambientColor = uLightAmbient[i];
            float distanceToLight = length(vPosition - lightPosition);
            float attenuation = 1.0 / (1.0 + pow(distanceToLight, 2.0));

            float diffuseCoefficient = clamp(dot(vNormal, lightPosition), 0.0, 1.0);
            vec3 ambient = materialColor * ambientColor;
            vec3 specular = vec3(0.0, 0.0, 0.0); //Not done
            vec3 diffuse = materialColor * lightColor * diffuseCoefficient;
            color += ambient + attenuation * (diffuse + specular);
        }

        gl_FragColor = vec4(color, 1.0);
    }
"""

class Program(val glProgram: WebGLProgram) {
    val cameraMatrix = mat4.create()
    val projectionMatrix = mat4.create()

    //Camera
    val cameraMatrixLocation: WebGLUniformLocation
    val projectionMatrixLocation: WebGLUniformLocation
    val modelViewMatrixLocation: WebGLUniformLocation
    //Lights
    val lightPositionLocation: WebGLUniformLocation
    val lightColorLocation: WebGLUniformLocation
    val lightAmbientLocation: WebGLUniformLocation

    val vertexPositionLocation: Int
    val normalLocation: Int

    init {
        gl.useProgram(glProgram)
        //Camera
        cameraMatrixLocation = gl.getUniformLocation(glProgram, "uCMatrix")!!
        projectionMatrixLocation = gl.getUniformLocation(glProgram, "uPMatrix")!!
        modelViewMatrixLocation = gl.getUniformLocation(glProgram, "uMVMatrix")!!
        //Light
        lightPositionLocation = gl.getUniformLocation(glProgram, "uLightPosition")!!
        lightColorLocation = gl.getUniformLocation(glProgram, "uLightColor")!!
        lightAmbientLocation = gl.getUniformLocation(glProgram, "uLightAmbient")!!

        vertexPositionLocation = gl.getAttribLocation(glProgram, "aVertexPosition")
        normalLocation = gl.getAttribLocation(glProgram, "aVertexNormal")

        gl.enableVertexAttribArray(vertexPositionLocation)
        gl.enableVertexAttribArray(normalLocation)
    }

    fun useProgram() {
        gl.useProgram(glProgram)
    }
}

