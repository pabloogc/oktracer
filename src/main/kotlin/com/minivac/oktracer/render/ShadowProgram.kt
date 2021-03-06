package com.minivac.oktracer.render

import com.minivac.oktracer.*
import com.minivac.oktracer.matrix.toFloatArray
import com.minivac.oktracer.mesh.Mesh
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE0
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE1
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE2
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE3
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE4
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.WebGLUniformLocation


//language=GLSL
private const val VS = """
    attribute vec3 aVertexPosition;
    attribute vec2 aTexCoord;
    attribute vec3 aNormal;

    uniform mat3 uNMatrix; //normal to model space
    uniform mat4 uCMatrix; //Camera
    uniform mat4 uPMatrix; //Projection
    uniform mat4 uMVMatrix; //Model-View

    //Material
    uniform sampler2D uDisplacementSampler;
    uniform float uDisplacementCoefficient;

    //Tangent to model space
    varying vec3 vPosition;
    varying vec2 vTexCoord;

    void main(void) {
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

    //Textures
    uniform sampler2D uOcclusionSampler;

    //Material
    varying vec3 vPosition;
    varying vec2 vTexCoord;

    void main(void) {
        vec3 color = vec3(0.0, 0.0, 0.0);

        for(int i = 0; i < MAX_LIGHTS; i++) {
            //Light properties
            vec3 lightPosition = uLightPosition[i];
            vec3 surfaceToLight = normalize(lightPosition - vPosition);

            float surfaceToLightDistance = length(lightPosition - vPosition);
            color += vec3(surfaceToLightDistance * 0.1);
        }
        gl_FragColor = vec4(color, 1.0);
    }
"""

class ShadowProgram {
  val glProgram by lazy {
    createProgram(VS, FS)!!
  }
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
    gl.enableVertexAttribArray(vertexPositionLocation)
    gl.enableVertexAttribArray(normalLocation)
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
          gl.drawElements(TRIANGLES, mesh.elementsCount, UNSIGNED_SHORT, 0)
        }

    gl.disableVertexAttribArray(vertexPositionLocation)
    gl.disableVertexAttribArray(normalLocation)
    gl.disableVertexAttribArray(texCoordLocation)
    gl.useProgram(null)
  }
}



