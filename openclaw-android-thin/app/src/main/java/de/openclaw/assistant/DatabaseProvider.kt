package de.openclaw.assistant

import androidx.room.Room
import de.openclaw.assistant.data.local.OpenClawDatabase
import de.openclaw.assistant.data.local.SettingsDataStore

object DatabaseProvider {
    private var database: OpenClawDatabase? = null
    private var settingsDataStore: SettingsDataStore? = null
    
    fun getDatabase(context: android.content.Context): OpenClawDatabase {
        return database ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                OpenClawDatabase::class.java,
                "openclaw_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            .also { database = it }
        }
    }
    
    fun getSettingsDataStore(context: android.content.Context): SettingsDataStore {
        return settingsDataStore ?: synchronized(this) {
            SettingsDataStore(context).also { settingsDataStore = it }
        }
    }
}
