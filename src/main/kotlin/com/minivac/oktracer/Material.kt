package com.minivac.oktracer

import org.khronos.webgl.WebGLRenderingContext.Companion.CLAMP_TO_EDGE
import org.khronos.webgl.WebGLRenderingContext.Companion.LINEAR
import org.khronos.webgl.WebGLRenderingContext.Companion.LINEAR_MIPMAP_NEAREST
import org.khronos.webgl.WebGLRenderingContext.Companion.REPEAT
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
  val crystal = Material(
//      "/oktracer/web/textures/crystal/color.jpg"
      "/oktracer/web/textures/grid_pattern_uv.jpg"
  )

  fun load(onLoadComplete: () -> Unit) {
    val materials = listOf(crystal)
    var texturesToLoad = materials.size
    fun onTextureLoaded() {
      texturesToLoad--
      if (texturesToLoad == 0) {
        onLoadComplete()
      }
    }
    materials.forEach {
      it.load { onTextureLoaded() }
    }
  }
}

class Material(color: String) {
  val color = Texture(color)

  fun load(onLoadComplete: () -> Unit) {
    val textures = listOf(color)

    var texturesToLoad = textures.size
    fun onTextureLoaded() {
      texturesToLoad--
      if (texturesToLoad == 0) {
        onLoadComplete()
      }
    }
    textures.forEach {
      it.load { onTextureLoaded() }
    }
  }
}

class Texture(val path: String) {
  val texture = gl.createTexture()!!

  fun load(onLoadComplete: () -> Unit) {
    val image = document.createElement("img") as HTMLImageElement
    image.src = path
    image.addEventListener("load", {
      console.log("Texture $path loaded")
      // Now that the image has loaded make copy it to the texture.
      gl.bindTexture(TEXTURE_2D, texture)
      gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
      gl.generateMipmap(TEXTURE_2D)
      onLoadComplete()
    })
  }
}