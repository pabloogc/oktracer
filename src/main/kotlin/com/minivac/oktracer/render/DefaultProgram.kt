package com.minivac.oktracer.render

import com.minivac.oktracer.*
import com.minivac.oktracer.matrix.toFloatArray
import com.minivac.oktracer.mesh.Mesh
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.LEQUAL
import org.khronos.webgl.WebGLRenderingContext.Companion.LINE_LOOP
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE0
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE1
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE2
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE3
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE4
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.WebGLUniformLocation
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document


//language=GLSL
private const val VS = """
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

        vPosition = (uMVMatrix * displacedPosition).xyz;
        vTexCoord = aTexCoord;
        gl_Position = uPMatrix * uCMatrix * uMVMatrix * displacedPosition;
    }
"""

//language=GLSL
private const val FS = """

    #define MAX_LIGHTS $MAX_LIGHTS

    precision mediump float;

    //Eye
    uniform vec3 uEyePosition;
    uniform vec3 uEyeDirection;

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
        vec3 materialNormal = normalize(vTBN * (2.0 * texture2D(uNormalSampler, vTexCoord).xyz - 1.0));
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
            vec3 eyeToLight = normalize(uEyePosition - lightPosition);
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
//            color += diffuseCoefficient;
        }
        gl_FragColor = vec4(color, 1.0);
    }
"""

class DefaultProgram {
  val glProgram by lazy {
    createProgram(VS, FS)!!
  }

  init {
    document.addEventListener("keydown", EventListener {
      it as KeyboardEvent
      when (it.key) {
        "b" -> drawMode = TRIANGLES
        "n" -> drawMode = LINE_LOOP
      }
    })
  }

  private var drawMode = TRIANGLES

  //Camera
  private val eyePosition: WebGLUniformLocation?
  private val eyeDirection: WebGLUniformLocation?
  //Camera
  private val cameraMatrixLocation: WebGLUniformLocation?
  private val projectionMatrixLocation: WebGLUniformLocation?
  private val modelViewMatrixLocation: WebGLUniformLocation?
  private val normalMatrixLocation: WebGLUniformLocation?

  //Lights
  private val lightPositionLocation: WebGLUniformLocation?
  private val lightColorLocation: WebGLUniformLocation?
  private val lightAmbientCoefficient: WebGLUniformLocation?

  //Material
  private val colorSampler: WebGLUniformLocation?
  private val normalSampler: WebGLUniformLocation?
  private val occlusionSampler: WebGLUniformLocation?
  private val roughnessSampler: WebGLUniformLocation?
  private val displacementSampler: WebGLUniformLocation?
  private val displacementCoefficient: WebGLUniformLocation?
  private val materialShininess: WebGLUniformLocation?

  //Attributes
  private val vertexPositionLocation: Int
  private val normalLocation: Int
  private val tangentLocation: Int
  private val bitangentLocation: Int
  private val texCoordLocation: Int

  init {
    gl.useProgram(glProgram)
    //Camera
    eyePosition = gl.getUniformLocation(glProgram, "uEyePosition")
    eyeDirection = gl.getUniformLocation(glProgram, "uEyeDirection")

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
  }

  fun render(camera: Camera, meshList: List<Mesh>, lights: Array<Light>) {
    gl.viewport(0, 0, canvas.width, canvas.height)
    gl.useProgram(glProgram)


    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)
    gl.enableVertexAttribArray(vertexPositionLocation)
    gl.enableVertexAttribArray(normalLocation)
    gl.enableVertexAttribArray(tangentLocation)
    gl.enableVertexAttribArray(bitangentLocation)
    gl.enableVertexAttribArray(texCoordLocation)

    gl.uniformMatrix4fv(projectionMatrixLocation, false, camera.projectionMatrix)
    gl.uniformMatrix4fv(cameraMatrixLocation, false, camera.lookMatrix)
    gl.uniform3fv(eyePosition, camera.position)
    gl.uniform3fv(eyeDirection, camera.direction)

    val position = lights.flatMap { it.position.toFloatArray().toList() }.toTypedArray()
    val color = lights.flatMap { it.color.toFloatArray().toList() }.toTypedArray()
    val ambient = lights.map { it.ambientCoefficient }.toTypedArray()

    gl.uniform3fv(lightPositionLocation, position)
    gl.uniform3fv(lightColorLocation, color)
    gl.uniform1fv(lightAmbientCoefficient, ambient)

    meshList
        .filter { it.material.loaded }
        .forEach { mesh ->
          gl.uniformMatrix4fv(modelViewMatrixLocation, false, mesh.modelMatrix)
          gl.uniformMatrix3fv(normalMatrixLocation, false, mesh.normalMatrix)

          gl.bindBuffer(ARRAY_BUFFER, mesh.verticesBuffer)
          gl.vertexAttribPointer(vertexPositionLocation, 3, FLOAT, false, 0, 0)

          gl.bindBuffer(ARRAY_BUFFER, mesh.tangentBuffer)
          gl.vertexAttribPointer(tangentLocation, 3, FLOAT, false, 0, 0)

          gl.bindBuffer(ARRAY_BUFFER, mesh.bitangentBuffer)
          gl.vertexAttribPointer(bitangentLocation, 3, FLOAT, false, 0, 0)

          gl.bindBuffer(ARRAY_BUFFER, mesh.normalBuffer)
          gl.vertexAttribPointer(normalLocation, 3, FLOAT, false, 0, 0)

          gl.bindBuffer(ARRAY_BUFFER, mesh.texCoordBuffer)
          gl.vertexAttribPointer(texCoordLocation, 2, FLOAT, false, 0, 0)

          gl.activeTexture(TEXTURE0)
          gl.bindTexture(TEXTURE_2D, mesh.material.color.texture)
          gl.uniform1i(colorSampler, 0)

          gl.activeTexture(TEXTURE1)
          gl.bindTexture(TEXTURE_2D, mesh.material.normal.texture)
          gl.uniform1i(normalSampler, 1)

          gl.activeTexture(TEXTURE2)
          gl.bindTexture(TEXTURE_2D, mesh.material.displacement.texture)
          gl.uniform1i(displacementSampler, 2)
          gl.uniform1f(displacementCoefficient, mesh.material.displacementCoefficient)

          gl.activeTexture(TEXTURE3)
          gl.bindTexture(TEXTURE_2D, mesh.material.roughness.texture)
          gl.uniform1i(roughnessSampler, 3)
          gl.uniform1f(materialShininess, mesh.material.shininess)

          gl.activeTexture(TEXTURE4)
          gl.bindTexture(TEXTURE_2D, mesh.material.occlusion.texture)
          gl.uniform1i(occlusionSampler, 4)

          gl.bindBuffer(ELEMENT_ARRAY_BUFFER, mesh.elementsBuffer)
          if (drawMode == TRIANGLES) {
            gl.drawElements(TRIANGLES, mesh.elementsCount, UNSIGNED_SHORT, 0)
          } else {
            for (i in 0 until mesh.elementsCount step 3) {
              gl.drawElements(LINE_LOOP, 3, UNSIGNED_SHORT, i * 2)
            }
          }
        }

    gl.disable(DEPTH_TEST)
    gl.disableVertexAttribArray(vertexPositionLocation)
    gl.disableVertexAttribArray(normalLocation)
    gl.disableVertexAttribArray(tangentLocation)
    gl.disableVertexAttribArray(bitangentLocation)
    gl.disableVertexAttribArray(texCoordLocation)
    gl.useProgram(null)
  }
}



