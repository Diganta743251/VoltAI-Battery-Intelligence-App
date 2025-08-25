package com.voltai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_samples")
data class CurrentSample(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val currentMa: Float,
    val isCharging: Boolean
)