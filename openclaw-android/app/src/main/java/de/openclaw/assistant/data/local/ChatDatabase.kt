package de.openclaw.assistant.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val model: String?,
    val tokens: Int = 0,
    val timestamp: Long,
    val synced: Boolean = false
)

@Entity(tableName = "pending_messages")
data class PendingMessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val timestamp: Long,
    val retryCount: Int = 0
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getMessagesForUser(userId: String, limit: Int = 100): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE synced = 0")
    suspend fun getUnsyncedMessages(): List<ChatMessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
    
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)
    
    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearMessagesForUser(userId: String)
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE userId = :userId")
    suspend fun getMessageCount(userId: String): Int
}

@Dao
interface PendingMessageDao {
    @Query("SELECT * FROM pending_messages ORDER BY timestamp ASC")
    fun getAllPending(): Flow<List<PendingMessageEntity>>
    
    @Insert
    suspend fun insertPending(message: PendingMessageEntity)
    
    @Delete
    suspend fun deletePending(message: PendingMessageEntity)
    
    @Query("DELETE FROM pending_messages")
    suspend fun clearAll()
}

@Database(
    entities = [ChatMessageEntity::class, PendingMessageEntity::class],
    version = 1
)
abstract class OpenClawDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun pendingMessageDao(): PendingMessageDao
}
