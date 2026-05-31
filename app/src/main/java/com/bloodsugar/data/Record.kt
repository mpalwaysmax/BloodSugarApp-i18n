package com.bloodsugar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val value: Float,           // mmol/L，UI层校验范围 1.0-33.3
    val segment: String,        // MealSegment 枚举的 name
    val note: String = "",      // 备注，默认空字符串
    val timestamp: Long = System.currentTimeMillis()  // 自动取当前时间
)
