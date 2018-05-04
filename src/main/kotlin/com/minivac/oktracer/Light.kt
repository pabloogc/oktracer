package com.minivac.oktracer

import com.minivac.oktracer.matrix.vec3

data class Light(val position: vec3 = vec3.create(),
                 val color: vec3 = vec3.create(),
                 var ambientCoefficient: Float = 0.20f)