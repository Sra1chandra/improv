package app.sandy.improv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Story(
    @PrimaryKey val id: String,
    val title: String,
    val letter: String,
    val lastLine: String,
    val createdAt: Long,
    val updatedAt: Long,
)
