package com.locationjoystick.feature.settings.impl

data class ChunkEnvelope(
    val k: String = "lj.s",
    val v: Int = 2,
    val session: String,
    val chunk: Int,
    val total: Int,
    val d: String,
)
