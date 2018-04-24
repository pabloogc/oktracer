package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext.Companion.CLAMP_TO_EDGE
import org.khronos.webgl.WebGLRenderingContext.Companion.LINEAR
import org.khronos.webgl.WebGLRenderingContext.Companion.RGBA
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MAG_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MIN_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_S
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_T
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import org.w3c.dom.HTMLImageElement
import kotlin.browser.document

object Materials {
  val metal = Material(
      color = Texture("/oktracer/web/textures/metal/Metal_plate_005_COLOR.jpg"),
      normal = Texture("/oktracer/web/textures/metal/Metal_plate_005_NORM.jpg"),
      displacement = Texture("/oktracer/web/textures/metal/Metal_plate_005_DISP.png"),
      occlusion = Texture("/oktracer/web/textures/metal/Metal_plate_005_OCC.jpg"),
      roughness = Texture("/oktracer/web/textures/metal/Metal_plate_005_ROUGH.jpg"),
      displacementCoefficient = 10 / 255f,
      shininess = 1 / 25f
  )

  val stone = Material(
      color = Texture("/oktracer/web/textures/stone/Stone_Wall_009_COLOR.jpg"),
      normal = Texture("/oktracer/web/textures/stone/Stone_Wall_009_NORM.jpg"),
      displacement = Texture("/oktracer/web/textures/stone/Stone_Wall_009_DISP.png"),
      occlusion = Texture("/oktracer/web/textures/stone/Stone_Wall_009_OCC.jpg"),
      roughness = Texture("/oktracer/web/textures/stone/Stone_Wall_009_ROUGH.jpg"),
      displacementCoefficient = 50 / 255f,
      shininess = 50f
  )

  val rock = Material(
      color = Texture("/oktracer/web/textures/rock/Rough_Rock_022_COLOR.jpg"),
      normal = Texture("/oktracer/web/textures/rock/Rough_Rock_022_NORM.jpg"),
      displacement = Texture("/oktracer/web/textures/rock/Rough_Rock_022_DISP.png"),
      occlusion = Texture("/oktracer/web/textures/rock/Rough_Rock_022_OCC.jpg"),
      roughness = Texture("/oktracer/web/textures/rock/Rough_Rock_022_ROUGH.jpg"),
      displacementCoefficient = 40 / 255f,
      shininess = 50f
  )

  val crystal = Material(
      color = Texture("/oktracer/web/textures/crystal/Crystal_002_COLOR.jpg"),
      normal = Texture("/oktracer/web/textures/crystal/Crystal_002_NORM.jpg"),
      displacement = Texture("/oktracer/web/textures/crystal/Crystal_002_DISP.png"),
      occlusion = Texture("/oktracer/web/textures/crystal/Crystal_002_OCC.jpg"),
      roughness = Texture("/oktracer/web/textures/crystal/Crystal_002_ROUGH.jpg"),
      displacementCoefficient = 40 / 255f,
      shininess = 50f
  )

  fun load(onLoadComplete: () -> Unit = {}) {
    val materials = listOf(metal, stone, rock, crystal)
    var texturesToLoad = materials.size
    fun onMaterialLoaded() {
      texturesToLoad--
      if (texturesToLoad == 0) {
        onLoadComplete()
      }
    }
    materials.forEach {
      it.load { onMaterialLoaded() }
    }
  }
}

data class Material(
    val color: Texture,
    val normal: Texture,
    val displacement: Texture,
    val occlusion: Texture,
    val roughness: Texture,
    val displacementCoefficient: Float = 100f / 255f,
    val shininess: Float = 1f) {

  var loaded = false

  fun load(onLoadComplete: () -> Unit) {
    val textures = listOf(color, normal, displacement, occlusion, roughness)

    var texturesToLoad = textures.size
    fun onTextureLoaded() {
      texturesToLoad--
      if (texturesToLoad == 0) {
        loaded = true
        onLoadComplete()
      }
    }
    textures.forEach {
      it.load { onTextureLoaded() }
    }
  }
}

data class Texture(val path: String) {
  val texture = gl.createTexture()!!

  private fun isPowerOfTwo(v: Int): Boolean {
    return (v and (v - 1)) == 0
  }

  fun load(onLoadComplete: () -> Unit) {
    val image = document.createElement("img") as HTMLImageElement
    image.src = path
    image.addEventListener("load", {
      console.log("Texture $path loaded")
      // Now that the image has loaded make copy it to the texture.
      gl.bindTexture(TEXTURE_2D, texture)
      gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
      if (isPowerOfTwo(image.width) && isPowerOfTwo(image.height)) {
        gl.generateMipmap(TEXTURE_2D)
      } else {
        gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
        gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
      }
      onLoadComplete()
    })
  }
}