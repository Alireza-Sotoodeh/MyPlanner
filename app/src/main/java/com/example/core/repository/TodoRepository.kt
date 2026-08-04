package com.example.core.repository

import com.example.core.database.dao.TodoDao
import com.example.core.database.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoEntity>> = todoDao.getAllTodos()

    val pendingTodos: Flow<List<TodoEntity>> = todoDao.getPendingTodos()

    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)

    suspend fun getTodoByLinkedTaskId(taskId: Long): TodoEntity? =
        todoDao.getTodoByLinkedTaskId(taskId)

    suspend fun insertTodo(todo: TodoEntity): Long = todoDao.insertTodo(todo)

    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)

    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)
}
