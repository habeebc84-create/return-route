package com.example.data.local.task

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val projectName: String = "ReturnRoute Core",
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val status: String = "TODO", // TODO, IN_PROGRESS, COMPLETED
    val assignedTo: String = "Engineering Team",
    val createdAt: Long = System.currentTimeMillis()
)
