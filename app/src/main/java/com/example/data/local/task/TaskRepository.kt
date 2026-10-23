package com.example.data.local.task

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val newStatus = when (task.status) {
            "TODO" -> "IN_PROGRESS"
            "IN_PROGRESS" -> "COMPLETED"
            else -> "TODO"
        }
        taskDao.updateTask(task.copy(status = newStatus))
    }
}
