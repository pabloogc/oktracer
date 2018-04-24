package com.minivac.oktracer

import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLUniformLocation


//language=GLSL
const val VS = """

    attribute vec3 aVertexPosition;
    attribute vec3 aVertexNormal;
    attribute vec2 aTexCoord;

    uniform mat4 uCMatrix; //Camera
    uniform mat4 uPMatrix; //Projection
    uniform mat4 uMVMatrix; //Model-View

    //Material
    uniform sampler2D uDisplacementSampler;
    uniform float uDisplacementCoefficient;

    varying vec3 vPosition;
    varying vec3 vNormal;
    varying vec2 vTexCoord;

    void main(void) {
        vec3 displacement = aVertexNormal * texture2D(uDisplacementSampler, aTexCoord).r * uDisplacementCoefficient;
        vec4 displacedPosition = vec4(aVertexPosition + displacement, 1.0);
        vPosition = (uMVMatrix * displacedPosition).xyz; //The position in model space
        vTexCoord = aTexCoord; //Interpolated texture values
        gl_Position = uPMatrix * uCMatrix * uMVMatrix * displacedPosition; //Position in camera space
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

    uniform mat3 uNMatrix; //Normal matrix transform

    //Textures
    uniform sampler2D uColorSampler;
    uniform sampler2D uNormalSampler;
    uniform sampler2D uOcclusionSampler;
    uniform sampler2D uRoughnessSampler;

    //Material
    varying vec3 vPosition;
    varying vec3 vNormal;
    varying vec2 vTexCoord;

    void main(void) {
        vec3 color = vec3(0.0, 0.0, 0.0);

        vec3 materialColor = texture2D(uColorSampler, vTexCoord).rgb;
        vec3 materialNormal = normalize(uNMatrix * texture2D(uNormalSampler, vTexCoord).rgb);
        vec3 materialSpecularColor = materialColor * texture2D(uRoughnessSampler, vTexCoord).r / 255.0;
//        float materialShininess = texture2D(uRoughnessSampler, vTexCoord).r;
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
            //vec3 ambient = ambientCoefficient * materialColor * lightColor;
            vec3 ambient = texture2D(uOcclusionSampler, vTexCoord).r * ambientCoefficient * materialColor * lightColor;

            //Diffuse
            float displacementCoefficient = clamp(dot(normalize(surfaceToLight), materialNormal), 0.0, 1.0);
            vec3 diffuse = displacementCoefficient * materialColor * lightColor;

            //Specular
            float specularCoefficient = 0.0;
            if(displacementCoefficient > 0.0 && materialShininess > 0.0) { //It's lit
                specularCoefficient = pow(max(0.0, dot(reflect(-surfaceToLight, materialNormal), surfaceToEye)), materialShininess);
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

  //Material
  val colorSampler: WebGLUniformLocation?
  val normalSampler: WebGLUniformLocation?
  val occlusionSampler: WebGLUniformLocation?
  val roughnessSampler: WebGLUniformLocation?
  val displacementSampler: WebGLUniformLocation?
  val displacementCoefficient: WebGLUniformLocation?

  //Attributes
  val vertexPositionLocation: Int
  val normalLocation: Int
  val texCoordLocation: Int

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

    //Material
    colorSampler = gl.getUniformLocation(glProgram, "uColorSampler")
    normalSampler = gl.getUniformLocation(glProgram, "uNormalSampler")
    occlusionSampler = gl.getUniformLocation(glProgram, "uOcclusionSampler")
    roughnessSampler = gl.getUniformLocation(glProgram, "uRoughnessSampler")
    displacementSampler = gl.getUniformLocation(glProgram, "uDisplacementSampler")
    displacementCoefficient = gl.getUniformLocation(glProgram, "uDisplacementCoefficient")

    vertexPositionLocation = gl.getAttribLocation(glProgram, "aVertexPosition")
    normalLocation = gl.getAttribLocation(glProgram, "aVertexNormal")
    texCoordLocation = gl.getAttribLocation(glProgram, "aTexCoord")

    gl.enableVertexAttribArray(vertexPositionLocation)
    gl.enableVertexAttribArray(normalLocation)
    gl.enableVertexAttribArray(texCoordLocation)
  }

  fun useProgram() {
    gl.useProgram(glProgram)
  }
}

