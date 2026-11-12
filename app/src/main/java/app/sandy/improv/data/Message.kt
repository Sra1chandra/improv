package app.sandy.improv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey val id: String,
    val storyId: String,
    val role: String,
    val text: String,
    val createdAt: Long,
)
