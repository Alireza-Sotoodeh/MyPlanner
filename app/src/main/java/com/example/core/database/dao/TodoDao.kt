package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.core.database.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY sortOrder ASC, id DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE status = 'PENDING' ORDER BY sortOrder ASC, id DESC")
    fun getPendingTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllTodosSync(): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE linkedTaskId = :taskId LIMIT 1")
    suspend fun getTodoByLinkedTaskId(taskId: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE parentTodoId = :parentId ORDER BY sortOrder ASC, id DESC")
    suspend fun getSubTodosSync(parentId: Long): List<TodoEntity>

    @Query("DELETE FROM todos WHERE parentTodoId = :parentId")
    suspend fun deleteSubTodos(parentId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Transaction
    suspend fun deleteTodoAndSubTodos(todo: TodoEntity) {
        deleteSubTodos(todo.id)
        deleteTodo(todo)
    }

    @Transaction
    suspend fun updateTodoSortOrders(todos: List<TodoEntity>) {
        todos.forEach { updateTodo(it) }
    }

    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()
}
