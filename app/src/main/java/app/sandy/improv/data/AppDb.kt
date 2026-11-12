package app.sandy.improv.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Story::class, Message::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun stories(): StoryDao
}
