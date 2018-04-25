package com.minivac.oktracer

import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLUniformLocation


//language=GLSL
const val VS = """

    attribute vec3 aVertexPosition;
    attribute vec2 aTexCoord;

    //Normal tangent and bitangent, all in tangent space
    attribute vec3 aNormal;
    attribute vec3 aTangent;
    attribute vec3 aBitangent;

    uniform mat3 uNMatrix; //normal to model space
    uniform mat4 uCMatrix; //Camera
    uniform mat4 uPMatrix; //Projection
    uniform mat4 uMVMatrix; //Model-View

    //Material
    uniform sampler2D uDisplacementSampler;
    uniform float uDisplacementCoefficient;

    //Tangent to model space
    varying mat3 vTBN;
    varying vec3 vPosition;
    varying vec3 vNormal;
    varying vec3 vTangent;
    varying vec3 vBitangent;
    varying vec2 vTexCoord;

    void main(void) {

        vec3 T = normalize(vec3(uMVMatrix * vec4(aTangent,   0.0)));
        vec3 B = normalize(vec3(uMVMatrix * vec4(aBitangent, 0.0)));
        vec3 N = normalize(vec3(uMVMatrix * vec4(aNormal,    0.0)));
        vTBN = mat3(T, B, N);

        vec3 displacement = aNormal
            * texture2D(uDisplacementSampler, aTexCoord).r
            * uDisplacementCoefficient;
        vec4 displacedPosition = vec4(aVertexPosition + displacement, 1.0);
        vPosition = (uMVMatrix * displacedPosition).xyz; //The position in model space

        vNormal = aNormal;
        vTangent = aTangent;
        vBitangent = aBitangent;

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

    //Textures
    uniform sampler2D uColorSampler;
    uniform sampler2D uNormalSampler;
    uniform sampler2D uOcclusionSampler;
    uniform sampler2D uRoughnessSampler;
    uniform float uMaterialShininess;

    //Material
    varying mat3 vTBN;
    varying vec3 vPosition;
    varying vec3 vNormal;
    varying vec3 vTangent;
    varying vec3 vBitangent;
    varying vec2 vTexCoord;

    mat2 transpose(mat2 m) {
      return mat2(m[0][0], m[1][0],
                  m[0][1], m[1][1]);
    }

    mat3 transpose(mat3 m) {
      return mat3(m[0][0], m[1][0], m[2][0],
                  m[0][1], m[1][1], m[2][1],
                  m[0][2], m[1][2], m[2][2]);
    }

    mat4 transpose(mat4 m) {
      return mat4(m[0][0], m[1][0], m[2][0], m[3][0],
                  m[0][1], m[1][1], m[2][1], m[3][1],
                  m[0][2], m[1][2], m[2][2], m[3][2],
                  m[0][3], m[1][3], m[2][3], m[3][3]);
    }

    void main(void) {
        vec3 color = vec3(0.0, 0.0, 0.0);

        vec3 materialColor = texture2D(uColorSampler, vTexCoord).rgb;
        vec3 materialNormal = 2.0 * texture2D(uNormalSampler, vTexCoord).xyz - 1.0;
        materialNormal = normalize(vTBN * materialNormal);
        vec3 materialSpecularColor = materialColor;
        float shininess = (1.0 - texture2D(uRoughnessSampler, vTexCoord).r) * uMaterialShininess;

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
            float occlusion = texture2D(uOcclusionSampler, vTexCoord).r;
            vec3 ambient = occlusion * ambientCoefficient * materialColor * lightColor;

            //Diffuse
            float diffuseCoefficient = clamp(dot(normalize(surfaceToLight), materialNormal), 0.0, 1.0);
            vec3 diffuse = diffuseCoefficient * materialColor * lightColor;

            //Specular
            float specularCoefficient = 0.0;
            if(diffuseCoefficient > 0.0 && shininess > 0.0) { //It's lit
                specularCoefficient = pow(max(0.0, dot(reflect(-surfaceToLight, materialNormal), surfaceToEye)), shininess);
            }
            vec3 specular = specularCoefficient * materialSpecularColor * lightColor;

            color += ambient + attenuation * (diffuse + specular);
        }

        gl_FragColor = vec4(color, 1.0);
    }


"""

class Program(val glProgram: WebGLProgram) {
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
  val materialShininess: WebGLUniformLocation?

  //Attributes
  val vertexPositionLocation: Int
  val normalLocation: Int
  val tangentLocation: Int
  val bitangentLocation: Int
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
    materialShininess = gl.getUniformLocation(glProgram, "uMaterialShininess")

    vertexPositionLocation = gl.getAttribLocation(glProgram, "aVertexPosition")
    texCoordLocation = gl.getAttribLocation(glProgram, "aTexCoord")
    normalLocation = gl.getAttribLocation(glProgram, "aNormal")
    tangentLocation = gl.getAttribLocation(glProgram, "aTangent")
    bitangentLocation = gl.getAttribLocation(glProgram, "aBitangent")

    gl.enableVertexAttribArray(vertexPositionLocation)
    gl.enableVertexAttribArray(normalLocation)
    gl.enableVertexAttribArray(tangentLocation)
    gl.enableVertexAttribArray(bitangentLocation)
    gl.enableVertexAttribArray(texCoordLocation)
  }

  fun useProgram() {
    gl.useProgram(glProgram)
  }
}

