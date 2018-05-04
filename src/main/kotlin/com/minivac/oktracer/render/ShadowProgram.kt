package com.minivac.oktracer.render

import com.minivac.oktracer.MAX_LIGHTS
import com.minivac.oktracer.createProgram
import com.minivac.oktracer.gl
import com.minivac.oktracer.mesh.Mesh
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLUniformLocation


//language=GLSL
private const val VS = """

    attribute vec3 aNormal;
    attribute vec2 aTexCoord;
    attribute vec3 aVertexPosition;

    uniform mat3 uNMatrix; //normal to model space
    uniform mat4 uCMatrix; //Camera
    uniform mat4 uPMatrix; //Projection
    uniform mat4 uMVMatrix; //Model-View

    //Material
    uniform sampler2D uDisplacementSampler;
    uniform float uDisplacementCoefficient;

    //Tangent to model space
    varying vec3 vPosition;

    void main(void) {
        vec3 displacement = aNormal
            * texture2D(uDisplacementSampler, aTexCoord).r
            * uDisplacementCoefficient;
        vec4 displacedPosition = vec4(aVertexPosition + displacement, 1.0);

        vPosition = (uMVMatrix * displacedPosition).xyz;
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

    //Material
    varying vec3 vPosition;

    void main(void) {
        vec3 color = vec3(0.0, 0.0, 0.0);

        for(int i = 0; i < MAX_LIGHTS; i++) {
            //Light properties
            vec3 lightPosition = uLightPosition[i];

            //Generic light
            vec3 surfaceToLight = normalize(lightPosition - vPosition);
            float surfaceToLightDistance = length(lightPosition - vPosition);
        }

        gl_FragColor = vec4(color, 1.0);
    }
"""

class ShadowProgram {
  val glProgram by lazy {
    createProgram(VS, FS)!!
  }

  //Camera
  val cameraMatrixLocation: WebGLUniformLocation?
  val projectionMatrixLocation: WebGLUniformLocation?
  val modelViewMatrixLocation: WebGLUniformLocation?
  val normalMatrixLocation: WebGLUniformLocation?

  //Lights
  val lightPositionLocation: WebGLUniformLocation?
  val displacementSampler: WebGLUniformLocation?
  val displacementCoefficient: WebGLUniformLocation?

  //Attributes
  val vertexPositionLocation: Int
  val texCoordLocation: Int
  val normalLocation: Int

  init {
    gl.useProgram(glProgram)

    cameraMatrixLocation = gl.getUniformLocation(glProgram, "uCMatrix")
    projectionMatrixLocation = gl.getUniformLocation(glProgram, "uPMatrix")
    modelViewMatrixLocation = gl.getUniformLocation(glProgram, "uMVMatrix")
    normalMatrixLocation = gl.getUniformLocation(glProgram, "uNMatrix")

    //Light
    lightPositionLocation = gl.getUniformLocation(glProgram, "uLightPosition")

    //Material
    displacementSampler = gl.getUniformLocation(glProgram, "uDisplacementSampler")
    displacementCoefficient = gl.getUniformLocation(glProgram, "uDisplacementCoefficient")


    vertexPositionLocation = gl.getAttribLocation(glProgram, "aVertexPosition")
    texCoordLocation = gl.getAttribLocation(glProgram, "aTexCoord")
    normalLocation = gl.getAttribLocation(glProgram, "aNormal")

    gl.enableVertexAttribArray(vertexPositionLocation)
    gl.enableVertexAttribArray(normalLocation)
    gl.enableVertexAttribArray(texCoordLocation)
  }

  fun render(meshList: List<Mesh>) {
    meshList
        .filter { it.material.loaded }
        .forEach { mesh ->
          gl.uniformMatrix4fv(modelViewMatrixLocation, false, mesh.modelMatrix)
          gl.uniformMatrix3fv(normalMatrixLocation, false, mesh.normalMatrix)

          gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, mesh.verticesBuffer)
          gl.vertexAttribPointer(vertexPositionLocation, 3, WebGLRenderingContext.FLOAT, false, 0, 0)

          gl.activeTexture(WebGLRenderingContext.TEXTURE2)
          gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, mesh.material.displacement.texture)
          gl.uniform1i(displacementSampler, 0)
          gl.uniform1f(displacementCoefficient, mesh.material.displacementCoefficient)

          gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, mesh.elementsBuffer)
          gl.drawElements(TRIANGLES, mesh.elementsCount, WebGLRenderingContext.UNSIGNED_SHORT, 0)
        }
  }

  fun useProgram() {
    gl.useProgram(glProgram)
  }
}



