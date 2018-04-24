package com.minivac.oktracer

data class Light(val position: vec3 = vec3.create(),
                 val color: vec3 = vec3.create(),
                 var ambientCoefficient: Float = 0.20f)