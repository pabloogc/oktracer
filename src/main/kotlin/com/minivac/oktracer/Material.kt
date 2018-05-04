package com.minivac.oktracer

import org.khronos.webgl.Uint8Array
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
  val debug = Material(
      color = FileTexture("/oktracer/web/textures/debug/debug.png"),
      normal = ByteArrayTexture(1, 1, byteArray = Uint8Array(arrayOf(128, 128, 255, 0).toByteArray())),
      displacement = ByteArrayTexture(1, 1, byteArray = Uint8Array(arrayOf(0, 0, 0, 0).toByteArray())),
      occlusion = ByteArrayTexture(1, 1, byteArray = Uint8Array(arrayOf(255, 255, 255, 255).toByteArray())),
      roughness = ByteArrayTexture(1, 1, byteArray = Uint8Array(arrayOf(0, 0, 0, 0).toByteArray())),
      displacementCoefficient = 1f,
      shininess = 1 / 25f
  )

  val metal = Material(
      color = FileTexture("/oktracer/web/textures/metal/Metal_plate_005_COLOR.jpg"),
      normal = FileTexture("/oktracer/web/textures/metal/Metal_plate_005_NORM.jpg"),
      displacement = FileTexture("/oktracer/web/textures/metal/Metal_plate_005_DISP.png"),
      occlusion = FileTexture("/oktracer/web/textures/metal/Metal_plate_005_OCC.jpg"),
      roughness = FileTexture("/oktracer/web/textures/metal/Metal_plate_005_ROUGH.jpg"),
      displacementCoefficient = 10 / 255f,
      shininess = 1 / 25f
  )

  val stone = Material(
      color = FileTexture("/oktracer/web/textures/stone/Stone_Wall_009_COLOR.jpg"),
      normal = FileTexture("/oktracer/web/textures/stone/Stone_Wall_009_NORM.jpg"),
      displacement = FileTexture("/oktracer/web/textures/stone/Stone_Wall_009_DISP.png"),
      occlusion = FileTexture("/oktracer/web/textures/stone/Stone_Wall_009_OCC.jpg"),
      roughness = FileTexture("/oktracer/web/textures/stone/Stone_Wall_009_ROUGH.jpg"),
      displacementCoefficient = 50 / 255f,
      shininess = 50f
  )

  val rock = Material(
      color = FileTexture("/oktracer/web/textures/rock/Rough_Rock_022_COLOR.jpg"),
      normal = FileTexture("/oktracer/web/textures/rock/Rough_Rock_022_NORM.jpg"),
      displacement = FileTexture("/oktracer/web/textures/rock/Rough_Rock_022_DISP.png"),
      occlusion = FileTexture("/oktracer/web/textures/rock/Rough_Rock_022_OCC.jpg"),
      roughness = FileTexture("/oktracer/web/textures/rock/Rough_Rock_022_ROUGH.jpg"),
      displacementCoefficient = 40 / 255f,
      shininess = 50f
  )

  val crystal = Material(
      color = FileTexture("/oktracer/web/textures/crystal/Crystal_002_COLOR.jpg"),
      normal = FileTexture("/oktracer/web/textures/crystal/Crystal_002_NORM.jpg"),
      displacement = FileTexture("/oktracer/web/textures/crystal/Crystal_002_DISP.png"),
      occlusion = FileTexture("/oktracer/web/textures/crystal/Crystal_002_OCC.jpg"),
      roughness = FileTexture("/oktracer/web/textures/crystal/Crystal_002_ROUGH.jpg"),
      displacementCoefficient = 40 / 255f,
      shininess = 50f
  )

  fun load(onLoadComplete: () -> Unit = {}) {
    val materials = listOf(debug, metal, stone, rock, crystal)
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

abstract class Texture(val linear: Boolean = true, val repeat: Boolean = false) {
  val texture = gl.createTexture()!!
  var width = -1
  var height = -1

  protected fun isPowerOfTwo(v: Int): Boolean {
    return (v and (v - 1)) == 0
  }

  protected fun generateMimmaps() {
    if (isPowerOfTwo(width) && isPowerOfTwo(height)) {
      gl.generateMipmap(TEXTURE_2D)
    } else {
      gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
      gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
      gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
      gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
    }
  }

  abstract fun load(onLoadComplete: () -> Unit)
}

class FileTexture(val path: String,
                  linear: Boolean = true,
                  repeat: Boolean = false) : Texture(linear = linear, repeat = repeat) {

  override fun load(onLoadComplete: () -> Unit) {
    val image = document.createElement("img") as HTMLImageElement
    image.src = path
    image.addEventListener("load", {
      console.log("Texture $path loaded")
      width = image.width
      height = image.height
      // Now that the image has loaded make copy it to the texture.
      gl.bindTexture(TEXTURE_2D, texture)
      gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
      generateMimmaps()
      onLoadComplete()
    })
  }
}

class ByteArrayTexture(width: Int, height: Int,
                       val byteArray: Uint8Array,
                       linear: Boolean = true,
                       repeat: Boolean = false) : Texture(linear = linear, repeat = repeat) {
  init {
    this.width = width
    this.height = height
  }

  override fun load(onLoadComplete: () -> Unit) {
    gl.bindTexture(TEXTURE_2D, texture)
    gl.texImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, byteArray)
    generateMimmaps()
    onLoadComplete()
  }
}

private fun Array<Int>.toByteArray(): Array<Byte> {
  return this.map { it.toByte() }.toTypedArray()
}


