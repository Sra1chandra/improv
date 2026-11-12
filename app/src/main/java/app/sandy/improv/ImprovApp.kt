package app.sandy.improv

import android.app.Application
import androidx.room.Room
import app.sandy.improv.data.AppDb
import app.sandy.improv.data.PrefsKeyStore

class ImprovApp : Application() {
    lateinit var db: AppDb
        private set
    lateinit var keys: PrefsKeyStore
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "sandy.db").build()
        keys = PrefsKeyStore(this)
    }
}
