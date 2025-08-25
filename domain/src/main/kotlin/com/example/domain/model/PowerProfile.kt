package com.voltai.domain.model

data class PowerProfile(
    val name: String,
    val brightness: Int,
    val bluetooth: Boolean,
    val sync: Boolean,
    val vibration: Boolean
)
