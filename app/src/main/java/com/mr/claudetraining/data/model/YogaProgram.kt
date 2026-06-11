package com.mr.claudetraining.data.model

import com.mr.claudetraining.domain.model.YogaProgram as DomainYogaProgram

/**
 * Firestore document under the `yogaPrograms` collection. Default values are required for
 * Firebase reflective deserialization; [toDomain] maps it onto the framework-free entity.
 */
data class YogaProgramDto(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val level: String = "",
    val durationMinutes: Int = 0,
    val imageUrl: String = "",
    val videoUrl: String = ""
)

fun YogaProgramDto.toDomain() = DomainYogaProgram(
    id = id,
    title = title,
    subtitle = subtitle,
    level = level,
    durationMinutes = durationMinutes,
    imageUrl = imageUrl,
    videoUrl = videoUrl
)