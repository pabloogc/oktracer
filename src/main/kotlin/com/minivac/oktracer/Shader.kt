package com.minivac.oktracer

import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLUniformLocation


data class Light(val position: vec3 = vec3.create(),
                 val color: vec3 = vec3.create(),
                 var ambientCoefficient: Float = 0.20f)

//language=GLSL
const val VS = """

    attribute vec3 aVertexPosition;
    attribute vec3 aVertexNormal;

    uniform mat4 uCMatrix; //Camera
    uniform mat4 uPMatrix; //Projection
    uniform mat4 uMVMatrix; //Model-View
    uniform mat3 uNMatrix; //Normal

    varying vec3 vPosition;
    varying vec3 vNormal;

    void main(void) {
        vec4 p4 = vec4(aVertexPosition, 1.0);
        vNormal = normalize(uNMatrix * aVertexNormal);
        vPosition = (uMVMatrix * p4).xyz;
        gl_Position = uPMatrix * uCMatrix * uMVMatrix * p4;
    }
"""

//language=GLSL
const val FS = """

    #define MAX_LIGHTS $MAX_LIGHTS

    precision mediump float;

    uniform vec3 uEyePosition;

    //Light uniforms
    uniform vec3 uLightPosition[MAX_LIGHTS];
    uniform vec3 uLightColor[MAX_LIGHTS];
    uniform float uLightAmbientCoefficient[MAX_LIGHTS];

    //Material
    varying vec3 vPosition;
    varying vec3 vNormal;

    void main(void) {
        vec3 color = vec3(0.0, 0.0, 0.0);

        //TODO: Texture mapping
        vec3 materialColor = vec3(0.55, 0.12, 0.93);
        vec3 materialSpecularColor = materialColor;
        float materialShininess = 20.0;

        for(int i = 0; i < MAX_LIGHTS; i++) {
            //Light properties
            vec3 lightPosition = uLightPosition[i];
            vec3 lightColor = uLightColor[i];
            float ambientCoefficient = uLightAmbientCoefficient[i];
            float attenuationCoefficient = 1.0; //TODO, argument

            //Generic light
            vec3 surfaceToLight = normalize(lightPosition - vPosition);
            vec3 surfaceToEye = normalize(uEyePosition - vPosition);
            float surfaceToLightDistance = length(lightPosition - vPosition);
            float attenuation = 1.0 / (1.0 + attenuationCoefficient * pow(surfaceToLightDistance, 2.0));

            //Ambient
            vec3 ambient = ambientCoefficient * materialColor * lightColor;

            //Diffuse
            float diffuseCoefficient = clamp(dot(normalize(surfaceToLight), vNormal), 0.0, 1.0);
            vec3 diffuse = diffuseCoefficient * materialColor * lightColor;

            //Specular
            float specularCoefficient = 0.0;
            if(diffuseCoefficient > 0.0 && materialShininess > 0.0) { //It's lit
                specularCoefficient = pow(max(0.0, dot(reflect(-surfaceToLight, vNormal), surfaceToEye)), materialShininess);
            }
            vec3 specular = specularCoefficient * materialSpecularColor * lightColor;

            color += ambient + attenuation * (diffuse + specular);
        }

        gl_FragColor = vec4(color, 1.0);
    }
"""

class Program(val glProgram: WebGLProgram) {
    val cameraMatrix = mat4.create()
    val projectionMatrix = mat4.create()

    //Camera
    val eyePosition: WebGLUniformLocation?
    val cameraMatrixLocation: WebGLUniformLocation?
    val projectionMatrixLocation: WebGLUniformLocation?
    val modelViewMatrixLocation: WebGLUniformLocation?
    val normalMatrixLocation: WebGLUniformLocation?

    //Lights
    val lightPositionLocation: WebGLUniformLocation?
    val lightColorLocation: WebGLUniformLocation?
    val lightAmbientCoefficient: WebGLUniformLocation?

    val vertexPositionLocation: Int
    val normalLocation: Int

    init {
        gl.useProgram(glProgram)
        //Camera
        eyePosition = gl.getUniformLocation(glProgram, "uEyePosition")
        cameraMatrixLocation = gl.getUniformLocation(glProgram, "uCMatrix")
        projectionMatrixLocation = gl.getUniformLocation(glProgram, "uPMatrix")
        modelViewMatrixLocation = gl.getUniformLocation(glProgram, "uMVMatrix")
        normalMatrixLocation = gl.getUniformLocation(glProgram, "uNMatrix")
        //Light
        lightPositionLocation = gl.getUniformLocation(glProgram, "uLightPosition")
        lightColorLocation = gl.getUniformLocation(glProgram, "uLightColor")
        lightAmbientCoefficient = gl.getUniformLocation(glProgram, "uLightAmbientCoefficient")

        vertexPositionLocation = gl.getAttribLocation(glProgram, "aVertexPosition")
        normalLocation = gl.getAttribLocation(glProgram, "aVertexNormal")

        gl.enableVertexAttribArray(vertexPositionLocation)
        gl.enableVertexAttribArray(normalLocation)
    }

    fun useProgram() {
        gl.useProgram(glProgram)
    }
}

