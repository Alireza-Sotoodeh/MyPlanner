package com.example.core.database.entity

enum class LearnStatus(val value: String) {
    NOT_STARTED("NOT_STARTED"),
    ACTIVE("ACTIVE"),
    PAUSED("PAUSED"),
    COMPLETED("COMPLETED"),
    ARCHIVED("ARCHIVED"),
    STUDIED("STUDIED"),
    IN_REVIEW("IN_REVIEW"),
    MASTERED("MASTERED");

    companion object {
        fun fromString(value: String): LearnStatus = values().firstOrNull { it.value == value } ?: NOT_STARTED
    }
}