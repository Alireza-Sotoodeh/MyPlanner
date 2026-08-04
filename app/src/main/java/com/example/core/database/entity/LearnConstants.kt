package com.example.core.database.entity

object LearnConstants {
    // LearnItemEntity status values
    const val STATUS_NOT_STARTED = "NOT_STARTED"
    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_PAUSED = "PAUSED"
    const val STATUS_COMPLETED = "COMPLETED"
    const val STATUS_ARCHIVED = "ARCHIVED"

    // LearnSectionEntity status values
    const val SECTION_NOT_STARTED = "NOT_STARTED"
    const val SECTION_STUDIED = "STUDIED"
    const val SECTION_IN_REVIEW = "IN_REVIEW"
    const val SECTION_MASTERED = "MASTERED"

    // LearnItemEntity scheduleMode values
    const val SCHEDULE_CONTINUOUS = "CONTINUOUS"
    const val SCHEDULE_WEEKLY = "WEEKLY"

    // Task labels for learn items
    const val LABEL_STUDY = "Study"
    const val LABEL_REVIEW = "Review"

    // Task types
    const val TASK_TYPE_TASK = "TASK"
    const val TASK_TYPE_EVENT = "EVENT"
    const val TASK_TYPE_NOTE = "NOTE"
}