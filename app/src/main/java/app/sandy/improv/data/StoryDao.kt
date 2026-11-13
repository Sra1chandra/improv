package app.sandy.improv.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM Story WHERE id IN (SELECT DISTINCT storyId FROM Message) ORDER BY updatedAt DESC")
    fun stories(): Flow<List<Story>>

    @Query("SELECT * FROM Story WHERE id = :id")
    suspend fun story(id: String): Story?

    @Query("SELECT * FROM Message WHERE storyId = :id ORDER BY createdAt ASC")
    fun messages(id: String): Flow<List<Message>>

    @Query("SELECT * FROM Message WHERE storyId = :id ORDER BY createdAt ASC")
    suspend fun messagesOnce(id: String): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStory(story: Story)

    @Update
    suspend fun updateStory(story: Story)

    @Insert
    suspend fun insertMessage(message: Message)
}
